package com.example.studenttaskmanagement.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.notifications.NotificationPreferences;
import com.example.studenttaskmanagement.notifications.NotificationStartup;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch switchTaskReminders;
    private Spinner spinnerDefaultLead;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setReminderEnabledInternal(true);
                } else {
                    switchTaskReminders.setChecked(false);
                    Toast.makeText(
                            this,
                            "Notification permission denied. Reminders remain off.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        switchTaskReminders = findViewById(R.id.switchTaskReminders);
        spinnerDefaultLead = findViewById(R.id.spinnerDefaultLead);

        setupLeadTimeSpinner();
        bindCurrentValues();
        setupActions();
    }

    private void setupLeadTimeSpinner() {
        String[] leadOptions = {"At deadline", "30 min before", "60 min before"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                leadOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultLead.setAdapter(adapter);
    }

    private void bindCurrentValues() {
        boolean enabled = NotificationPreferences.areRemindersEnabled(this);
        switchTaskReminders.setChecked(enabled);
        spinnerDefaultLead.setEnabled(enabled);

        int leadMinutes = NotificationPreferences.getDefaultLeadTimeMinutes(this);
        spinnerDefaultLead.setSelection(leadToPosition(leadMinutes));
    }

    private void setupActions() {
        switchTaskReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestPermissionIfNeededAndEnable();
            } else {
                setReminderEnabledInternal(false);
            }
        });

        spinnerDefaultLead.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                NotificationPreferences.setDefaultLeadTimeMinutes(SettingsActivity.this, positionToLead(position));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void requestPermissionIfNeededAndEnable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            setReminderEnabledInternal(true);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            setReminderEnabledInternal(true);
            return;
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void setReminderEnabledInternal(boolean enabled) {
        NotificationPreferences.setRemindersEnabled(this, enabled);
        spinnerDefaultLead.setEnabled(enabled);
        NotificationStartup.updateReminderWorkerSchedule(getApplicationContext());
    }

    private int leadToPosition(int leadMinutes) {
        if (leadMinutes == NotificationPreferences.LEAD_TIME_AT_DEADLINE) return 0;
        if (leadMinutes == NotificationPreferences.LEAD_TIME_60_MIN) return 2;
        return 1;
    }

    private int positionToLead(int position) {
        if (position == 0) return NotificationPreferences.LEAD_TIME_AT_DEADLINE;
        if (position == 2) return NotificationPreferences.LEAD_TIME_60_MIN;
        return NotificationPreferences.LEAD_TIME_30_MIN;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
