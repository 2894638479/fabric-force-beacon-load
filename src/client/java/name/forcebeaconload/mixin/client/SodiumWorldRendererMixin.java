package name.forcebeaconload.mixin.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import name.forcebeaconload.ForceBeaconLoadClient;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {
    @Shadow
    private static void renderBlockEntity(MatrixStack matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions, float tickDelta, VertexConsumerProvider.Immediate immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity) {}

    @Shadow
    private ClientWorld world;

    @Inject(method = "renderGlobalBlockEntities",at = @At("HEAD"))
    void forcebeaconload$renderAllCachedBeacons(MatrixStack matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions, float tickDelta, VertexConsumerProvider.Immediate immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci){
        ForceBeaconLoadClient.INSTANCE.getBeacons(world).forEach((entity)->
                renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, entity));
    }
}
