/**
 * @Author: RogerDTZ
 * @FileName: Timer.java
 */

package component;

public class Timer implements Component {

    private double time;
    private boolean active;
    private double maxTime;

    private boolean markForDestroy;


    public Timer(double initTime, double maxTime) {
        this.init(initTime, maxTime);
    }

    public Timer(double maxTime) {
        this.init(0, maxTime);
    }

    public void init(double initTime, double maxTime) {
        this.time = initTime;
        this.maxTime = maxTime;
        this.active = true;
    }

    public void init(double maxTime) {
        this.init(0, maxTime);
    }

    @Override
    public void update(double dt) {
        if (active)
            this.time += dt;
    }

    @Override
    public void destroy() {
        this.markForDestroy = true;
    }

    @Override
    public boolean isDestroy() {
        return this.markForDestroy;
    }

    public double getRatio() {
        return time / maxTime;
    }

    public void setActive(boolean flag) {
        this.active = flag;
    }

    public void reset(boolean activeImmediately) {
        this.active = activeImmediately;
        this.time = 0;
    }

    public boolean done() {
        return this.time >= this.maxTime;
    }


}
