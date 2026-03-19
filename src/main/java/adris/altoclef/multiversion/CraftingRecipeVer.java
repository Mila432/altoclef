package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;

//#if MC >= 12111
import net.minecraft.recipe.input.CraftingRecipeInput;
//#endif

public class CraftingRecipeVer {

    @Pattern
    private static ItemStack getOutput(CraftingRecipe craftingRecipe) {
        // Only log if we receive null input - this is a critical error state
        if (craftingRecipe == null) {
            return null;
        }
        
        // Only log if we get unexpected null output - this indicates a recipe processing bug
        //#if MC >= 12111
        ItemStack result = craftingRecipe.craft(CraftingRecipeInput.EMPTY, null);
        if (result == null) {
        }
        return result;
        //#elseif MC >= 11904
        //$$ ItemStack result = craftingRecipe.getResult(null);
        //$$ if (result == null) {
        //$$     Debug.logError("CraftingRecipeVer.getOutput() got null result from valid recipe: " + craftingRecipe.getClass().getSimpleName());
        //$$ }
        //$$ return result;
        //#else
        //$$ ItemStack result = craftingRecipe.getOutput();
        //$$ if (result == null) {
        //$$     Debug.logError("CraftingRecipeVer.getOutput() got null result from valid recipe: " + craftingRecipe.getClass().getSimpleName());
        //$$ }
        //$$ return result;
        //#endif
    }

}
