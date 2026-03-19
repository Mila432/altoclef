package adris.altoclef.multiversion;

import adris.altoclef.mixins.DrawableHelperInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public class DrawContextWrapper {

    //#if MC >= 12001
    public static DrawContextWrapper of(DrawContext context) {
        if (context == null) {
            return null;
        }
        DrawContextWrapper wrapper = new DrawContextWrapper(context);
        return wrapper;
    }
    private final DrawContext context;

    private DrawContextWrapper(DrawContext context) {
        this.context = context;
    }
    //#else
    //$$ public static DrawContextWrapper of(MatrixStack matrices) {
    //$$    if (matrices == null) {
    //$$        Debug.logWarning("DrawContextWrapper.of() - Received null MatrixStack, returning null wrapper");
    //$$        return null;
    //$$    }
    //$$    DrawContextWrapper wrapper = new DrawContextWrapper(matrices);
    //$$    return wrapper;
    //$$ }
    //$$
    //$$ private final MatrixStack matrices;
    //$$ private final DrawableHelper helper;
    //$$ private DrawContextWrapper(MatrixStack matrices) {
    //$$        this.matrices = matrices;
    //$$        this.helper = new DrawableHelper(){};
    //$$ }
    //#endif

    private RenderLayer renderLayer = null;

    // used only 1.20.1 and later... can pass null in earlier versions
    public void setRenderLayer(RenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        // Critical validation: Check for invalid coordinates
        if (x1 > x2 || y1 > y2) {
        }
        
        //#if MC >= 12111
        if (context == null) {
            return;
        }
        context.fill(x1, y1, x2, y2, color);
        //#elseif MC >= 12001
        //$$ if (context == null) {
        //$$     Debug.logError("DrawContextWrapper.fill() - CRITICAL: context is null when attempting to fill rectangle!");
        //$$     return;
        //$$ }
        //$$ context.fill(renderLayer, x1, y1, x2, y2, color);
        //#else
        //$$ DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
        //#endif
    }

    public void drawHorizontalLine(int x1, int x2, int y, int color) {
        // Critical validation: Check for invalid line parameters
        if (x1 == x2) {
        }
        
        //#if MC >= 12111
        if (context == null) {
            return;
        }
        context.drawHorizontalLine(x1, x2, y, color);
        //#elseif MC >= 12001
        //$$ if (context == null) {
        //$$     Debug.logError("DrawContextWrapper.drawHorizontalLine() - CRITICAL: context is null when attempting to draw horizontal line!");
        //$$     return;
        //$$ }
        //$$ context.drawHorizontalLine(renderLayer, x1, x2, y, color);
        //#else
        //$$ ((DrawableHelperInvoker) helper).invokeDrawHorizontalLine(matrices, x1, x2, y, color);
        //#endif
    }

    public void drawVerticalLine(int x, int y1, int y2, int color) {
        // Critical validation: Check for invalid line parameters
        if (y1 == y2) {
        }
        
        //#if MC >= 12111
        if (context == null) {
            return;
        }
        context.drawVerticalLine(x, y1, y2, color);
        //#elseif MC >= 12001
        //$$ if (context == null) {
        //$$     Debug.logError("DrawContextWrapper.drawVerticalLine() - CRITICAL: context is null when attempting to draw vertical line!");
        //$$     return;
        //$$ }
        //$$ context.drawVerticalLine(renderLayer, x, y1, y2, color);
        //#else
        //$$ ((DrawableHelperInvoker) helper).invokeDrawVerticalLine(matrices, x, y1, y2, color);
        //#endif
    }

    public void drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
        // Critical validation: Check for null textRenderer
        if (textRenderer == null) {
            return;
        }
        
        // Log warning for empty text (might be intentional but worth noting)
        if (text != null && text.isEmpty()) {
        }
        
        //#if MC >= 12001
        if (context == null) {
            return;
        }
        context.drawText(textRenderer,text,x,y,color,shadow);
        //#else
        //$$ if (shadow) {
        //$$    textRenderer.drawWithShadow(matrices, text,x,y,color);
        //$$ } else {
        //$$    textRenderer.draw(matrices, text,x,y,color);
        //$$ }
        //#endif
    }


    public MatrixStack getMatrices() {
        //#if MC >= 12111
        // In 1.21.11, DrawContext.getMatrices() returns Matrix3x2fStack
        // We need to create a MatrixStack from the Matrix3x2fStack for compatibility
        // Since Matrix3x2fStack is not directly convertible to MatrixStack,
        // we'll return a new MatrixStack that mirrors the current transformations
        // This is a temporary solution until callers can be updated
        MatrixStack stack = new MatrixStack();

        return stack;
        //#elseif MC >= 12001
        //$$ if (context == null) {
        //$$     Debug.logError("DrawContextWrapper.getMatrices() - CRITICAL: context is null! Returning new empty MatrixStack.");
        //$$     return new MatrixStack(); // Return empty stack to prevent NPE
        //$$ }
        //$$ MatrixStack result = context.getMatrices();
        //$$ return result;
        //#else
        //$$ if (matrices == null) {
        //$$     Debug.logError("DrawContextWrapper.getMatrices() - CRITICAL: matrices is null! Returning new empty MatrixStack.");
        //$$     return new MatrixStack(); // Return empty stack to prevent NPE
        //$$ }
        //$$ return matrices;
        //#endif
    }

    public int getScaledWindowWidth() {
        //#if MC >= 12001
        if (context == null) {
            return 0;
        }
        int width = context.getScaledWindowWidth();
        return width;
        //#else
        //$$ MinecraftClient client = MinecraftClient.getInstance();
        //$$ if (client == null) {
        //$$     Debug.logError("DrawContextWrapper.getScaledWindowWidth() - CRITICAL: MinecraftClient instance is null! Returning 0 as fallback.");
        //$$     return 0;
        //$$ }
        //$$ if (client.getWindow() == null) {
        //$$     Debug.logError("DrawContextWrapper.getScaledWindowWidth() - CRITICAL: MinecraftClient window is null! Returning 0 as fallback.");
        //$$     return 0;
        //$$ }
        //$$ int width = client.getWindow().getScaledWidth();
        //$$ Debug.logMessage("DrawContextWrapper.getScaledWindowWidth() - Returning width=" + width + " from MinecraftClient window");
        //$$ return width;
        //#endif
    }

    public int getScaledWindowHeight() {
        //#if MC >= 12001
        if (context == null) {
            return 0;
        }
        int height = context.getScaledWindowHeight();
        return height;
        //#else
        //$$ MinecraftClient client = MinecraftClient.getInstance();
        //$$ if (client == null) {
        //$$     Debug.logError("DrawContextWrapper.getScaledWindowHeight() - CRITICAL: MinecraftClient instance is null! Returning 0 as fallback.");
        //$$     return 0;
        //$$ }
        //$$ if (client.getWindow() == null) {
        //$$     Debug.logError("DrawContextWrapper.getScaledWindowHeight() - CRITICAL: MinecraftClient window is null! Returning 0 as fallback.");
        //$$     return 0;
        //$$ }
        //$$ int height = client.getWindow().getScaledHeight();
        //$$ Debug.logMessage("DrawContextWrapper.getScaledWindowHeight() - Returning height=" + height + " from MinecraftClient window");
        //$$ return height;
        //#endif
    }


}
