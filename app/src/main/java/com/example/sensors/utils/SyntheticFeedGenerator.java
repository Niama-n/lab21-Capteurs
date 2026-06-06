package com.example.sensors.utils;

import android.hardware.Sensor;

public final class SyntheticFeedGenerator {

    private SyntheticFeedGenerator() {}

    public static float nextSample(int sensorKind, long tick) {
        switch (sensorKind) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return 21f + 3f * (float) Math.cos(tick * 0.2f);
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return 50f + 15f * (float) Math.sin(tick * 0.15f);
            case Sensor.TYPE_PROXIMITY:
                return ((tick / 3) % 2 == 0) ? 0f : 5f;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return 40f + 10f * (float) Math.cos(tick * 0.25f);
            default:
                return (float) Math.sin(tick * 0.5f);
        }
    }
}
