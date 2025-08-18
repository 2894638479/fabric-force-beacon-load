package name.forcebeaconload.mixin.client;

import name.forcebeaconload.ForceBeaconLoadClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setWorld",at = @At("RETURN"))
    void forcebeaconload$setBeaconsWorld(ClientWorld world, CallbackInfo ci){
        ForceBeaconLoadClient.INSTANCE.setWorld(world);
    }
}
