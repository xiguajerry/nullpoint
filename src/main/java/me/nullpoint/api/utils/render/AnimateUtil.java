package me.nullpoint.api.utils.render;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;

public class AnimateUtil implements Wrapper {
    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1.0) {
            return endPoint;
        } else {
            return speed == 0.0 ? current : thunder(current, endPoint, speed);
        }
    }

    public static double animate(double current, double endPoint, double speed, AnimMode mode) {
        return switch (mode) {
            case Mio -> mio(current, endPoint, speed);
            case Thunder -> thunder(current, endPoint, speed);
            case My -> my(current, endPoint, speed);
            case Old -> old(current, endPoint, speed);
            case Normal -> normal(current, endPoint, speed);
            default -> endPoint;
        };
    }

    public static double mio(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        } else {
            int negative = speed < 0.0 ? -1 : 1;
            if (negative == -1) {
                speed *= -1.0;
            }

            double diff = endPoint - current;
            double factor = diff * (double) mc.getTickDelta() / (1.0 / speed * (Math.min(240.0, Nullpoint.FPS.getFps()) / 240.0));
            if (diff < 0.0 && factor < diff) {
                factor = diff;
            } else if (diff > 0.0 && factor >= diff) {
                factor = diff;
            }

            return current + factor * (double) negative;
        }
    }

    public static double old(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        } else {
            int negative = speed < 0.0 ? -1 : 1;
            if (negative == -1) {
                speed *= -1.0;
            }

            double diff = endPoint - current;
            double factor = diff * speed;
            if (diff < 0.0 && factor < diff) {
                factor = diff;
            } else if (diff > 0.0 && factor >= diff) {
                factor = diff;
            }

            return current + factor * (double) negative;
        }
    }

    public static double my(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        } else {
            int negative = speed < 0.0 ? -1 : 1;
            if (negative == -1) {
                speed *= -1.0;
            }

            double diff = endPoint - current;
            double factor = diff * (double) mc.getTickDelta() * speed;
            if (diff < 0.0 && factor < diff) {
                factor = diff;
            } else if (diff > 0.0 && factor >= diff) {
                factor = diff;
            }

            return current + factor * (double) negative;
        }
    }

    public static double thunder(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        if (Math.abs(dif) <= 0.001) {
            return endPoint;
        } else {
            double factor = dif * speed;
            return current + (shouldContinueAnimation ? factor : -factor);
        }
    }

    public static double normal(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        speed *= 10.0;
        return Math.abs(Math.max(endPoint, current) - Math.min(endPoint, current)) <= speed ? endPoint : current + (shouldContinueAnimation ? speed : -speed);
    }

    public enum AnimMode {
        Thunder,
        Mio,
        My,
        Old,
        Normal,
        None
    }
}
