package adris.altoclef.util;

import adris.altoclef.Debug;  // Added import
import net.minecraft.item.Item;

import java.util.Objects;

public class RecipeTarget {

    private final CraftingRecipe recipe;
    private final Item item;
    private final int targetCount;

    public RecipeTarget(Item item, int targetCount, CraftingRecipe recipe) {
        if (item == null) {
        }
        if (recipe == null) {
        }
        if (targetCount <= 0) {
        }
        this.item = item;
        this.targetCount = targetCount;
        this.recipe = recipe;
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public Item getOutputItem() {
        return item;
    }

    public int getTargetCount() {
        return targetCount;
    }

    @Override
    public String toString() {
        if (targetCount == 1)
            return "Recipe{"+item+"}";

        return "Recipe{" +
                item + " x " + targetCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecipeTarget that = (RecipeTarget) o;
        boolean targetCountMatch = targetCount == that.targetCount;
        boolean recipeMatch = (recipe != null ? recipe.equals(that.recipe) : that.recipe == null);
        boolean itemMatch = Objects.equals(item, that.item);
        boolean result = targetCountMatch && recipeMatch && itemMatch;
        if (!result) {
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, item);
    }
}
