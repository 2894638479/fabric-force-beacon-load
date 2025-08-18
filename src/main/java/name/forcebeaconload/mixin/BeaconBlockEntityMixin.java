package name.forcebeaconload.mixin;

import name.forcebeaconload.ForceBeaconLoad;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @Inject(method = "tick",at = @At("RETURN"))
    private static void forcebeaconload$updateBeaconData(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci){
        if(world instanceof ServerWorld world1) {
            ForceBeaconLoad.INSTANCE.getBeaconData(world1).add(blockEntity,world1);
        }
    }
}
