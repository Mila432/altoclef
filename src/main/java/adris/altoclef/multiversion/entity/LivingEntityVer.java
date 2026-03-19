package adris.altoclef.multiversion.entity;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//#if MC >= 12111
import com.google.common.collect.Iterables;
import net.minecraft.entity.EquipmentSlot;
import java.util.Arrays;
//#endif

public class LivingEntityVer {


    // FIXME this should be possible with mappings, right?
    @Pattern
    private static Iterable<ItemStack> getItemsEquipped(LivingEntity entity) {
        if (entity == null) {
            return java.util.Collections.emptyList();
        }
        
        //#if MC >= 12111
        Iterable<ItemStack> handItems = Arrays.asList(
            entity.getEquippedStack(EquipmentSlot.MAINHAND),
            entity.getEquippedStack(EquipmentSlot.OFFHAND)
        );
        Iterable<ItemStack> armorItems = Arrays.asList(
            entity.getEquippedStack(EquipmentSlot.HEAD),
            entity.getEquippedStack(EquipmentSlot.CHEST),
            entity.getEquippedStack(EquipmentSlot.LEGS),
            entity.getEquippedStack(EquipmentSlot.FEET)
        );
        return Iterables.concat(handItems, armorItems);
        //#elseif MC >= 12005
        //$$ return entity.getEquippedItems();
        //#else
        //$$ return entity.getItemsEquipped();
        //#endif
    }

    @Pattern
    private static boolean isSuitableFor(Item item, BlockState state) {
        if (item == null) {
            return false;
        }
        if (state == null) {
            return false;
        }
        
        //#if MC >= 12005
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return item.isSuitableFor(state);
        //#endif
    }

}
