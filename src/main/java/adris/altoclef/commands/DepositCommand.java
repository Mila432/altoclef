package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug; // Added import
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.ItemTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class DepositCommand extends Command {
    public DepositCommand() {
        super("deposit", "Deposit ALL of our items",
                new ListArg<>(new ItemTargetArg("itemStack"), "Item list", null, false)
        );
    }

    public static ItemTarget[] getAllNonEquippedOrToolItemsAsTarget(AltoClef mod) {
        
        ItemTarget[] result = StorageHelper.getAllInventoryItemsAsTargets(slot -> {
            // Ignore armor
            if (ArrayUtils.contains(PlayerSlot.ARMOR_SLOTS, slot))
                return false;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // Ignore tools
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                //#if MC >= 12111
                boolean isTool = stack.contains(net.minecraft.component.DataComponentTypes.TOOL);
                if (isTool) {
                }
                return !isTool;
                //#else
                //$$ boolean isTool = item instanceof net.minecraft.item.ToolItem;
                //$$ if (isTool) {
                //$$     Debug.logMessage("DepositCommand: Skipping tool item: " + item.getTranslationKey() + " in slot " + slot);
                //$$ }
                //$$ return !isTool;
                //#endif
            }
            return false;
        });
        
        return result;
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        
        List<ItemTarget> itemList = parser.get(List.class);

        ItemTarget[] items;
        if (itemList == null) {
            items = getAllNonEquippedOrToolItemsAsTarget(mod);
        } else {
            items = itemList.toArray(ItemTarget[]::new);
        }

        if (items.length == 0) {
        }

        mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
    }
}
