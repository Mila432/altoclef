package adris.altoclef.util.slots;


public class ChestSlot extends Slot {

    private final boolean big;

    public ChestSlot(int slot, boolean big) {
        this(slot, big, false);
    }

    public ChestSlot(int slot, boolean big, boolean inventory) {
        super(slot, inventory);
        this.big = big;
    }

    @Override
    public int inventorySlotToWindowSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            int result = inventorySlot + (big ? 81 : 54);
            return result;
        }
        int result = (inventorySlot - 9) + (big ? 54 : 27);
        return result;
    }

    @Override
    protected int windowSlotToInventorySlot(int windowSlot) {
        int bottomStart = (big ? 81 : 54);
        if (windowSlot >= bottomStart) {
            int result = windowSlot - bottomStart;
            return result;
        }
        int result = (windowSlot + 9) - (big ? 54 : 27);
        return result;
    }

    @Override
    protected String getName() {
        return "Chest";
    }
}
