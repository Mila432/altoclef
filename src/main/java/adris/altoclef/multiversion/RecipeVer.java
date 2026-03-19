package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

//#if MC >= 12111
import java.util.Collections;
import net.minecraft.recipe.input.RecipeInput;
//#endif

public class RecipeVer {

    public static ItemStack getOutput(Recipe<?> recipe, World world) {
        // Entry point logging - this is a critical state change
        
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        if (world == null) {
            return ItemStack.EMPTY;
        }
        
        //#if MC >= 12111
        // In 1.21.11+, getResult(DynamicRegistryManager) was removed.
        // We must create a dummy RecipeInput matching the expected subclass to avoid crashes.
        ItemStack result = getOutputImpl(recipe, world);
        if (result == null) {
            return ItemStack.EMPTY;
        }
        return result;
        //#elseif MC >= 11904
        //$$ Debug.logMessage("RecipeVer.getOutput - MC >= 11904 detected, using recipe.getResult(world.getRegistryManager())");
        //$$ ItemStack result = recipe.getResult(world.getRegistryManager());
        //$$ Debug.logMessage("RecipeVer.getOutput - Result: " + result);
        //$$ return result;
        //#else
        //$$ Debug.logMessage("RecipeVer.getOutput - Legacy MC detected, using recipe.getOutput()");
        //$$ ItemStack result = recipe.getOutput();
        //$$ Debug.logMessage("RecipeVer.getOutput - Result: " + result);
        //$$ return result;
        //#endif
    }

    //#if MC >= 12111
    @SuppressWarnings("unchecked")
    private static <T extends RecipeInput> ItemStack getOutputImpl(Recipe<T> recipe, World world) {
        
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        if (world == null) {
            return ItemStack.EMPTY;
        }
        
        RecipeInput input;
        String recipeType = recipe.getClass().getSimpleName();
        
        if (recipe instanceof net.minecraft.recipe.CraftingRecipe) {
            // CraftingRecipeInput.EMPTY has size 0x0, which crashes ShapedRecipes when they check dimensions.
            // We use a 3x3 grid of empty item stacks instead.
            input = net.minecraft.recipe.input.CraftingRecipeInput.create(3, 3, Collections.nCopies(9, ItemStack.EMPTY));
        } else if (recipe instanceof net.minecraft.recipe.SingleStackRecipe) {
            input = new net.minecraft.recipe.input.SingleStackRecipeInput(ItemStack.EMPTY);
        } else if (recipe instanceof net.minecraft.recipe.SmithingRecipe) {
            input = new net.minecraft.recipe.input.SmithingRecipeInput(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        } else {
            // Generic fallback - create an empty RecipeInput
            input = new RecipeInput() {
                @Override
                public ItemStack getStackInSlot(int slot) {
                    // High-frequency method - don't log here
                    return ItemStack.EMPTY;
                }

                @Override
                public int size() {
                    // High-frequency method - don't log here
                    return 0;
                }
            };
        }

        try {
            ItemStack result = recipe.craft((T) input, world.getRegistryManager());
            if (result == null) {
                return ItemStack.EMPTY;
            }
            if (result.isEmpty()) {
            }
            return result;
        } catch (Exception e) {
            // Some special recipes might throw an exception if inputs are empty.
            return ItemStack.EMPTY;
        }
    }
    //#endif
}
