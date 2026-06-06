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
import com.example.sensors.utils.SignalMath;
import com.example.sensors.utils.UiPalette;
import com.example.sensors.views.LineChartView;

public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE       = "title";

    private SensorManager hardwareRegistry;
    private Sensor targetPeripheral;
    private TextView axisLabel;
    private LineChartView trendCanvas;

    private int peripheralKind;
    private String screenTitle;

    public static MotionSensorFragment newInstance(int peripheralKind, String screenTitle) {
        MotionSensorFragment pane = new MotionSensorFragment();
        Bundle payload = new Bundle();
        payload.putInt(ARG_SENSOR_TYPE, peripheralKind);
        payload.putString(ARG_TITLE, screenTitle);
        pane.setArguments(payload);
        return pane;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        peripheralKind = requireArguments().getInt(ARG_SENSOR_TYPE);
        screenTitle    = requireArguments().getString(ARG_TITLE);

        hardwareRegistry = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        targetPeripheral = hardwareRegistry.getDefaultSensor(peripheralKind);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(UiPalette.SURFACE_DEEP);

        TextView heading = new TextView(requireContext());
        heading.setText(screenTitle);
        heading.setTextSize(20f);
        heading.setTextColor(UiPalette.ACCENT_GOLD);
        heading.setPadding(0, 0, 0, 16);

        axisLabel = new TextView(requireContext());
        axisLabel.setTextSize(15f);
        axisLabel.setTextColor(UiPalette.TEXT_BRIGHT);
        axisLabel.setPadding(0, 0, 0, 24);
        axisLabel.setLineSpacing(6f, 1f);

        trendCanvas = new LineChartView(requireContext());
        trendCanvas.setEmptyHint(getString(R.string.msg_waiting_data));
        trendCanvas.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 600));

        root.addView(heading);
        root.addView(axisLabel);
        root.addView(trendCanvas);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (targetPeripheral != null) {
            hardwareRegistry.registerListener(this, targetPeripheral,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            axisLabel.setText(R.string.msg_sensor_missing);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hardwareRegistry.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float axisX = event.values[0];
        float axisY = event.values[1];
        float axisZ = event.values[2];
        float vectorNorm = SignalMath.vectorLength(axisX, axisY, axisZ);

        axisLabel.setText(formatAxisBlock(axisX, axisY, axisZ, vectorNorm));
        trendCanvas.addValue(vectorNorm);
    }

    private String formatAxisBlock(float x, float y, float z, float norm) {
        return getString(R.string.label_axis_x, String.format("%.4f", x)) + "\n"
                + getString(R.string.label_axis_y, String.format("%.4f", y)) + "\n"
                + getString(R.string.label_axis_z, String.format("%.4f", z)) + "\n"
                + getString(R.string.label_magnitude, String.format("%.4f", norm));
    }

    @Override
    public void onAccuracyChanged(Sensor peripheral, int precision) {}
}
