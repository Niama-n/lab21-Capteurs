package com.example.sensors.utils;

import android.hardware.Sensor;

public class SensorFormatter {

    public static String format(Sensor peripheral) {
        return "Identifier : " + peripheral.getId() + "\n"
                + "Label : " + peripheral.getName() + "\n"
                + "Manufacturer : " + peripheral.getVendor() + "\n"
                + "Revision : " + peripheral.getVersion() + "\n"
                + "Category : " + peripheral.getStringType() + "\n"
                + "Numeric code : " + peripheral.getType() + "\n"
                + "Precision : " + peripheral.getResolution() + "\n"
                + "Consumption : " + peripheral.getPower() + " mA\n"
                + "Upper bound : " + peripheral.getMaximumRange() + "\n"
                + "Min interval : " + peripheral.getMinDelay() + " µs\n";
    }
}
