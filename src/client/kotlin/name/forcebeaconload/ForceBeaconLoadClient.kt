package name.forcebeaconload

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ForceBeaconLoadClient : ClientModInitializer {
    val ClientWorld.id get() = dimensionEntry.key.orElse(null)?.value
    val beaconMaps = mutableMapOf<Identifier, Map<BlockPos, BeaconBlockEntity>>()
	override fun onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register { handler,sender,client ->
            beaconMaps.clear()
        }
        ClientPlayNetworking.registerGlobalReceiver(BeaconDataPayload.ID){ payload,context ->
            val nbt = payload.nbt
            val receivedWorldId = Identifier.tryParse(nbt.getString("world")) ?: return@registerGlobalReceiver
            val world = context.client().world ?: return@registerGlobalReceiver
            val worldId = world.id ?: return@registerGlobalReceiver
            if(worldId != receivedWorldId) return@registerGlobalReceiver
            beaconMaps[receivedWorldId] = WorldBeaconData(nbt,world.registryManager).beacons
            setWorld(world)
        }
    }
    fun setWorld(world: ClientWorld?){
        beaconMaps.forEach { (id,map)->
            if(id == world?.id){
                map.forEach { (_,entity) -> entity.world = world }
            }
        }
    }
    fun getBeacons(world: World?): List<BeaconBlockEntity>{
        if (world !is ClientWorld) return listOf()
        val key = world.id ?: return listOf()
        val map = beaconMaps[key] ?: return listOf()
        return map.filter { it.value.world == world }.values.toList()
    }
}