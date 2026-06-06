package com.example.sensors.utils;

public final class BearingMapper {

    private static final String[] SECTOR_LABELS = {
            "North ↑", "North-East ↗", "East →", "South-East ↘",
            "South ↓", "South-West ↙", "West ←", "North-West ↖"
    };

    private static final float DEGREES_PER_SECTOR = 45f;

    private BearingMapper() {}

    public static float normalizeAzimuth(float radians) {
        float degrees = (float) Math.toDegrees(radians);
        return degrees < 0f ? degrees + 360f : degrees;
    }

    public static String sectorLabel(float azimuthDegrees) {
        int index = (int) Math.floor((azimuthDegrees + DEGREES_PER_SECTOR / 2f) / DEGREES_PER_SECTOR);
        return SECTOR_LABELS[index % SECTOR_LABELS.length];
    }
}
