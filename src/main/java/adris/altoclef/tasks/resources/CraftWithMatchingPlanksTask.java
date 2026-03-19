package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.function.Function;

public class CraftWithMatchingPlanksTask extends CraftWithMatchingMaterialsTask {

    private final ItemTarget _visualTarget;
    private final Function<ItemHelper.WoodItems, Item> _getTargetItem;

    public CraftWithMatchingPlanksTask(Item[] validTargets, Function<ItemHelper.WoodItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask, int count) {
        super(new ItemTarget(validTargets, count), recipe, sameMask);
        _getTargetItem = getTargetItem;
        _visualTarget = new ItemTarget(validTargets, count);
    }


    @Override
    protected int getExpectedTotalCountOfSameItem(AltoClef mod, Item sameItem) {
        int plankCount = mod.getItemStorage().getItemCount(sameItem);
        Item logItem = ItemHelper.planksToLog(sameItem);
        int logCount = mod.getItemStorage().getItemCount(logItem);
        int total = plankCount + (logCount * 4);
        return total;
    }

    @Override
    protected Task getSpecificSameResourceTask(AltoClef mod, Item[] toGet) {
        for (Item plankToGet : toGet) {
            Item log = ItemHelper.planksToLog(plankToGet);
            int logCount = mod.getItemStorage().getItemCount(log);
            if (logCount >= 1) {
                return TaskCatalogue.getItemTask(plankToGet, 1);
            }
        }
        return null;
    }

    @Override
    protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
        for (ItemHelper.WoodItems woodItems : ItemHelper.getWoodItems()) {
            if (woodItems.planks == majority) {
                Item targetItem = _getTargetItem.apply(woodItems);
                return targetItem;
            }
        }
        return null;
    }


    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftWithMatchingPlanksTask task) {
            return task._visualTarget.equals(_visualTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Crafting: " + _visualTarget;
    }


    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

}
