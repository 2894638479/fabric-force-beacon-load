package name.forcebeaconload

import io.netty.buffer.Unpooled
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.math.sqrt


object ForceBeaconLoad : ModInitializer {
    private val logger = LoggerFactory.getLogger("force-beacon-load")

	override fun onInitialize() {
        ServerPlayConnectionEvents.JOIN.register { handler,sender,server ->
            sendBeaconDataToPlayer(handler.player)
        }
        ServerPlayerEvents.AFTER_RESPAWN.register { oldPlayer, newPlayer, alive ->
            sendBeaconDataToPlayer(newPlayer)
        }
        ServerTickEvents.END_WORLD_TICK.register { world: ServerWorld ->
            world.beaconData.tick(world)
            if(world.time % 80L == 0L) world.players.forEach { player ->
                world.beaconData.beacons.forEach { (_,beacon) ->
                    if(shouldAddEffectToPlayer(beacon,player)){
                        addEffectToPlayer(beacon,player)
                    }
                }
            }
        }
    }
    val ServerWorld.beaconData get() = chunkManager.persistentStateManager.getOrCreate(
        { WorldBeaconData(it) },{ WorldBeaconData(NbtCompound())},"beacons")
    val beaconDataPackId = Identifier("forcebeaconload","beacon_data")
    fun sendBeaconDataToPlayer(player: ServerPlayerEntity,check: Boolean = true){
        val world = (player.world as? ServerWorld) ?: return
        val worldId = world.dimensionKey.value
        val beaconData = world.beaconData.apply { if(check) check(world) }
        if(beaconData.isEmpty) return
        val nbt = beaconData.writeNbt(NbtCompound())
        nbt.putString("world",worldId.toString())
        ServerPlayNetworking.send(player,beaconDataPackId,
            PacketByteBuf(Unpooled.buffer()).writeNbt(nbt))
    }
    fun sendToAllPlayer(world: ServerWorld){
        world.beaconData.check(world)
        world.players.forEach {
            sendBeaconDataToPlayer(it,false)
        }
    }
    fun shouldAddEffectToPlayer(beacon: BeaconBlockEntity,player: ServerPlayerEntity): Boolean{
        val level = beacon.level
        val posP = player.pos
        val posB = beacon.pos
        val distance = sqrt((posB.x - posP.x).pow(2) + (posB.y - posP.y).pow(2))
        return distance < (level * 50 + 100)
    }
    fun addEffectToPlayer(beacon: BeaconBlockEntity,player: ServerPlayerEntity){
        val pos = BlockPos.ofFloored(player.pos)
        BeaconBlockEntity.applyPlayerEffects(player.world,pos, beacon.level,beacon.primary,beacon.secondary)
    }
}