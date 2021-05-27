/**
 * @Author: RogerDTZ
 * @FileName: Animator.java
 */

package component.animation;

import component.Component;
import sun.awt.image.ImageWatched;

import java.util.LinkedList;
import java.util.Queue;

public class Animator implements Component {

    private Queue<Animation> queue;
    private boolean active;
    private double sleep;
    private double value;
    private boolean isLoop;
    private double restTime;

    private boolean markForDestroy;


    public Animator(double defaultValue) {
        this.reset(defaultValue);
    }

    public void reset(double defaultValue) {
        this.queue = new LinkedList<>();
        this.active = true;
        this.sleep = 0;
        this.value = defaultValue;
        this.isLoop = false;
        this.restTime = 0;
    }

    public void append(Animation animation) {
        this.queue.offer(animation);
        this.restTime += animation.getDuration();
    }

    public void append(Animation animation, double delayFromNow) {
        animation.setDelay(Math.max(0, delayFromNow - this.restTime));
        this.queue.offer(animation);
        this.restTime += animation.getDuration();
    }

    public void forceAppend(Animation animation) {
        this.queue.clear();
        this.queue.offer(animation);
        this.restTime = animation.getDuration();
    }

    @Override
    public void update(double dt) {
        if (!this.active)
            return;
        if (this.sleep > 0) {
            this.sleep -= dt;
            if (this.sleep > 0)
                return;
            dt = -this.sleep;
        }
        while (!this.queue.isEmpty() && dt > 0) {
            Animation ani = this.queue.peek();
            double rest = ani.getRest();
            if (rest > dt) {
                ani.update(dt);
                this.restTime -= dt;
                dt = 0;
                this.value = ani.val();
            } else {
                ani.update(dt);
                this.restTime -= rest;
                dt -= rest;
                this.value = ani.val();
                this.queue.poll();
                if (this.isLoop) {
                    ani.reset();
                    this.queue.offer(ani);
                    this.restTime += ani.getDuration();
                }
            }
        }
    }

    public void setLoop(boolean flag) {
        this.isLoop = flag;
    }

    public void setActive(boolean flag) {
        this.active = flag;
    }

    public double val() {
        if (!this.queue.isEmpty())
            return this.queue.peek().val();
        else
            return this.value;
    }

    public double endVal() {
        if (!this.queue.isEmpty())
            return ((Animation)this.queue.toArray()[this.queue.toArray().length - 1]).getEndVal();
        else
            return this.value;
    }

    public boolean isIdle() {
        return this.queue.isEmpty();
    }

    // may be override immediately if the Animator is active
    public void setValue(double value) {
        this.value = value;
    }

    public void sleep(double time) {
        this.sleep = time;
    }

    @Override
    public void destroy() {
        this.markForDestroy = true;
    }

    @Override
    public boolean isDestroy() {
        return this.markForDestroy;
    }

}
