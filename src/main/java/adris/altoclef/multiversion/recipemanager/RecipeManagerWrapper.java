package adris.altoclef.multiversion.recipemanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

//#if MC >= 12111
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.item.ItemStack;
import java.util.Optional;
//#endif

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManagerWrapper {

    private final RecipeManager recipeManager;

    public static RecipeManagerWrapper of(RecipeManager recipeManager) {
        if (recipeManager == null) {
            return null;
        }

        return new RecipeManagerWrapper(recipeManager);
    }

    private RecipeManagerWrapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    //#if MC >= 12111
    public Collection<WrappedRecipeEntry> values() {
        
        // If we have a ServerRecipeManager (e.g. from an integrated server's data), it has the real recipes.
        if (recipeManager instanceof net.minecraft.recipe.ServerRecipeManager serverRecipeManager) {
            Collection<WrappedRecipeEntry> result = serverRecipeManager.values().stream()
                .map(r -> new WrappedRecipeEntry(r.id().getValue(), r.value(), new net.minecraft.recipe.NetworkRecipeId(-1)))
                .collect(Collectors.toSet());
            return result;
        }

        // Try getting recipes directly from the integrated server if playing singleplayer
        net.minecraft.server.MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            Collection<WrappedRecipeEntry> result = server.getRecipeManager().values().stream()
                .map(r -> new WrappedRecipeEntry(r.id().getValue(), r.value(), new net.minecraft.recipe.NetworkRecipeId(-1)))
                .collect(Collectors.toSet());
            return result;
        }

        // In 1.21.2+ multiplayer, the client RecipeManager does NOT contain full recipes.
        // The server only sends visual displays in the ClientRecipeBook. We must reconstruct dummy Recipe
        // objects from these displays so AltoClef knows how to craft items manually.
        List<WrappedRecipeEntry> reconstructed = new ArrayList<>();
        net.minecraft.client.network.ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        
        if (networkHandler != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            net.minecraft.client.recipebook.ClientRecipeBook book = MinecraftClient.getInstance().player.getRecipeBook();
            if (book == null) {
                return reconstructed;
            }
            
            ContextParameterMap context = SlotDisplayContexts.createParameters(MinecraftClient.getInstance().world);
            if (context == null) {
                return reconstructed;
            }

            int count = 0;
            int nullCount = 0;
            int emptyResultCount = 0;
            for (net.minecraft.client.gui.screen.recipebook.RecipeResultCollection collection : book.getOrderedResults()) {
                if (collection == null) {
                    continue;
                }
                for (RecipeDisplayEntry entry : collection.getAllRecipes()) {
                    if (entry == null) {
                        continue;
                    }
                    Recipe<?> recipe = reconstructRecipe(entry, context);
                    if (recipe != null) {
                        count++;
                        reconstructed.add(new WrappedRecipeEntry(Identifier.of("altoclef", "reconstructed_" + entry.id().index()), recipe, entry.id()));
                    } else {
                        nullCount++;
                    }
                }
            }
            if (nullCount > 0) {
            }
        } else {
        }
        return reconstructed;
    }

    private Recipe<?> reconstructRecipe(RecipeDisplayEntry entry, ContextParameterMap context) {
        RecipeDisplay display = entry.display();
        
        if (display == null) {
            return null;
        }

        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            ItemStack resultStack = shaped.result().getFirst(context);
            if (resultStack.isEmpty()) {
                return null;
            }

            List<Optional<Ingredient>> grid = new ArrayList<>();
            for (SlotDisplay slotDisplay : shaped.ingredients()) {
                List<ItemStack> stacks = slotDisplay.getStacks(context);
                if (stacks.isEmpty()) {
                    grid.add(Optional.empty());
                } else {
                    grid.add(Optional.of(Ingredient.ofItems(stacks.stream().map(ItemStack::getItem))));
                }
            }

            // Validate recipe structure
            if (grid.size() != shaped.width() * shaped.height()) {
                return null;
            }

            RawShapedRecipe raw = new RawShapedRecipe(shaped.width(), shaped.height(), grid, Optional.empty());
            return new ShapedRecipe("reconstructed", CraftingRecipeCategory.MISC, raw, resultStack);
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            ItemStack resultStack = shapeless.result().getFirst(context);
            if (resultStack.isEmpty()) {
                return null;
            }

            List<Ingredient> ingredients = new ArrayList<>();
            for (SlotDisplay slotDisplay : shapeless.ingredients()) {
                List<ItemStack> stacks = slotDisplay.getStacks(context);
                if (!stacks.isEmpty()) {
                    ingredients.add(Ingredient.ofItems(stacks.stream().map(ItemStack::getItem)));
                }
            }

            if (ingredients.isEmpty()) {
                return null;
            }

            return new ShapelessRecipe("reconstructed", CraftingRecipeCategory.MISC, resultStack, ingredients);
        }
        
        return null;
    }
    //#elseif MC>12001
    //$$ public Collection<WrappedRecipeEntry> values() {
    //$$     return recipeManager.values().stream().map(r -> new WrappedRecipeEntry(r.id(),r.value())).collect(Collectors.toSet());
    //$$ }
    //#else
    //$$ public Collection<WrappedRecipeEntry> values() {
    //$$     List<WrappedRecipeEntry> result = new ArrayList<>();
    //$$     for (Identifier id : recipeManager.keys().toList()) {
    //$$         result.add(new WrappedRecipeEntry(id, recipeManager.get(id).get()));
    //$$     }
    //$$
    //$$     return result;
    //$$ }
    //#endif

}
