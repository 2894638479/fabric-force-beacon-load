package name.forcebeaconload.mixin.client;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    private float viewDistance;

    @Inject(method = "getFarPlaneDistance",at = @At("RETURN"),cancellable = true)
    private void forcebeaconload$changeFarPlane(CallbackInfoReturnable<Float> cir){
        cir.setReturnValue(viewDistance*4 + 3000);
    }
}
