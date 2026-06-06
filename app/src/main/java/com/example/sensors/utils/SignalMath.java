package com.example.sensors.utils;

public final class SignalMath {

    private SignalMath() {}

    /** Euclidean length of a 3-axis sample. */
    public static float vectorLength(float a, float b, float c) {
        return (float) Math.hypot(Math.hypot(a, b), c);
    }

    /** Exponential smoothing: output += gain * (input - output). */
    public static void smoothInPlace(float[] estimate, float gain, float x, float y, float z) {
        estimate[0] += gain * (x - estimate[0]);
        estimate[1] += gain * (y - estimate[1]);
        estimate[2] += gain * (z - estimate[2]);
    }

    /** Population standard deviation (single pass, Welford). */
    public static float populationStdDev(float[] samples, int count) {
        if (count < 2) return 0f;

        float mean = 0f;
        float m2   = 0f;
        for (int i = 0; i < count; i++) {
            float value = samples[i];
            float delta = value - mean;
            mean += delta / (i + 1);
            float delta2 = value - mean;
            m2 += delta * delta2;
        }
        return (float) Math.sqrt(m2 / count);
    }

    public static float peakValue(float[] samples, int count) {
        float peak = 0f;
        for (int i = 0; i < count; i++) {
            peak = Math.max(peak, samples[i]);
        }
        return peak;
    }
}
