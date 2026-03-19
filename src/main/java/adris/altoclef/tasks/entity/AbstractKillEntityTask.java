package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//#if MC < 12111
//$$ import net.minecraft.item.SwordItem;
//#else
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
//#endif
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.tag.ItemTags;

import java.util.List;

/**
 * Attacks an entity, but the target entity must be specified.
 */
public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {
    private static final double OTHER_FORCE_FIELD_RANGE = 2;

    // Not the "striking" distance, but the "ok we're close enough, lower our guard for other mobs and focus on this one" range.
    private static final double CONSIDER_COMBAT_RANGE = 10;

    protected AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    protected AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    protected AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    private static float getAttackDamage(ItemStack stack) {
        //#if MC >= 12111
        AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        return (float) modifiers.applyOperations(EntityAttributes.ATTACK_DAMAGE, 0.0, EquipmentSlot.MAINHAND);
        //#else
        //$$ // This method is not used for MC < 12111, so return 0 (should never be called)
        //$$ return 0;
        //#endif
    }

    public static Item bestWeapon(AltoClef mod) {
        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);

        Item bestItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
        float bestDamage = Float.NEGATIVE_INFINITY;

        ItemStack bestItemStack = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
        //#if MC < 12111
        //$$ if (bestItem instanceof SwordItem handToolItem) {
        //$$     bestDamage = handToolItem.getMaterial().getAttackDamage();
        //$$ }
        //#else
        if (bestItemStack.isIn(ItemTags.SWORDS)) {
            bestDamage = getAttackDamage(bestItemStack);
        }
        //#endif

        for (ItemStack invStack : invStacks) {
            //#if MC < 12111
            //$$ if (!(invStack.getItem() instanceof SwordItem item)) continue;
            //$$ float itemDamage = item.getMaterial().getAttackDamage();
            //#else
            if (!invStack.isIn(ItemTags.SWORDS)) continue;
            float itemDamage = getAttackDamage(invStack);
            //#endif

            if (itemDamage > bestDamage) {
                bestItem = invStack.getItem();
                bestDamage = itemDamage;
            }
        }

        return bestItem;
    }

    public static boolean equipWeapon(AltoClef mod) {
        Item bestWeapon = bestWeapon(mod);
        Item equipedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
        if (bestWeapon != null && bestWeapon != equipedWeapon) {
            mod.getSlotHandler().forceEquipItem(bestWeapon);
            return true;
        }
        return false;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        // Equip weapon
        if (!equipWeapon(mod)) {
            float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
            boolean onGround = mod.getPlayer().isOnGround();
            boolean falling = mod.getPlayer().getVelocity().getY() < 0;
            boolean inWater = mod.getPlayer().isTouchingWater();
            if (hitProg >= 1 && (onGround || falling || inWater)) {
                LookHelper.lookAt(mod, entity.getEyePos());
                mod.getControllerExtras().attack(entity);
            }
        }
        return null;
    }
}
