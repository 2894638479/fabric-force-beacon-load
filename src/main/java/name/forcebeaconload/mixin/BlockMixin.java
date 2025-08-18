package name.forcebeaconload.mixin;

import name.forcebeaconload.ForceBeaconLoad;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "replace(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V",at = @At("HEAD"))
    private static void forcebeaconload$updateBeaconData(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci){
        if(world instanceof ServerWorld world1) {
            if(state.getBlock() == Blocks.BEACON) {
                ForceBeaconLoad.INSTANCE.getBeaconData(world1).remove(pos,world1);
            }
            if(newState.getBlock() == Blocks.BEACON || state.getBlock() == Blocks.BEACON) {
                ForceBeaconLoad.INSTANCE.getBeaconData(world1).invalidate();
            }
        }
    }
    @Inject(method = "onBroken",at = @At("HEAD"))
    private static void forcebeaconload$updateBeaconData(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci){
        if(world instanceof ServerWorld world1) {
            if(state.getBlock() == Blocks.BEACON) {
                ForceBeaconLoad.INSTANCE.getBeaconData(world1).remove(pos,world1);
            }
        }
    }
}
