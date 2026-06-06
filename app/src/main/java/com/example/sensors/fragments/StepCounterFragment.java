package com.example.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sensors.R;
import com.example.sensors.utils.UiPalette;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager hardwareRegistry;
    private Sensor pacePeripheral;
    private TextView statsLabel;
    private float baselineCount = -1;

    private final ActivityResultLauncher<String> permissionGate =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) startSensor();
                        else statsLabel.setText(R.string.msg_permission_denied);
                    });

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
        banner.setText(R.string.title_pace);
        banner.setTextSize(22f);
        banner.setTextColor(UiPalette.ACCENT_GOLD);
        banner.setPadding(0, 0, 0, 32);
        root.addView(banner);

        statsLabel = new TextView(requireContext());
        statsLabel.setTextSize(18f);
        statsLabel.setTextColor(UiPalette.TEXT_BRIGHT);
        statsLabel.setLineSpacing(8f, 1f);
        root.addView(statsLabel);

        hardwareRegistry = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        pacePeripheral = hardwareRegistry.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pacePeripheral == null) {
            statsLabel.setText(R.string.msg_pace_missing);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionGate.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            startSensor();
        }
    }

    private void startSensor() {
        hardwareRegistry.registerListener(this, pacePeripheral,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        hardwareRegistry.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float cumulativeSteps = event.values[0];
        if (baselineCount < 0) baselineCount = cumulativeSteps;
        int sessionTotal = (int) (cumulativeSteps - baselineCount);

        statsLabel.setText(
                getString(R.string.label_steps_boot) + "\n"
                        + getString(R.string.label_steps_unit, (int) cumulativeSteps) + "\n\n"
                        + getString(R.string.label_steps_session) + "\n"
                        + getString(R.string.label_steps_unit, sessionTotal));
    }

    @Override
    public void onAccuracyChanged(Sensor peripheral, int precision) {}
}
