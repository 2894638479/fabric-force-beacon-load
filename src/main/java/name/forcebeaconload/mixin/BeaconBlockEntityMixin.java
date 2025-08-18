package name.forcebeaconload.mixin;

import name.forcebeaconload.ForceBeaconLoad;
import name.forcebeaconload.HasLevelShrink;
import name.forcebeaconload.IsLevelValid;
import name.forcebeaconload.UpdateLevelShrink;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin implements IsLevelValid, HasLevelShrink, UpdateLevelShrink {
    @Shadow
    public int level;
    @Unique boolean forcebeaconload$isLevelValid = false;
    @Unique int forcebeaconload$levelShrink = 0;
    @Unique
    private int forcebeaconload$calLevelShrink(){
        BeaconBlockEntity entity = (BeaconBlockEntity)(Object) this;
        World world = entity.getWorld();
        if(world == null) return 0;
        BlockPos pos = entity.getPos();
        for(int i = 0;i < level;i++){
            BlockPos detectPos = new BlockPos(pos.getX(), pos.getY() + 1+i, pos.getZ());
            BlockState state = world.getBlockState(detectPos);
            boolean shrink = state.isOf(Blocks.GLASS) || state.isOf(Blocks.GLASS_PANE);
            if(!shrink) return i;
        }
        return level;
    }

    @Inject(method = "tick",at = @At("RETURN"))
    private static void forcebeaconload$updateBeaconData(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci){
        if(world instanceof ServerWorld world1) {
            ForceBeaconLoad.INSTANCE.getBeaconData(world1).add(blockEntity,world1);
        }
    }
    @Inject(method = "tick",at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/entity/BeaconBlockEntity;updateLevel(Lnet/minecraft/world/World;III)I"))
    private static void forcebeaconload$onLevelUpdated(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci){
        ((UpdateLevelShrink)blockEntity).forcebeaconload$updateLevelShrink();
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
    @Override
    public int getForcebeaconload$levelShrink() {
        return forcebeaconload$levelShrink;
    }
    @Override
    public void setForcebeaconload$levelShrink(int forcebeaconload$levelShrink) {
        this.forcebeaconload$levelShrink = forcebeaconload$levelShrink;
    }
    @Override
    public void forcebeaconload$updateLevelShrink() {
        forcebeaconload$levelShrink = forcebeaconload$calLevelShrink();
    }
    @Inject(method = "writeNbt",at = @At("HEAD"))
    void forcebeaconload$writeExtra(NbtCompound nbt, CallbackInfo ci){
        nbt.putInt("levelShrink",forcebeaconload$levelShrink);
    }
    @Inject(method = "readNbt",at = @At("HEAD"))
    void forcebeaconload$readExtra(NbtCompound nbt, CallbackInfo ci){
        forcebeaconload$levelShrink = nbt.getInt("levelShrink");
    }
}
