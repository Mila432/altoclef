package adris.altoclef.mixins;

//#if MC <= 11605
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.item.MiningToolItem;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ import java.util.Set;
//$$
//$$ @Mixin(MiningToolItem.class)
//#endif
public interface MiningToolItemAccessor {

    //#if MC <= 11605
    //$$ @Accessor("effectiveBlocks")
    //$$ Set<Block> getEffectiveBlocks();
    //#endif

}
