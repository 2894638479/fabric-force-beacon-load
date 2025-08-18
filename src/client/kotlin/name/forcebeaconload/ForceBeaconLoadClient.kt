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
    val beaconMaps = mutableMapOf<Identifier, Map<BlockPos, BeaconBlockEntity>>()
	override fun onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register { handler,sender,client ->
            beaconMaps.clear()
        }
        ClientPlayNetworking.registerGlobalReceiver(ForceBeaconLoad.beaconDataPackId){ client,handler,buf,sender ->
            val nbt = buf.readNbt() ?: return@registerGlobalReceiver
            val receivedWorld = Identifier.tryParse(nbt.getString("world")) ?: return@registerGlobalReceiver
            beaconMaps[receivedWorld] = WorldBeaconData(nbt).beacons
            setWorld(client.world)
        }
    }
    fun setWorld(world: ClientWorld?){
        beaconMaps.forEach { (id,map)->
            if(id == world?.dimensionKey?.value){
                map.forEach { (_,entity) -> entity.world = world }
            }
        }
    }
    fun getBeacons(world: World?): List<BeaconBlockEntity>{
        if (world == null) return listOf()
        val map = beaconMaps[world.dimensionKey.value] ?: return listOf()
        return map.filter { it.value.world == world }.values.toList()
    }
}