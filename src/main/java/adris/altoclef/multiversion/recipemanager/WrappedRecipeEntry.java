package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

//#if MC >= 12111
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
//#endif

public record WrappedRecipeEntry(Identifier id, Recipe<?> value
//#if MC >= 12111
, NetworkRecipeId networkRecipeId
//#endif
) {

    //#if MC >= 12111
    public RecipeEntry<?> asRecipe() {
        if (id == null) {
        }
        if (value == null) {
        }
        
        RecipeEntry<?> entry = new RecipeEntry<>(RegistryKey.of(RegistryKeys.RECIPE, id), value);
        
        return entry;
    }
    //#elseif MC>12001
    //$$ public net.minecraft.recipe.RecipeEntry<?> asRecipe() {
    //$$     if (id == null) {
    //$$         Debug.logError("WrappedRecipeEntry.asRecipe (1.20.2-1.21.0): id is NULL, cannot create RecipeEntry. value=" + value);
    //$$     }
    //$$     if (value == null) {
    //$$         Debug.logError("WrappedRecipeEntry.asRecipe (1.20.2-1.21.0): value (Recipe<?>) is NULL for id=" + id);
    //$$     }
    //$$     Debug.logMessage("WrappedRecipeEntry.asRecipe (1.20.2-1.21.0): Converting to RecipeEntry for id=" + id + ", recipeType=" + value.getType());
    //$$     net.minecraft.recipe.RecipeEntry<?> entry = new net.minecraft.recipe.RecipeEntry<>(id, value);
    //$$     Debug.logMessage("WrappedRecipeEntry.asRecipe (1.20.2-1.21.0): Successfully created RecipeEntry");
    //$$     return entry;
    //$$ }
    //#else
    //$$ public Recipe<?> asRecipe(){
    //$$     if (id == null) {
    //$$         Debug.logError("WrappedRecipeEntry.asRecipe (legacy): id is NULL, cannot identify recipe. value=" + value);
    //$$     }
    //$$     if (value == null) {
    //$$         Debug.logError("WrappedRecipeEntry.asRecipe (legacy): value (Recipe<?>) is NULL for id=" + id);
    //$$     }
    //$$     Debug.logMessage("WrappedRecipeEntry.asRecipe (legacy): Returning raw recipe for id=" + id + ", recipeType=" + value.getType());
    //$$     return value;
    //$$ }
    //#endif

}
