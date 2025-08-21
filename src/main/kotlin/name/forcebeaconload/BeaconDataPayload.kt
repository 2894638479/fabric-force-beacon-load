package name.forcebeaconload

import name.forcebeaconload.ForceBeaconLoad.beaconDataPackId
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class BeaconDataPayload(val nbt : NbtCompound) : CustomPayload {
    override fun getId() = ID
    companion object {
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, BeaconDataPayload>({ buf, payload ->
            buf.writeNbt(payload.nbt)
        }){ BeaconDataPayload(it.readNbt() ?: NbtCompound()) }
        val ID = CustomPayload.Id<BeaconDataPayload>(beaconDataPackId)
    }
}