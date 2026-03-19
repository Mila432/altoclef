package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
//#if MC >= 12100
import net.minecraft.recipe.input.CraftingRecipeInput;
//#endif

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// TODO remove those ugly "ensureUpdate" statements, realistically we only need to update only upon joining a world
public class CraftingRecipeTracker extends Tracker{


    private final HashMap<Item, List<adris.altoclef.util.CraftingRecipe>> itemRecipeMap = new HashMap<>();
    private final HashMap<adris.altoclef.util.CraftingRecipe, ItemStack> recipeResultMap = new HashMap<>();

    private boolean shouldRebuild;

    public CraftingRecipeTracker(TrackerManager manager) {
        super(manager);
        shouldRebuild = true;
    }

    public List<adris.altoclef.util.CraftingRecipe> getRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            mod.logWarning("trying to access recipe for unknown item: "+item);
            return null;
        }

        return itemRecipeMap.get(item);
    }

    public adris.altoclef.util.CraftingRecipe getFirstRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            mod.logWarning("trying to access recipe for unknown item: "+item);
            return null;
        }

        return itemRecipeMap.get(item).get(0);
    }

    public List<RecipeTarget> getRecipeTarget(Item item, int targetCount) {
        ensureUpdated();

        List<RecipeTarget> targets = new ArrayList<>();
        for (adris.altoclef.util.CraftingRecipe recipe : getRecipeForItem(item)) {
            targets.add(new RecipeTarget(item, targetCount, recipe));
        }

        return targets;
    }

    public RecipeTarget getFirstRecipeTarget(Item item, int targetCount) {
        ensureUpdated();
        return new RecipeTarget(item, targetCount, getFirstRecipeForItem(item));
    }

    public boolean hasRecipeForItem(Item item) {
        ensureUpdated();
        return itemRecipeMap.containsKey(item);
    }

    public ItemStack getRecipeResult(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();

        if (!hasRecipe(recipe)) {
            mod.logWarning("Trying to get result for unknown recipe: "+recipe);
            return null;
        }
        ItemStack result = recipeResultMap.get(recipe);

        return new ItemStack(result.getItem(), result.getCount());
    }

    public boolean hasRecipe(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();
        return recipeResultMap.containsKey(recipe);
    }


    @Override
    protected void updateState() {
        if (!shouldRebuild) return;

        // rebuild once we are in game
        if (!AltoClef.inGame()) return;

        ClientPlayNetworkHandler networkHandler =  MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return;

        RecipeManagerWrapper recipeManager = RecipeManagerWrapper.of(networkHandler.getRecipeManager());

        int processedRecipes = 0;

        for (WrappedRecipeEntry recipe : recipeManager.values()) {
            if (!(recipe.value() instanceof net.minecraft.recipe.CraftingRecipe craftingRecipe)) continue;

            // not implemented for now because it isn't needed (I hope xd)
            if (craftingRecipe instanceof SpecialCraftingRecipe) continue;

            ItemStack resultStack;
            List<Ingredient> ingredients;

            if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
                // Get result from the recipe
                //#if MC >= 12100
                resultStack = craftingRecipe.craft(CraftingRecipeInput.EMPTY, null);
                //#elseif MC >= 12002
                //$$ resultStack = shapedRecipe.getResult(null).copy();
                //#elseif MC >= 11903
                //$$ resultStack = shapedRecipe.getOutput(null).copy();
                //#else
                //$$ resultStack = shapedRecipe.getOutput().copy();
                //#endif

                // Get ingredients
                //#if MC >= 12111
                // In 1.21.11, ShapedRecipe.getIngredients() returns List<Optional<Ingredient>>
                // We need to convert it to List<Ingredient> by unwrapping optionals
                ingredients = new ArrayList<>();
                for (java.util.Optional<Ingredient> opt : shapedRecipe.getIngredients()) {
                    ingredients.add(opt.orElse(null));
                }
                //#else
                //$$ ingredients = shapedRecipe.getIngredients();
                //#endif
            } else if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
                // Get result from the recipe
                //#if MC >= 12100
                resultStack = craftingRecipe.craft(CraftingRecipeInput.EMPTY, null);
                //#elseif MC >= 12002
                //$$ resultStack = shapelessRecipe.getResult(null).copy();
                //#elseif MC >= 11903
                //$$ resultStack = shapelessRecipe.getOutput(null).copy();
                //#else
                //$$ resultStack = shapelessRecipe.getOutput().copy();
                //#endif

                // Get ingredients
                //#if MC >= 12111
                // In 1.21.11, ShapelessRecipe doesn't have getIngredients() method
                // We need to get ingredients via getIngredientPlacement()
                ingredients = shapelessRecipe.getIngredientPlacement().getIngredients();
                //#else
                //$$ ingredients = shapelessRecipe.getIngredients();
                //#endif
            } else {
                // Skip other recipes
                continue;
            }

            ItemStack result = new ItemStack(resultStack.getItem(), resultStack.getCount());

            Item[][] altoclefRecipeItems = getShapedCraftingRecipe(ingredients);

            adris.altoclef.util.CraftingRecipe altoclefRecipe = adris.altoclef.util.CraftingRecipe.newShapedRecipe(altoclefRecipeItems, result.getCount());

            if (itemRecipeMap.containsKey(result.getItem())) {
                itemRecipeMap.get(result.getItem()).add(altoclefRecipe);
            } else {
                List<adris.altoclef.util.CraftingRecipe> recipes = new ArrayList<>();
                recipes.add(altoclefRecipe);

                itemRecipeMap.put(result.getItem(), recipes);
            }

            recipeResultMap.put(altoclefRecipe, result);
            processedRecipes++;
        }

        itemRecipeMap.replaceAll((k,v) -> Collections.unmodifiableList(v));

        shouldRebuild = false;
    }

    // TODO adjust for small recipes
    // it is always shaped, but that doesn't matter for shapeless
    // the second dimension of the array is for different types of items (eq. logs)
    private static Item[][] getShapedCraftingRecipe(List<Ingredient> ingredients) {
        Item[][] result = new Item[9][];
        int x = 0;

        for (Ingredient ingredient : ingredients) {
            if (ingredient == null) {
                result[x] = null;
                x++;
                continue;
            }

            Item[] items;
            //#if MC >= 12111
            items = ingredient.getMatchingItems().map(net.minecraft.registry.entry.RegistryEntry::value).toArray(Item[]::new);
            //#else
            //$$ items = Arrays.stream(ingredient.getMatchingStacks()).map(ItemStack::getItem).toArray(Item[]::new);
            //#endif

            if (items.length != 0) {
                // FIXME this is so stupid, but TaskCatalogue is kinda setup this way, so it would require a rewrite to allow for multiple resource :')
                result[x] = new Item[]{items[0]};
            } else {
                result[x] = null;
            }

            x++;
        }


        return result;
    }

    @Override
    protected void reset() {
       shouldRebuild = true;
       itemRecipeMap.clear();
       recipeResultMap.clear();
    }

    @Override
    protected boolean isDirty() {
        return shouldRebuild;
    }
}