/*
 * Decompiled with CFR 0.152.
 */
package me.nullpoint.api.managers;

import me.nullpoint.mod.modules.impl.miscellaneous.Timer;

public class TimerManager {
    public float timer = 1.0f;
    public float lastTime;

    public void set(float factor) {
        if (factor < 0.1f) {
            factor = 0.1f;
        }
        this.timer = factor;
    }

    public void reset() {
        this.lastTime = this.timer = this.getDefault();
    }

    public void tryReset() {
        if (this.lastTime != this.getDefault()) {
            this.reset();
        }
    }

    public float get() {
        return this.timer;
    }

    public float getDefault() {
        return Timer.INSTANCE.isOn() ? Timer.INSTANCE.multiplier.getValueFloat() : 1.0f;
    }
}

