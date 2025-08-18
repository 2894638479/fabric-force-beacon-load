package name.forcebeaconload.mixin;

import name.forcebeaconload.ForceBeaconLoad;
import name.forcebeaconload.IsLevelValid;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin implements IsLevelValid {
    @Unique boolean forcebeaconload$isLevelValid = false;
    @Inject(method = "tick",at = @At("RETURN"))
    private static void forcebeaconload$updateBeaconData(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci){
        if(world instanceof ServerWorld world1) {
            ForceBeaconLoad.INSTANCE.getBeaconData(world1).add(blockEntity,world1);
        }
    }
    @Inject(method = "tick",at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/entity/BeaconBlockEntity;updateLevel(Lnet/minecraft/world/World;III)I"))
    private static void forcebeaconload$markLevelValid(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci){
        ((IsLevelValid)blockEntity).setForcebeaconload$isLevelValid(true);
    }
    @Inject(method = "updateLevel",at = @At("RETURN"))
    private static void forcebeaconload$updateBeaconData(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir){
        if(world instanceof ServerWorld world1) {
            ForceBeaconLoad.INSTANCE.getBeaconData(world1).invalidate();
        }
    }
    @Redirect(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BeaconBlockEntity;applyPlayerEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/entity/effect/StatusEffect;Lnet/minecraft/entity/effect/StatusEffect;)V"))
    private static void forcebeaconload$notAddPlayerEffects(World world, BlockPos pos, int beaconLevel, StatusEffect primaryEffect, StatusEffect secondaryEffect){}

    @Redirect(method = "applyPlayerEffects",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;"))
    private static Box forcebeaconload$changePlayerEffectDistance(Box instance, double value){
        return instance.expand(2);
    }
    @Override
    public boolean getForcebeaconload$isLevelValid() {
        return forcebeaconload$isLevelValid;
    }
    @Override
    public void setForcebeaconload$isLevelValid(boolean b) {
        forcebeaconload$isLevelValid = b;
    }
}
