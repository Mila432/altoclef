package adris.altoclef.util.time;


public class TimerReal extends BaseTimer {
    public TimerReal(double intervalSeconds) {
        super(intervalSeconds);
        if (intervalSeconds <= 0) {
        }
    }

    @Override
    protected double currentTime() {
        double time = (double) System.currentTimeMillis() / 1000.0;
        if (time < 0) {
        }
        return time;
    }
}
