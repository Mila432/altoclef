package adris.altoclef.trackers.storage;

import adris.altoclef.util.slots.ChestSlot;
import adris.altoclef.util.slots.FurnaceSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.*;
import org.apache.commons.lang3.NotImplementedException;

public enum ContainerType {
    CHEST, ENDER_CHEST, SHULKER, FURNACE, BREWING, MISC, EMPTY;

    public static ContainerType getFromBlock(Block block) {
        if (block instanceof ChestBlock) {
            return CHEST;
        }
        if (block instanceof AbstractFurnaceBlock) {
            return FURNACE;
        }
        if (block.equals(Blocks.ENDER_CHEST)) {
            return ENDER_CHEST;
        }
        if (block instanceof ShulkerBoxBlock) {
            return SHULKER;
        }
        if (block instanceof BrewingStandBlock) {
            return BREWING;
        }
        if (block instanceof BarrelBlock || block instanceof DispenserBlock || block instanceof HopperBlock) {
            return MISC;
        }
        return EMPTY;
    }

    public static boolean screenHandlerMatches(ContainerType type, ScreenHandler handler) {
        boolean result;
        switch (type) {
            case CHEST, ENDER_CHEST -> {
                result = handler instanceof GenericContainerScreenHandler;
            }
            case SHULKER -> {
                result = handler instanceof ShulkerBoxScreenHandler;
            }
            case FURNACE -> {
                result = handler instanceof AbstractFurnaceScreenHandler;
            }
            case BREWING -> {
                result = handler instanceof BrewingStandScreenHandler;
            }
            case MISC -> {
                result = handler instanceof Generic3x3ContainerScreenHandler || handler instanceof GenericContainerScreenHandler;
            }
            case EMPTY -> {
                return false;
            }
            default -> throw new NotImplementedException("Missed this chest type: " + type);
        }
        if (!result) {
        }
        return result;
    }

    public static boolean screenHandlerMatches(ContainerType type) {
        if (MinecraftClient.getInstance().player != null) {
            ScreenHandler h = MinecraftClient.getInstance().player.currentScreenHandler;
            if (h != null)
                return screenHandlerMatches(type, h);
            else
        } else {
        }
        return false;
    }

    public static boolean screenHandlerMatchesAny() {
        boolean matchesChest = screenHandlerMatches(CHEST);
        boolean matchesShulker = screenHandlerMatches(SHULKER);
        boolean matchesFurnace = screenHandlerMatches(FURNACE);
        boolean anyMatch = matchesChest || matchesShulker || matchesFurnace;
        if (!anyMatch) {
        }
        return anyMatch;
    }

    public static boolean slotTypeMatches(ContainerType type, Slot slot) {
        boolean result;
        switch (type) {
            case CHEST, ENDER_CHEST, SHULKER -> {
                result = slot instanceof ChestSlot;
            }
            case FURNACE -> {
                result = slot instanceof FurnaceSlot;
            }
            case BREWING -> throw new NotImplementedException("Brewing slots not implemented yet.");
            case MISC -> {
                result = true;
            }
            default -> throw new NotImplementedException("Missed this chest type: " + type);
        }
        if (!result) {
        }
        return result;
    }
}
