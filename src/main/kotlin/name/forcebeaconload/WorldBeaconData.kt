package name.forcebeaconload

import net.minecraft.block.Blocks
import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

class WorldBeaconData(nbt: NbtCompound,lookup:RegistryWrapper.WrapperLookup): PersistentState() {
    val beacons = readBeacons(nbt,lookup).associateBy { it.pos }.toMutableMap()
    private var lastSendTime = 0L
    fun tick(world: ServerWorld){
        if(world.time - lastSendTime > 100){
            ForceBeaconLoad.sendToAllPlayer(world)
            lastSendTime = world.time
        }
    }
    fun add(beacon: BeaconBlockEntity,world: ServerWorld){
        if(beacons[beacon.pos] != beacon && beacon.valid){
            beacons[beacon.pos] = beacon
            markDirty()
            ForceBeaconLoad.sendToAllPlayer(world)
            lastSendTime = world.time
        }
    }
    fun remove(pos: BlockPos,world: ServerWorld){
        if(beacons.remove(pos) != null) {
            markDirty()
            ForceBeaconLoad.sendToAllPlayer(world)
            lastSendTime = world.time
        }
    }
    fun check(world: World){
        beacons.entries.removeIf { (pos,entity) ->
            if(!world.isChunkLoaded(pos)) false
            else world.getBlockEntity(pos) !is BeaconBlockEntity
        }
        if(false)
        beacons.replaceAll { pos,entity ->
            (world.getBlockEntity(pos) as? BeaconBlockEntity)
                ?.takeIf { it.valid } ?: entity
        }
    }
    val isEmpty get() = beacons.isEmpty()
    override fun writeNbt(nbt: NbtCompound,registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        saveBeacons(beacons.values,nbt,registryLookup)
        return nbt
    }
    fun invalidate() { lastSendTime = 0 }

    companion object {
        val BeaconBlockEntity.valid get() = (this as IsLevelValid).`forcebeaconload$isLevelValid`
        private fun saveBeacons(beacons: Collection<BeaconBlockEntity>,nbt: NbtCompound,lookup:RegistryWrapper.WrapperLookup) {
            val listTag = NbtList()
            for (beacon in beacons) {
                val tag = NbtCompound()
                beacon.writeNbt(tag,lookup)
                tag.putInt("x",beacon.pos.x)
                tag.putInt("y",beacon.pos.y)
                tag.putInt("z",beacon.pos.z)
                val segments = NbtList()
                for(segment in beacon.beamSegments){
                    val segNbt = NbtCompound()
                    segNbt.putInt("h",segment.height)
                    segNbt.putInt("color",segment.color)
                    segments.add(segNbt)
                }
                tag.put("segments",segments)
                listTag.add(tag)
            }
            nbt.put("beacons",listTag)
        }
        private fun readBeacons(nbt: NbtCompound,lookup:RegistryWrapper.WrapperLookup):List<BeaconBlockEntity> {
            val listTag = nbt.getList("beacons",10)
            return List(listTag.size) {
                val tag = listTag.getCompound(it)
                val pos = BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))
                val segNbts = tag.getList("segments",10)
                val segments = List(segNbts.size){
                    val tag = segNbts.getCompound(it)
                    val height = tag.getInt("h")
                    BeaconBlockEntity.BeamSegment(tag.getInt("color"))
                        .also { it.height = height }
                }
                BeaconBlockEntity(pos,Blocks.BEACON.defaultState).apply {
                    readNbt(tag,lookup)
                    level = tag.getInt("Levels")
                    beamSegments = segments
                }
            }
        }
    }
}