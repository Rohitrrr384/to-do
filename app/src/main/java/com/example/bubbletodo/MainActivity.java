package com.example.bubbletodo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bubbletodo.SoundManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout bubbleContainer;
    private FloatingActionButton fabAdd;
    private List<BubbleTask> bubbleTasks;
    private Random random;
    private Vibrator vibrator;
    private GestureDetector gestureDetector;
    private ImageView btnSettings;
    private SoundManager soundManager ;

    // Settings integration
    private static final int SETTINGS_REQUEST_CODE = 1001;
    private long sessionStartTime;

    // Task categories with their colors and transparency
    public enum TaskCategory {
        WORK(Color.parseColor("#4285F4"), "Work", 0.85f),
        PERSONAL(Color.parseColor("#FFD700"), "Personal", 0.85f),
        HEALTH(Color.parseColor("#34A853"), "Health", 0.85f),
        URGENT(Color.parseColor("#FF5722"), "Urgent", 0.90f);

        private final int color;
        private final String name;
        private final float alpha;

        TaskCategory(int color, String name, float alpha) {
            this.color = color;
            this.name = name;
            this.alpha = alpha;
        }

        public int getColor() { return color; }
        public String getName() { return name; }
        public float getAlpha() { return alpha; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionStartTime = System.currentTimeMillis();

        initializeViews();
        setupGestureDetector();
        bubbleTasks = new ArrayList<>();
        random = new Random();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        soundManager = SoundManager.getInstance(this);

        applyTheme();
        addSampleBubbles();
    }

    private void initializeViews() {
        bubbleContainer = findViewById(R.id.bubbleContainer);
        fabAdd = findViewById(R.id.fabAdd);
        btnSettings = findViewById(R.id.btnSettings);

        fabAdd.setOnClickListener(v -> showAddTaskDialog());
        btnSettings.setOnClickListener(v -> openSettings());
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("settings_changed", false)) {
                applySettingsToExistingBubbles();
                applyTheme();
            }
        }
    }

    private void applySettingsToExistingBubbles() {
        boolean physicsEnabled = SettingsActivity.isPhysicsEnabled(this);

        for (int i = 0; i < bubbleContainer.getChildCount(); i++) {
            View child = bubbleContainer.getChildAt(i);
            if (child.getTag() instanceof BubbleTask) {
                if (physicsEnabled) {
                    if (child.getTag(R.id.float_animator) == null) {
                        startFloatingAnimation(child);
                    }
                } else {
                    ObjectAnimator floatAnimator = (ObjectAnimator) child.getTag(R.id.float_animator);
                    if (floatAnimator != null) {
                        floatAnimator.cancel();
                        child.setTag(R.id.float_animator, null);
                    }
                }
            }
        }
    }

    private void applyTheme() {
        int themeIndex = SettingsActivity.getTheme(this);
        RelativeLayout mainLayout = findViewById(R.id.bubbleContainer).getParent() instanceof RelativeLayout ?
                (RelativeLayout) findViewById(R.id.bubbleContainer).getParent() : null;

        if (mainLayout != null) {
            switch (themeIndex) {
                case 0: mainLayout.setBackgroundResource(R.drawable.bubble_background); break;
                case 1: mainLayout.setBackgroundResource(R.drawable.theme_ocean); break;
                case 2: mainLayout.setBackgroundResource(R.drawable.theme_sunset); break;
                case 3: mainLayout.setBackgroundResource(R.drawable.theme_forest); break;
                case 4: mainLayout.setBackgroundResource(R.drawable.theme_purple); break;
                case 5: mainLayout.setBackgroundResource(R.drawable.theme_mono); break;
                default: mainLayout.setBackgroundResource(R.drawable.bubble_background);
            }
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distance = Math.abs(e1.getX() - e2.getX()) + Math.abs(e1.getY() - e2.getY());
                if (distance > 200) {
                    triggerBurstMode(e1, e2);
                    return true;
                }
                return false;
            }
        });

        bubbleContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void addSampleBubbles() {
        addBubbleTask("Review project proposal", TaskCategory.WORK);
        addBubbleTask("Buy groceries", TaskCategory.PERSONAL);
        addBubbleTask("Morning workout", TaskCategory.HEALTH);
        addBubbleTask("Client meeting", TaskCategory.URGENT);
    }

    private void showAddTaskDialog() {
        soundManager.playClickSound();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Bubble Task");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText taskInput = new EditText(this);
        taskInput.setHint("Enter your task...");
        layout.addView(taskInput);

        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category will be randomly assigned");
        categoryLabel.setPadding(0, 20, 0, 0);
        layout.addView(categoryLabel);

        builder.setView(layout);

        builder.setPositiveButton("Add Bubble", (dialog, which) -> {
            String taskText = taskInput.getText().toString().trim();
            if (!taskText.isEmpty()) {
                TaskCategory randomCategory = TaskCategory.values()[random.nextInt(TaskCategory.values().length)];
                addBubbleTask(taskText, randomCategory);
                soundManager.playAddSound();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addBubbleTask(String taskText, TaskCategory category) {
        // Check max bubbles limit
        int maxBubbles = SettingsActivity.getMaxBubbles(this);
        if (bubbleTasks.size() >= maxBubbles) {
            Toast.makeText(this, "Maximum bubble limit reached (" + maxBubbles + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        BubbleTask bubbleTask = new BubbleTask(taskText, category);
        bubbleTasks.add(bubbleTask);

        // Track statistics
        SettingsActivity.incrementTasksCreated(this);

        // Create bubble view
        TextView bubbleView = createRealisticBubbleView(bubbleTask);
        bubbleContainer.addView(bubbleView);

        // Position bubble at bottom initially for flow animation
        positionBubbleAtBottom(bubbleView);

        // Add flowing entrance animation from bottom to random position
        animateBubbleFlowEntrance(bubbleView);

        // Set up interactions
        setupBubbleInteractions(bubbleView, bubbleTask);
    }

    private TextView createRealisticBubbleView(BubbleTask bubbleTask) {
        TextView bubbleView = new TextView(this);
        bubbleView.setText(bubbleTask.getText());
        bubbleView.setTextColor(Color.WHITE);
        bubbleView.setTextSize(11);
        bubbleView.setPadding(25, 15, 25, 15);
        bubbleView.setMaxLines(3);
        bubbleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        bubbleView.setShadowLayer(8.0f, 0.0f, 4.0f, Color.parseColor("#40000000"));

        // Create realistic transparent bubble with gradient and glow
        GradientDrawable bubble = new GradientDrawable();
        bubble.setShape(GradientDrawable.OVAL);

        // Create gradient effect for more realistic bubble appearance
        int primaryColor = bubbleTask.getCategory().getColor();
        int lighterColor = adjustColorBrightness(primaryColor, 0.3f);
        int[] gradientColors = {lighterColor, primaryColor, adjustColorBrightness(primaryColor, -0.2f)};
        bubble.setColors(gradientColors);
        bubble.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        bubble.setGradientRadius(120f);

        // Add subtle border with transparency
        bubble.setStroke(3, Color.parseColor("#30FFFFFF"));

        bubbleView.setBackground(bubble);

        // Set transparency
        bubbleView.setAlpha(bubbleTask.getCategory().getAlpha());

        // Random size with more variation
        int bubbleSize = 140 + random.nextInt(120); // 140-260dp
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(bubbleSize, bubbleSize);
        bubbleView.setLayoutParams(params);

        bubbleView.setTag(bubbleTask);
        return bubbleView;
    }

    private int adjustColorBrightness(int color, float factor) {
        int r = Math.round((Color.red(color) * (1 + factor)));
        int g = Math.round((Color.green(color) * (1 + factor)));
        int b = Math.round((Color.blue(color) * (1 + factor)));

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return Color.rgb(r, g, b);
    }

    private void positionBubbleAtBottom(View bubbleView) {
        bubbleView.post(() -> {
            int containerWidth = bubbleContainer.getWidth();
            int containerHeight = bubbleContainer.getHeight();
            int bubbleWidth = bubbleView.getWidth();

            // Start at bottom center
            int x = (containerWidth - bubbleWidth) / 2;
            int y = containerHeight; // Start below visible area

            bubbleView.setX(x);
            bubbleView.setY(y);
        });
    }

    private void animateBubbleFlowEntrance(View bubbleView) {
        bubbleView.setScaleX(0.3f);
        bubbleView.setScaleY(0.3f);
        bubbleView.setAlpha(0f);

        bubbleView.post(() -> {
            // Calculate final random position
            int containerWidth = bubbleContainer.getWidth();
            int containerHeight = bubbleContainer.getHeight();
            int bubbleWidth = bubbleView.getWidth();
            int bubbleHeight = bubbleView.getHeight();

            // Allow positioning anywhere on screen (not just top)
            int finalX = random.nextInt(Math.max(1, containerWidth - bubbleWidth));
            int finalY = 100 + random.nextInt(Math.max(1, containerHeight - bubbleHeight - 200));

            // Create flowing animation
            AnimatorSet flowSet = new AnimatorSet();

            // Movement animation - smooth flow upward
            ObjectAnimator moveX = ObjectAnimator.ofFloat(bubbleView, "x", bubbleView.getX(), finalX);
            ObjectAnimator moveY = ObjectAnimator.ofFloat(bubbleView, "y", bubbleView.getY(), finalY);

            // Scale animation - grow as it flows
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 0.3f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 0.3f, 1.1f, 1f);

            // Fade in animation
            ObjectAnimator alpha = ObjectAnimator.ofFloat(bubbleView, "alpha", 0f, 1f);

            // Rotation for more natural flow
            ObjectAnimator rotation = ObjectAnimator.ofFloat(bubbleView, "rotation", 0f, 360f * (random.nextFloat() - 0.5f));

            flowSet.playTogether(moveX, moveY, scaleX, scaleY, alpha, rotation);
            flowSet.setDuration(1500 + random.nextInt(1000)); // 1.5-2.5 seconds
            flowSet.setInterpolator(new AccelerateDecelerateInterpolator());

            flowSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Start floating animation after flow is complete
                    if (SettingsActivity.isPhysicsEnabled(MainActivity.this)) {
                        startFloatingAnimation(bubbleView);
                    }

                    // Add subtle pulsing effect
                    startPulsingAnimation(bubbleView);
                }
            });

            flowSet.start();
        });
    }

    private void startPulsingAnimation(View bubbleView) {
        ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(bubbleView, "alpha",
                bubbleView.getAlpha(), bubbleView.getAlpha() * 0.7f, bubbleView.getAlpha());
        pulseAnimator.setDuration(3000 + random.nextInt(2000));
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
    }

    private void startFloatingAnimation(View bubbleView) {
        if (!SettingsActivity.isPhysicsEnabled(this)) {
            return;
        }

        int animationSpeed = SettingsActivity.getAnimationSpeed(this);
        int duration = 3000 - (animationSpeed * 20);

        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(bubbleView, "translationY", 0f, -30f, 0f);
        floatAnimator.setDuration(Math.max(1500, duration));
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.start();

        bubbleView.setTag(R.id.float_animator, floatAnimator);
    }

    private void setupBubbleInteractions(TextView bubbleView, BubbleTask bubbleTask) {
        bubbleView.setOnClickListener(v -> showTaskDetails(bubbleTask, bubbleView));

        bubbleView.setOnLongClickListener(v -> {
            vibrateIfEnabled(50);
            showTaskOptions(bubbleTask, bubbleView);
            return true;
        });

        makeBubbleDraggable(bubbleView);
    }

    private void makeBubbleDraggable(View bubbleView) {
        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private boolean isDragging = false;
            private long startTime;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTime = System.currentTimeMillis();
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        isDragging = false;

                        // Highlight bubble when touched
                        view.setScaleX(1.05f);
                        view.setScaleY(1.05f);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (System.currentTimeMillis() - startTime > 100) {
                            isDragging = true;

                            // Calculate new position
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            // Keep bubble within screen bounds
                            int containerWidth = bubbleContainer.getWidth();
                            int containerHeight = bubbleContainer.getHeight();

                            newX = Math.max(0, Math.min(newX, containerWidth - view.getWidth()));
                            newY = Math.max(0, Math.min(newY, containerHeight - view.getHeight()));

                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        // Return to normal size
                        view.setScaleX(1f);
                        view.setScaleY(1f);

                        if (!isDragging && System.currentTimeMillis() - startTime < 500) {
                            view.performClick();
                        }

                        if (isDragging) {
                            // Add settling animation
                            ObjectAnimator settle = ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), 0f);
                            settle.setDuration(300);
                            settle.setInterpolator(new DecelerateInterpolator());
                            settle.start();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void showTaskDetails(BubbleTask bubbleTask, View bubbleView) {
        soundManager.playClickSound();

        AnimatorSet pulseSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 1f, 1.15f, 1f);
        pulseSet.playTogether(scaleX, scaleY);
        pulseSet.setDuration(200);
        pulseSet.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎈 " + bubbleTask.getCategory().getName());
        builder.setMessage(bubbleTask.getText());
        builder.setPositiveButton("Complete", (dialog, which) -> completeBubbleTask(bubbleView, bubbleTask));
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showTaskOptions(BubbleTask bubbleTask, TextView bubbleView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bubble Options");

        String[] options = {"Complete Task", "Pin to Top", "Delete", "Edit"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: completeBubbleTask(bubbleView, bubbleTask); break;
                case 1: pinBubbleToTop(bubbleView); break;
                case 2: deleteBubbleTask(bubbleView, bubbleTask); break;
                case 3: editBubbleTask(bubbleTask, bubbleView); break;
            }
        });

        builder.show();
    }

    private void completeBubbleTask(View bubbleView, BubbleTask bubbleTask) {
        SettingsActivity.incrementTasksCompleted(this);

        ObjectAnimator floatAnimator = (ObjectAnimator) bubbleView.getTag(R.id.float_animator);
        if (floatAnimator != null) {
            floatAnimator.cancel();
        }

        AnimatorSet completionSet = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(bubbleView, "alpha", bubbleView.getAlpha(), 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 1f, 2f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 1f, 2f, 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(bubbleView, "rotation", 0f, 720f);

        completionSet.playTogether(alpha, scaleX, scaleY, rotation);
        completionSet.setDuration(800);
        completionSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bubbleContainer.removeView(bubbleView);
                bubbleTasks.remove(bubbleTask);
                Toast.makeText(MainActivity.this, "🎉 Task completed!", Toast.LENGTH_SHORT).show();
            }
        });
        completionSet.start();

        vibrateIfEnabled(100);
    }

    private void pinBubbleToTop(View bubbleView) {
        ObjectAnimator moveY = ObjectAnimator.ofFloat(bubbleView, "y", bubbleView.getY(), 100);
        moveY.setDuration(800);
        moveY.setInterpolator(new DecelerateInterpolator());
        moveY.start();

        Toast.makeText(this, "📌 Bubble pinned to top!", Toast.LENGTH_SHORT).show();
    }

    private void deleteBubbleTask(View bubbleView, BubbleTask bubbleTask) {
        SettingsActivity.incrementBubblesBurst(this);

        AnimatorSet burstSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 1f, 3f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 1f, 3f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(bubbleView, "alpha", bubbleView.getAlpha(), 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(bubbleView, "rotation", 0f, 1080f);

        burstSet.playTogether(scaleX, scaleY, alpha, rotation);
        burstSet.setDuration(500);
        burstSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bubbleContainer.removeView(bubbleView);
                bubbleTasks.remove(bubbleTask);
                Toast.makeText(MainActivity.this, "💥 Bubble burst!", Toast.LENGTH_SHORT).show();
            }
        });
        burstSet.start();

        vibrateIfEnabled(50);
    }

    private void editBubbleTask(BubbleTask bubbleTask, TextView bubbleView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bubble Task");

        EditText editText = new EditText(this);
        editText.setText(bubbleTask.getText());
        builder.setView(editText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = editText.getText().toString().trim();
            if (!newText.isEmpty()) {
                bubbleTask.setText(newText);
                bubbleView.setText(newText);

                AnimatorSet updateSet = new AnimatorSet();
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 1f, 1.2f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 1f, 1.2f, 1f);
                updateSet.playTogether(scaleX, scaleY);
                updateSet.setDuration(300);
                updateSet.start();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void triggerBurstMode(MotionEvent e1, MotionEvent e2) {
        Toast.makeText(this, "💥 Burst Mode Activated!", Toast.LENGTH_SHORT).show();
        soundManager.playBurstSound();

        for (int i = bubbleContainer.getChildCount() - 1; i >= 0; i--) {
            View child = bubbleContainer.getChildAt(i);
            if (child instanceof TextView && child.getTag() instanceof BubbleTask) {
                float bubbleX = child.getX() + child.getWidth() / 2;
                float bubbleY = child.getY() + child.getHeight() / 2;

                if (isPointInSwipePath(bubbleX, bubbleY, e1, e2)) {
                    new Handler().postDelayed(() -> {
                        deleteBubbleTask(child, (BubbleTask) child.getTag());
                    }, random.nextInt(300));
                }
            }
        }

        vibrateIfEnabled(200);
    }

    private boolean isPointInSwipePath(float x, float y, MotionEvent e1, MotionEvent e2) {
        float A = e2.getY() - e1.getY();
        float B = e1.getX() - e2.getX();
        float C = e2.getX() * e1.getY() - e1.getX() * e2.getY();

        float distance = Math.abs(A * x + B * y + C) / (float) Math.sqrt(A * A + B * B);
        return distance < 100;
    }

    private void vibrateIfEnabled(int duration) {
        if (SettingsActivity.isVibrationEnabled(this) && vibrator != null) {
            vibrator.vibrate(duration);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        long sessionEndTime = System.currentTimeMillis();
        long sessionDuration = sessionEndTime - sessionStartTime;
        SettingsActivity.updateSessionTime(this, sessionDuration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionStartTime = System.currentTimeMillis();

        // Update sound settings when resuming
        soundManager.setSoundEnabled(SettingsActivity.isSoundEnabled(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release sound resources
        if (soundManager != null) {
            soundManager.release();
        }
    }

    // Inner class for BubbleTask
    public static class BubbleTask {
        private String text;
        private TaskCategory category;
        private boolean isPinned;

        public BubbleTask(String text, TaskCategory category) {
            this.text = text;
            this.category = category;
            this.isPinned = false;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public TaskCategory getCategory() { return category; }
        public boolean isPinned() { return isPinned; }
        public void setPinned(boolean pinned) { isPinned = pinned; }
    }
}