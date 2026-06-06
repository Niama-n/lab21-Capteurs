package com.example.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sensors.R;
import com.example.sensors.utils.BearingMapper;
import com.example.sensors.utils.UiPalette;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager hardwareRegistry;
    private Sensor accelPeripheral;
    private Sensor fieldPeripheral;
    private TextView bearingLabel;

    private final float[] tiltVector  = new float[3];
    private final float[] fieldVector = new float[3];
    private boolean tiltReady  = false;
    private boolean fieldReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(UiPalette.SURFACE_DEEP);

        TextView banner = new TextView(requireContext());
        banner.setText(R.string.title_heading);
        banner.setTextSize(22f);
        banner.setTextColor(UiPalette.ACCENT_GOLD);
        banner.setPadding(0, 0, 0, 32);
        root.addView(banner);

        bearingLabel = new TextView(requireContext());
        bearingLabel.setTextSize(20f);
        bearingLabel.setTextColor(UiPalette.TEXT_BRIGHT);
        bearingLabel.setLineSpacing(8f, 1f);
        root.addView(bearingLabel);

        hardwareRegistry = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelPeripheral = hardwareRegistry.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        fieldPeripheral = hardwareRegistry.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelPeripheral != null) {
            hardwareRegistry.registerListener(this, accelPeripheral,
                    SensorManager.SENSOR_DELAY_UI);
        }
        if (fieldPeripheral != null) {
            hardwareRegistry.registerListener(this, fieldPeripheral,
                    SensorManager.SENSOR_DELAY_UI);
        }
        if (accelPeripheral == null || fieldPeripheral == null) {
            bearingLabel.setText(R.string.msg_compass_missing);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hardwareRegistry.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        cacheSample(event);
        if (tiltReady && fieldReady) {
            renderHeading();
        }
    }

    private void cacheSample(SensorEvent event) {
        int kind = event.sensor.getType();
        if (kind == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, tiltVector, 0, 3);
            tiltReady = true;
        } else if (kind == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, fieldVector, 0, 3);
            fieldReady = true;
        }
    }

    private void renderHeading() {
        float[] rotation = new float[9];
        float[] angles   = new float[3];

        if (!SensorManager.getRotationMatrix(rotation, null, tiltVector, fieldVector)) {
            return;
        }

        SensorManager.getOrientation(rotation, angles);
        float azimuthDeg = BearingMapper.normalizeAzimuth(angles[0]);
        String cardinal  = BearingMapper.sectorLabel(azimuthDeg);

        bearingLabel.setText(
                getString(R.string.label_azimuth, String.format("%.1f", azimuthDeg))
                        + "\n\n"
                        + getString(R.string.label_direction, cardinal));
    }

    @Override
    public void onAccuracyChanged(Sensor peripheral, int precision) {}
}
