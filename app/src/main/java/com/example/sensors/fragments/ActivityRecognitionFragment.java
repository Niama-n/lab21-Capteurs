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
import com.example.sensors.utils.MotionHeuristics;
import com.example.sensors.utils.SignalMath;
import com.example.sensors.utils.UiPalette;

public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private static final float GRAVITY_TRACKING_GAIN = 0.2f;

    private SensorManager hardwareRegistry;
    private Sensor accelPeripheral;
    private TextView outcomeLabel;

    private final float[] gravityEstimate = new float[3];
    private final float[] energyRing = new float[MotionHeuristics.RING_CAPACITY];
    private int ringCursor = 0;
    private int ringFilled = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout root = buildScreenShell();
        outcomeLabel = (TextView) root.getChildAt(1);

        hardwareRegistry = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelPeripheral = hardwareRegistry.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return root;
    }

    private LinearLayout buildScreenShell() {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(UiPalette.SURFACE_DEEP);

        TextView banner = new TextView(requireContext());
        banner.setText(R.string.title_motion);
        banner.setTextSize(22f);
        banner.setTextColor(UiPalette.ACCENT_GOLD);
        banner.setPadding(0, 0, 0, 32);
        root.addView(banner);

        TextView body = new TextView(requireContext());
        body.setTextSize(15f);
        body.setTextColor(UiPalette.TEXT_BRIGHT);
        body.setLineSpacing(8f, 1f);
        root.addView(body);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelPeripheral != null) {
            hardwareRegistry.registerListener(this, accelPeripheral,
                    SensorManager.SENSOR_DELAY_GAME);
        } else {
            outcomeLabel.setText(R.string.msg_accel_missing);
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

        SignalMath.smoothInPlace(gravityEstimate, GRAVITY_TRACKING_GAIN, axisX, axisY, axisZ);

        float kinetic = SignalMath.vectorLength(
                axisX - gravityEstimate[0],
                axisY - gravityEstimate[1],
                axisZ - gravityEstimate[2]);

        recordEnergy(kinetic);

        MotionHeuristics.PostureKind posture =
                MotionHeuristics.evaluate(energyRing, ringFilled, axisX, axisY, axisZ);

        String motionTag = posture == MotionHeuristics.PostureKind.CALIBRATING
                ? getString(R.string.msg_calibrating)
                : MotionHeuristics.labelFor(posture);

        outcomeLabel.setText(
                getString(R.string.label_axis_x, String.format("%.3f", axisX)) + "\n"
                        + getString(R.string.label_axis_y, String.format("%.3f", axisY)) + "\n"
                        + getString(R.string.label_axis_z, String.format("%.3f", axisZ)) + "\n\n"
                        + getString(R.string.label_movement, String.format("%.3f", kinetic)) + "\n\n"
                        + getString(R.string.label_detected) + "\n" + motionTag);
    }

    private void recordEnergy(float kinetic) {
        energyRing[ringCursor] = kinetic;
        ringCursor = (ringCursor + 1) % MotionHeuristics.RING_CAPACITY;
        if (ringFilled < MotionHeuristics.RING_CAPACITY) {
            ringFilled++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor peripheral, int precision) {}
}
