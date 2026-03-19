package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.Stopwatch;

// A task chain that runs a user defined task at the same priority.
// This basically replaces our old Task Runner.
public class UserTaskChain extends SingleTaskChain {

    private final Stopwatch taskStopwatch = new Stopwatch();
    private Runnable currentOnFinish = null;

    private boolean runningIdleTask;
    private boolean nextTaskIdleFlag;

    public UserTaskChain(TaskRunner runner) {
        super(runner);
    }

    private static String prettyPrintTimeDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int hours = minutes / 60;
        int days = hours / 24;

        String result = "";
        if (days != 0) {
            result += days + " days ";
        }
        if (hours != 0) {
            result += (hours % 24) + " hours ";
        }
        if (minutes != 0) {
            result += (minutes % 60) + " minutes ";
        }
        if (!result.isEmpty()) {
            result += "and ";
        }
        result += String.format("%.3f", (seconds % 60));
        return result;
    }

    @Override
    protected void onTick() {

        // Pause if we're not loaded into a world.
        if (!AltoClef.inGame()) return;

        super.onTick();
    }

    public void cancel(AltoClef mod) {
        if (mainTask != null && mainTask.isActive()) {
            stop();
            onTaskFinish(mod);
        }
        mod.getTaskRunner().disable();

        // FIXME kinda junk, the whole pausing should probably be moved to this class
        mod.setStoredTask(null);
        mod.setPaused(false);
    }

    @Override
    public float getPriority() {
        return 50;
    }

    @Override
    public String getName() {
        return "User Tasks";
    }

    public void runTask(AltoClef mod, Task task, Runnable onFinish) {
        runningIdleTask = nextTaskIdleFlag;
        nextTaskIdleFlag = false;

        currentOnFinish = onFinish;

        if (!runningIdleTask) {
        } else {
        }
        mod.getTaskRunner().enable();
        taskStopwatch.begin();
        setTask(task);

        if (mod.getModSettings().failedToLoad()) {
        }
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        boolean shouldIdle = mod.getModSettings().shouldRunIdleCommandWhenNotActive();
        if (!shouldIdle) {
            // Stop.
            mod.getTaskRunner().disable();
            // Extra reset. Sometimes baritone is laggy and doesn't properly reset our press
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }
        double seconds = taskStopwatch.time();
        Task oldTask = mainTask;
        mainTask = null;
        if (currentOnFinish != null) {
            currentOnFinish.run();
        }
        // our `onFinish` might have triggered more tasks.
        boolean actuallyDone = mainTask == null;
        if (actuallyDone) {
            if (!runningIdleTask) {
                EventBus.publish(new TaskFinishedEvent(seconds, oldTask));
            }
            if (shouldIdle) {
                AltoClef.getCommandExecutor().executeWithPrefix(mod.getModSettings().getIdleCommand());
                signalNextTaskToBeIdleTask();
                runningIdleTask = true;
            }
        }
    }

    public boolean isRunningIdleTask() {
        return isActive() && runningIdleTask;
    }

    // The next task will be an idle task.
    public void signalNextTaskToBeIdleTask() {
        nextTaskIdleFlag = true;
    }
}
