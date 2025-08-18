package name.forcebeaconload

import net.minecraft.block.entity.BeaconBlockEntity

object BeaconLevelShrinker:(BeaconBlockEntity)->Int {
    override fun invoke(entity: BeaconBlockEntity): Int {
        return (entity as HasLevelShrink).`forcebeaconload$levelShrink`
    }
}