package name.forcebeaconload.mixin.client;

import name.forcebeaconload.ForceBeaconLoadClient;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    @Final
    private Set<BlockEntity> noCullingBlockEntities;

    @Shadow
    @Nullable
    private ClientWorld world;

    @Inject(method = "render",at = @At("HEAD"))
    void forcebeaconload$renderAllBeacons(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        noCullingBlockEntities.removeIf((entity) -> entity instanceof BeaconBlockEntity);
        noCullingBlockEntities.addAll(ForceBeaconLoadClient.INSTANCE.getBeacons(world));
    }
}
