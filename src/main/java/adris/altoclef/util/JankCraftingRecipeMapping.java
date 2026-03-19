package adris.altoclef.util;

import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;
import adris.altoclef.multiversion.RecipeVer;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;

import java.util.*;
import java.util.stream.Collectors;

public class JankCraftingRecipeMapping {

    private static final Map<Item, List<WrappedRecipeEntry>> recipeMapping = new HashMap<>();
    private static boolean cached = false;

    private static void reloadRecipeMapping() {
        if (cached) {
            return;
        }
        recipeMapping.clear();
        if (MinecraftClient.getInstance().player != null) {
            World world = 
                //#if MC >= 12111
                MinecraftClient.getInstance().player.getEntityWorld();
                //#else
                //$$ MinecraftClient.getInstance().player.getWorld();
                //#endif
            RecipeManagerWrapper recipes = null;
            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.networkHandler != null) {
                recipes = RecipeManagerWrapper.of(MinecraftClient.getInstance().player.networkHandler.getRecipeManager());
            }

            if (recipes != null) {
                for (WrappedRecipeEntry recipe : recipes.values()) {
                    assert world != null;
                    Recipe<?> value = recipe.value();
                    //#if MC >= 12111
                    if (!(value instanceof net.minecraft.recipe.CraftingRecipe)) {
                        continue;
                    }
                    //#endif
                    Item output = RecipeVer.getOutput(value, world).getItem();
                    if (output == null) {
                        continue;
                    }
                    recipeMapping.computeIfAbsent(output, k -> new ArrayList<>()).add(recipe);
                }
                cached = true;
            } else {
            }
        } else {
        }
    }

    public static Optional<WrappedRecipeEntry> getMinecraftMappedRecipe(CraftingRecipe recipe, Item output) {
        reloadRecipeMapping();

        if (recipeMapping.containsKey(output)) {
            for (WrappedRecipeEntry checkRecipe : recipeMapping.get(output)) {
                List<ItemTarget> toSatisfy = Arrays.stream(recipe.getSlots())
                        .filter(itemTarget -> itemTarget != null && !itemTarget.isEmpty())
                        .collect(Collectors.toList());

                //#if MC >= 12111
                List<Ingredient> ingredients;
                if (checkRecipe.value() instanceof net.minecraft.recipe.CraftingRecipe craftingRecipe) {
                    ingredients = craftingRecipe.getIngredientPlacement().getIngredients();
                } else if (checkRecipe.value() instanceof net.minecraft.recipe.AbstractCookingRecipe cookingRecipe) {
                    ingredients = cookingRecipe.getIngredientPlacement().getIngredients();
                } else if (checkRecipe.value() instanceof net.minecraft.recipe.SingleStackRecipe singleStackRecipe) {
                    ingredients = singleStackRecipe.getIngredientPlacement().getIngredients();
                } else {
                    ingredients = new ArrayList<>();
                }
                //#else
                //$$ List<Ingredient> ingredients = checkRecipe.value().getIngredients();
                //#endif

                if (!ingredients.isEmpty()) {
                    for (Ingredient ingredient : ingredients) {
                        if (ingredient.isEmpty()) {
                            continue;
                        }

                        outer:
                        for (int i = 0; i < toSatisfy.size(); ++i) {
                            ItemTarget target = toSatisfy.get(i);

                            //#if MC >= 12111
                            for (net.minecraft.registry.entry.RegistryEntry<Item> itemEntry : ingredient.getMatchingItems().toList()) {
                                 if (target.matches(itemEntry.value())) {
                                     toSatisfy.remove(i);
                                     break outer;
                                 }
                             }
                            //#else
                            //$$ for (net.minecraft.item.ItemStack stack : ingredient.getMatchingStacks()) {
                            //$$     if (target.matches(stack.getItem())) {
                            //$$         toSatisfy.remove(i);
                            //$$         break outer;
                            //$$     }
                            //$$ }
                            //#endif
                        }
                    }
                }

                if (toSatisfy.isEmpty()) {
                    return Optional.of(checkRecipe);
                } else {
                }
            }
        } else {
        }

        return Optional.empty();
    }
}
