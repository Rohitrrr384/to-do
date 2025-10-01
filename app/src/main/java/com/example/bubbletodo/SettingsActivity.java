package com.example.bubbletodo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private Switch soundSwitch;
    private Switch vibrationSwitch;
    private Switch physicsSwitch;
    private Switch autoArrangeSwitch;
    private Spinner themeSpinner;
    private SeekBar animationSpeedSeekBar;
    private SeekBar maxBubblesSeekBar;
    private TextView maxBubblesValue;
    private TextView tasksCreatedCount;
    private TextView tasksCompletedCount;
    private TextView bubblesBurstCount;
    private TextView sessionTimeCount;
    private TextView versionText;
    private MaterialButton rateAppButton;
    private MaterialButton shareAppButton;
    private MaterialButton clearDataButton;
    private MaterialButton resetSettingsButton;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "BubbleTodoPrefs";
    private static final String PREF_SOUND_ENABLED = "sound_enabled";
    private static final String PREF_VIBRATION_ENABLED = "vibration_enabled";
    private static final String PREF_PHYSICS_ENABLED = "physics_enabled";
    private static final String PREF_AUTO_ARRANGE = "auto_arrange";
    private static final String PREF_THEME = "theme";
    private static final String PREF_ANIMATION_SPEED = "animation_speed";
    private static final String PREF_MAX_BUBBLES = "max_bubbles";
    private static final String PREF_TASKS_CREATED = "tasks_created";
    private static final String PREF_TASKS_COMPLETED = "tasks_completed";
    private static final String PREF_BUBBLES_BURST = "bubbles_burst";
    private static final String PREF_SESSION_TIME = "session_time";

    // Theme options
    private String[] themeOptions = {
            "Default Blue",
            "Ocean Breeze",
            "Sunset Glow",
            "Forest Green",
            "Purple Dreams",
            "Monochrome"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeComponents();
        setupToolbar();
        setupSharedPreferences();
        loadSettings();
        setupEventListeners();
        updateStatistics();
    }

    private void initializeComponents() {
        toolbar = findViewById(R.id.toolbar);
        soundSwitch = findViewById(R.id.soundSwitch);
        vibrationSwitch = findViewById(R.id.vibrationSwitch);
        physicsSwitch = findViewById(R.id.physicsSwitch);
        autoArrangeSwitch = findViewById(R.id.autoArrangeSwitch);
        themeSpinner = findViewById(R.id.themeSpinner);
        animationSpeedSeekBar = findViewById(R.id.animationSpeedSeekBar);
        maxBubblesSeekBar = findViewById(R.id.maxBubblesSeekBar);
        maxBubblesValue = findViewById(R.id.maxBubblesValue);
        tasksCreatedCount = findViewById(R.id.tasksCreatedCount);
        tasksCompletedCount = findViewById(R.id.tasksCompletedCount);
        bubblesBurstCount = findViewById(R.id.bubblesBurstCount);
        sessionTimeCount = findViewById(R.id.sessionTimeCount);
        versionText = findViewById(R.id.versionText);
        rateAppButton = findViewById(R.id.rateAppButton);
        shareAppButton = findViewById(R.id.shareAppButton);
        clearDataButton = findViewById(R.id.clearDataButton);
        resetSettingsButton = findViewById(R.id.resetSettingsButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private void loadSettings() {
        // Load switch states
        soundSwitch.setChecked(sharedPreferences.getBoolean(PREF_SOUND_ENABLED, true));
        vibrationSwitch.setChecked(sharedPreferences.getBoolean(PREF_VIBRATION_ENABLED, true));
        physicsSwitch.setChecked(sharedPreferences.getBoolean(PREF_PHYSICS_ENABLED, true));
        autoArrangeSwitch.setChecked(sharedPreferences.getBoolean(PREF_AUTO_ARRANGE, false));

        // Load seekbar values
        int animationSpeed = sharedPreferences.getInt(PREF_ANIMATION_SPEED, 50);
        animationSpeedSeekBar.setProgress(animationSpeed);

        int maxBubbles = sharedPreferences.getInt(PREF_MAX_BUBBLES, 25);
        maxBubblesSeekBar.setProgress(maxBubbles - 10); // Adjust for min value
        maxBubblesValue.setText(String.valueOf(maxBubbles));

        // Setup theme spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, themeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        int themeSelection = sharedPreferences.getInt(PREF_THEME, 0);
        themeSpinner.setSelection(themeSelection);

        // Set version text
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("Version " + versionName);
        } catch (Exception e) {
            versionText.setText("Version 1.0");
        }
    }

    private void setupEventListeners() {
        // Sound switch
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(PREF_SOUND_ENABLED, isChecked).apply();
            showToast(isChecked ? "Sound effects enabled" : "Sound effects disabled");
        });

        // Vibration switch
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(PREF_VIBRATION_ENABLED, isChecked).apply();
            showToast(isChecked ? "Vibration enabled" : "Vibration disabled");
        });

        // Physics switch
        physicsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(PREF_PHYSICS_ENABLED, isChecked).apply();
            showToast(isChecked ? "Physics enabled" : "Physics disabled");
        });

        // Auto-arrange switch
        autoArrangeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(PREF_AUTO_ARRANGE, isChecked).apply();
            showToast(isChecked ? "Auto-arrange enabled" : "Auto-arrange disabled");
        });

        // Animation speed seekbar
        animationSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    editor.putInt(PREF_ANIMATION_SPEED, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Animation speed updated");
            }
        });

        // Max bubbles seekbar
        maxBubblesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actualValue = progress + 10; // Add min value
                maxBubblesValue.setText(String.valueOf(actualValue));
                if (fromUser) {
                    editor.putInt(PREF_MAX_BUBBLES, actualValue).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showToast("Max bubbles updated");
            }
        });

        // Theme spinner
        themeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                editor.putInt(PREF_THEME, position).apply();
                showToast("Theme changed to: " + themeOptions[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Rate app button
        rateAppButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                // If Play Store app is not available, open in browser
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
            }
        });

        // Share app button
        shareAppButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out Bubble To-Do!");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey! I found this amazing task management app called Bubble To-Do. " +
                            "It makes managing tasks fun with floating bubbles! 🎈\n\n" +
                            "Get it here: https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share Bubble To-Do"));
        });

        // Clear data button
        clearDataButton.setOnClickListener(v -> showClearDataDialog());

        // Reset settings button
        resetSettingsButton.setOnClickListener(v -> showResetSettingsDialog());
    }

    private void updateStatistics() {
        int tasksCreated = sharedPreferences.getInt(PREF_TASKS_CREATED, 0);
        int tasksCompleted = sharedPreferences.getInt(PREF_TASKS_COMPLETED, 0);
        int bubblesBurst = sharedPreferences.getInt(PREF_BUBBLES_BURST, 0);
        long sessionTime = sharedPreferences.getLong(PREF_SESSION_TIME, 0);

        tasksCreatedCount.setText(String.valueOf(tasksCreated));
        tasksCompletedCount.setText(String.valueOf(tasksCompleted));
        bubblesBurstCount.setText(String.valueOf(bubblesBurst));

        // Convert session time from milliseconds to minutes
        int sessionMinutes = (int) (sessionTime / (1000 * 60));
        if (sessionMinutes < 60) {
            sessionTimeCount.setText(sessionMinutes + "m");
        } else {
            int hours = sessionMinutes / 60;
            int minutes = sessionMinutes % 60;
            sessionTimeCount.setText(hours + "h " + minutes + "m");
        }
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Clear All Data")
                .setMessage("This will permanently delete all your tasks, statistics, and settings. This action cannot be undone!\n\nAre you sure you want to continue?")
                .setPositiveButton("Clear Everything", (dialog, which) -> {
                    clearAllData();
                    showToast("All data cleared successfully");

                    // Return to main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showResetSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🔄 Reset Settings")
                .setMessage("This will reset all settings to their default values. Your tasks and statistics will not be affected.\n\nContinue?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    resetSettingsToDefault();
                    loadSettings();
                    showToast("Settings reset to defaults");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void resetSettingsToDefault() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Reset only settings, keep statistics
        int tasksCreated = sharedPreferences.getInt(PREF_TASKS_CREATED, 0);
        int tasksCompleted = sharedPreferences.getInt(PREF_TASKS_COMPLETED, 0);
        int bubblesBurst = sharedPreferences.getInt(PREF_BUBBLES_BURST, 0);
        long sessionTime = sharedPreferences.getLong(PREF_SESSION_TIME, 0);

        editor.clear();

        // Restore statistics
        editor.putInt(PREF_TASKS_CREATED, tasksCreated);
        editor.putInt(PREF_TASKS_COMPLETED, tasksCompleted);
        editor.putInt(PREF_BUBBLES_BURST, bubblesBurst);
        editor.putLong(PREF_SESSION_TIME, sessionTime);

        // Set default settings
        editor.putBoolean(PREF_SOUND_ENABLED, true);
        editor.putBoolean(PREF_VIBRATION_ENABLED, true);
        editor.putBoolean(PREF_PHYSICS_ENABLED, true);
        editor.putBoolean(PREF_AUTO_ARRANGE, false);
        editor.putInt(PREF_THEME, 0);
        editor.putInt(PREF_ANIMATION_SPEED, 50);
        editor.putInt(PREF_MAX_BUBBLES, 25);

        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Return settings data to main activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("settings_changed", true);
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Static methods to help other activities access settings
    public static boolean isSoundEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_SOUND_ENABLED, true);
    }

    public static boolean isVibrationEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_VIBRATION_ENABLED, true);
    }

    public static boolean isPhysicsEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_PHYSICS_ENABLED, true);
    }

    public static boolean isAutoArrangeEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_AUTO_ARRANGE, false);
    }

    public static int getAnimationSpeed(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_ANIMATION_SPEED, 50);
    }

    public static int getMaxBubbles(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_MAX_BUBBLES, 25);
    }

    public static int getTheme(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_THEME, 0);
    }

    // Static methods to update statistics
    public static void incrementTasksCreated(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int count = prefs.getInt(PREF_TASKS_CREATED, 0);
        prefs.edit().putInt(PREF_TASKS_CREATED, count + 1).apply();
    }

    public static void incrementTasksCompleted(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int count = prefs.getInt(PREF_TASKS_COMPLETED, 0);
        prefs.edit().putInt(PREF_TASKS_COMPLETED, count + 1).apply();
    }

    public static void incrementBubblesBurst(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int count = prefs.getInt(PREF_BUBBLES_BURST, 0);
        prefs.edit().putInt(PREF_BUBBLES_BURST, count + 1).apply();
    }

    public static void updateSessionTime(android.content.Context context, long additionalTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long currentTime = prefs.getLong(PREF_SESSION_TIME, 0);
        prefs.edit().putLong(PREF_SESSION_TIME, currentTime + additionalTime).apply();
    }
}