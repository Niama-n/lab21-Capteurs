package com.example.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.sensors.utils.SyntheticFeedGenerator;
import com.example.sensors.utils.UiPalette;
import com.example.sensors.views.LineChartView;

public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE       = "title";
    private static final String ARG_MODE        = "mode";
    private static final long MOCK_INTERVAL_MS  = 1000L;

    private SensorManager hardwareRegistry;
    private Sensor targetPeripheral;
    private TextView readingLabel;
    private LineChartView trendCanvas;

    private int peripheralKind;
    private String screenTitle;
    private String parseMode;

    private final Handler mockDataLooper = new Handler(Looper.getMainLooper());
    private long mockTick = 0L;

    public static SensorGraphFragment newInstance(int peripheralKind, String screenTitle, String parseMode) {
        SensorGraphFragment pane = new SensorGraphFragment();
        Bundle payload = new Bundle();
        payload.putInt(ARG_SENSOR_TYPE, peripheralKind);
        payload.putString(ARG_TITLE, screenTitle);
        payload.putString(ARG_MODE, parseMode);
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
        parseMode      = requireArguments().getString(ARG_MODE);

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

        readingLabel = new TextView(requireContext());
        readingLabel.setTextSize(16f);
        readingLabel.setTextColor(UiPalette.TEXT_BRIGHT);
        readingLabel.setPadding(0, 0, 0, 24);

        trendCanvas = new LineChartView(requireContext());
        trendCanvas.setEmptyHint(getString(R.string.msg_waiting_data));
        trendCanvas.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 600));

        root.addView(heading);
        root.addView(readingLabel);
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
            readingLabel.setText(R.string.msg_sensor_mock);
            scheduleMockTick();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hardwareRegistry.unregisterListener(this);
        mockDataLooper.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        publishReading(parseSample(event.values));
    }

    @Override
    public void onAccuracyChanged(Sensor peripheral, int precision) {}

    private float parseSample(float[] rawSamples) {
        if ("MAGNITUDE".equals(parseMode)) {
            return SignalMath.vectorLength(rawSamples[0], rawSamples[1], rawSamples[2]);
        }
        return rawSamples[0];
    }

    private void publishReading(float reading) {
        readingLabel.setText(getString(R.string.label_reading,
                String.format("%.4f", reading)));
        trendCanvas.addValue(reading);
    }

    private void scheduleMockTick() {
        mockDataLooper.postDelayed(new Runnable() {
            @Override
            public void run() {
                mockTick++;
                publishReading(SyntheticFeedGenerator.nextSample(peripheralKind, mockTick));
                mockDataLooper.postDelayed(this, MOCK_INTERVAL_MS);
            }
        }, MOCK_INTERVAL_MS);
    }
}
