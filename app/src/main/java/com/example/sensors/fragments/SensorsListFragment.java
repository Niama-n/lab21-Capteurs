package com.example.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sensors.R;
import com.example.sensors.utils.SensorFormatter;
import com.example.sensors.utils.UiPalette;

import java.util.List;

public class SensorsListFragment extends Fragment {

    private SensorManager hardwareRegistry;
    private LinearLayout itemHolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        ScrollView scrollPane = new ScrollView(requireContext());
        scrollPane.setBackgroundColor(UiPalette.SURFACE_DEEP);

        itemHolder = new LinearLayout(requireContext());
        itemHolder.setOrientation(LinearLayout.VERTICAL);
        itemHolder.setPadding(32, 32, 32, 32);
        scrollPane.addView(itemHolder);

        TextView banner = new TextView(requireContext());
        banner.setText(R.string.title_catalog);
        banner.setTextSize(22f);
        banner.setTextColor(UiPalette.ACCENT_GOLD);
        banner.setPadding(0, 0, 0, 32);
        itemHolder.addView(banner);

        hardwareRegistry = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        displaySensors();

        return scrollPane;
    }

    private void displaySensors() {
        List<Sensor> peripherals = hardwareRegistry.getSensorList(Sensor.TYPE_ALL);

        for (Sensor peripheral : peripherals) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(28, 24, 28, 24);
            card.setBackgroundColor(UiPalette.SURFACE_CARD);

            LinearLayout.LayoutParams cardLayout = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLayout.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardLayout);

            TextView labelView = new TextView(requireContext());
            labelView.setText(peripheral.getName());
            labelView.setTextSize(15f);
            labelView.setTextColor(UiPalette.ACCENT_VIOLET);
            labelView.setPadding(0, 0, 0, 10);

            TextView specView = new TextView(requireContext());
            specView.setText(SensorFormatter.format(peripheral));
            specView.setTextSize(12.5f);
            specView.setTextColor(UiPalette.TEXT_MUTED);
            specView.setLineSpacing(4f, 1f);

            card.addView(labelView);
            card.addView(specView);
            itemHolder.addView(card);
        }
    }
}
