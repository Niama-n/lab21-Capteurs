package com.example.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.sensors.fragments.ActivityRecognitionFragment;
import com.example.sensors.fragments.CompassFragment;
import com.example.sensors.fragments.MotionSensorFragment;
import com.example.sensors.fragments.SensorGraphFragment;
import com.example.sensors.fragments.SensorsListFragment;
import com.example.sensors.fragments.StepCounterFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout sideDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sideDrawer = findViewById(R.id.side_drawer);
        NavigationView menuPanel = findViewById(R.id.menu_panel);
        menuPanel.setNavigationItemSelectedListener(this);

        androidx.appcompat.widget.Toolbar topBar = findViewById(R.id.top_bar);
        setSupportActionBar(topBar);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, sideDrawer, topBar,
                R.string.nav_open, R.string.nav_close);
        sideDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (savedInstanceState == null) {
            openFragment(new SensorsListFragment());
            menuPanel.setCheckedItem(R.id.nav_catalog);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem entry) {
        int entryId = entry.getItemId();

        if (entryId == R.id.nav_catalog) {
            openFragment(new SensorsListFragment());
        } else if (entryId == R.id.nav_heat) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_AMBIENT_TEMPERATURE,
                    getString(R.string.title_heat),
                    "FIRST_VALUE"));
        } else if (entryId == R.id.nav_moisture) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_RELATIVE_HUMIDITY,
                    getString(R.string.title_moisture),
                    "FIRST_VALUE"));
        } else if (entryId == R.id.nav_range) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_PROXIMITY,
                    getString(R.string.title_range),
                    "FIRST_VALUE"));
        } else if (entryId == R.id.nav_field) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_MAGNETIC_FIELD,
                    getString(R.string.title_field),
                    "MAGNITUDE"));
        } else if (entryId == R.id.nav_accel) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_ACCELEROMETER,
                    getString(R.string.title_accel)));
        } else if (entryId == R.id.nav_grav) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_GRAVITY,
                    getString(R.string.title_grav)));
        } else if (entryId == R.id.nav_spin) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_GYROSCOPE,
                    getString(R.string.title_spin)));
        } else if (entryId == R.id.nav_pace) {
            openFragment(new StepCounterFragment());
        } else if (entryId == R.id.nav_heading) {
            openFragment(new CompassFragment());
        } else if (entryId == R.id.nav_motion) {
            openFragment(new ActivityRecognitionFragment());
        }

        sideDrawer.closeDrawers();
        return true;
    }

    private void openFragment(Fragment pane) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_pane, pane)
                .commit();
    }
}
