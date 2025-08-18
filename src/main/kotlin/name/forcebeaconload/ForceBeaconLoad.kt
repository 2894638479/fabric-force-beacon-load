package name.forcebeaconload

import io.netty.buffer.Unpooled
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory


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
}