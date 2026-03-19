package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

public class SleepThroughNightTask extends Task {

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        return new PlaceBedAndSetSpawnTask().stayInBed();
    }

    @Override
    protected void onStop(Task interruptTask) {
        if (interruptTask != null) {
        } else {
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SleepThroughNightTask;
    }

    @Override
    protected String toDebugString() {
        return "Sleeping through the night";
    }

    @Override
    public boolean isFinished() {
        // We're in daytime
        int time = (int) (AltoClef.getInstance().getWorld().getTimeOfDay() % 24000);
        boolean finished = 0 <= time && time < 13000;
        if (finished) {
        }
        return finished;
    }
}
