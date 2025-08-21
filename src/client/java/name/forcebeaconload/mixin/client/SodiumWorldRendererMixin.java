package name.forcebeaconload.mixin.client;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import name.forcebeaconload.ForceBeaconLoadClient;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {

    @Shadow
    private static void renderBlockEntity(MatrixStack matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<net.minecraft.entity.player.BlockBreakingInfo>> blockBreakingProgressions, float tickDelta, VertexConsumerProvider.Immediate immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, ClientPlayerEntity player, LocalBooleanRef isGlowing) {}

    @Shadow
    private ClientWorld level;

    @Inject(method = "renderGlobalBlockEntities",at = @At("HEAD"))
    void forcebeaconload$renderAllCachedBeacons(MatrixStack matrices, BufferBuilderStorage bufferBuilders, Long2ObjectMap<SortedSet<net.minecraft.entity.player.BlockBreakingInfo>> blockBreakingProgressions, float tickDelta, VertexConsumerProvider.Immediate immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, ClientPlayerEntity player, LocalBooleanRef isGlowing, CallbackInfo ci){
        ForceBeaconLoadClient.INSTANCE.getBeacons(level).forEach((entity)->
                renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer,entity,player,isGlowing));
    }
}
