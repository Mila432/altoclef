package adris.altoclef.commands.random;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug; // Added import for Debug
import adris.altoclef.trackers.BlockScanner;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.StringArg;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.lang.reflect.Field;

public class ScanCommand extends Command {

    public ScanCommand() throws CommandException {
        super("scan", "Locates nearest block",
                new StringArg("block", "DIRT")
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String blockStr = parser.get(String.class);

        // Critical: Log the search parameter to track potential invalid inputs

        Field[] declaredFields = Blocks.class.getDeclaredFields();
        Block block = null;

        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                if (field.getName().equalsIgnoreCase(blockStr)) {
                    block = (Block) field.get(Blocks.class);
                    // Critical: Log when a block is successfully found via reflection
                    break; // Added break to stop after finding the first match (unchanged logic)
                }
            } catch (IllegalAccessException e) {
                // Critical: Reflection access failure - potential runtime bug
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
        }

        if (block == null) {
            // Critical: Block not found - user input error or missing block
            mod.logWarning("Block named: " + blockStr + " not found :(");
            return;
        }

        BlockScanner blockScanner = mod.getBlockScanner();
        
        // Critical: Check for null BlockScanner - system integrity issue
        if (blockScanner == null) {
            return;
        }

        //#if MC >= 12111
        // Critical: Log the actual scan result for debugging visibility
        var nearestBlock = blockScanner.getNearestBlock(block, mod.getPlayer().getEntityPos());
        if (nearestBlock == null) {
        } else {
        }
        mod.log(nearestBlock + "");
        //#else
        //$$ var nearestBlock = blockScanner.getNearestBlock(block, mod.getPlayer().getPos());
        //$$ if (nearestBlock == null) {
        //$$     Debug.logWarning("[ScanCommand] No nearest block found for: " + block.getName().getString());
        //$$ } else {
        //$$     Debug.logMessage("[ScanCommand] Nearest " + block.getName().getString() + " found at: " + nearestBlock);
        //$$ }
        //$$ mod.log(nearestBlock + "");
        //#endif
    }

}
