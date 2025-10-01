package com.example.bubbletodo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;

public class ThemeManager {
    private static final String PREFS_NAME = "bubble_themes";
    private static final String CURRENT_THEME_KEY = "current_theme";

    private Context context;
    private SharedPreferences preferences;
    private BubbleTheme currentTheme;

    public enum BubbleTheme {
        OCEAN("Ocean Bubbles",
                new int[]{Color.parseColor("#0077be"), Color.parseColor("#00a8cc"), Color.parseColor("#00d4ff")},
                new int[]{Color.parseColor("#006ba6"), Color.parseColor("#0096c7"), Color.parseColor("#00b4d8")}),

        SPACE("Space Bubbles",
                new int[]{Color.parseColor("#2d1b69"), Color.parseColor("#5b2c6f"), Color.parseColor("#8b3a8f")},
                new int[]{Color.parseColor("#240046"), Color.parseColor("#3c096c"), Color.parseColor("#5a189a")}),

        SUNSET("Sunset Bubbles",
                new int[]{Color.parseColor("#ff6b6b"), Color.parseColor("#ffa726"), Color.parseColor("#ffca28")},
                new int[]{Color.parseColor("#e53935"), Color.parseColor("#f57c00"), Color.parseColor("#fbc02d")}),

        FOREST("Forest Bubbles",
                new int[]{Color.parseColor("#2e7d32"), Color.parseColor("#388e3c"), Color.parseColor("#43a047")},
                new int[]{Color.parseColor("#1b5e20"), Color.parseColor("#2e7d32"), Color.parseColor("#388e3c")}),

        NEON("Neon Bubbles",
                new int[]{Color.parseColor("#e91e63"), Color.parseColor("#9c27b0"), Color.parseColor("#3f51b5")},
                new int[]{Color.parseColor("#c2185b"), Color.parseColor("#7b1fa2"), Color.parseColor("#303f9f")});

        private final String name;
        private final int[] primaryColors;
        private final int[] secondaryColors;

        BubbleTheme(String name, int[] primaryColors, int[] secondaryColors) {
            this.name = name;
            this.primaryColors = primaryColors;
            this.secondaryColors = secondaryColors;
        }

        public String getName() { return name; }
        public int[] getPrimaryColors() { return primaryColors; }
        public int[] getSecondaryColors() { return secondaryColors; }
    }

    public ThemeManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCurrentTheme();
    }

    public void setTheme(BubbleTheme theme) {
        this.currentTheme = theme;
        preferences.edit().putString(CURRENT_THEME_KEY, theme.name()).apply();
    }

    public BubbleTheme getCurrentTheme() {
        return currentTheme;
    }

    private void loadCurrentTheme() {
        String themeName = preferences.getString(CURRENT_THEME_KEY, BubbleTheme.OCEAN.name());
        try {
            currentTheme = BubbleTheme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            currentTheme = BubbleTheme.OCEAN;
        }
    }

    public GradientDrawable createThemedBubble(MainActivity.TaskCategory category, int size) {
        GradientDrawable bubble = new GradientDrawable();
        bubble.setShape(GradientDrawable.OVAL);

        int[] colors = currentTheme.getPrimaryColors();
        int[] secondaryColors = currentTheme.getSecondaryColors();

        // Select color based on category
        int colorIndex = category.ordinal() % colors.length;
        int primaryColor = colors[colorIndex];
        int secondaryColor = secondaryColors[colorIndex];

        // Create gradient
        bubble.setColors(new int[]{primaryColor, secondaryColor});
        bubble.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        bubble.setGradientRadius(size / 2f);

        // Add stroke
        bubble.setStroke(4, Color.WHITE);

        return bubble;
    }

    public int[] getBackgroundGradient() {
        switch (currentTheme) {
            case OCEAN:
                return new int[]{Color.parseColor("#667eea"), Color.parseColor("#764ba2")};
            case SPACE:
                return new int[]{Color.parseColor("#0c0c0c"), Color.parseColor("#1a1a2e")};
            case SUNSET:
                return new int[]{Color.parseColor("#ff7e5f"), Color.parseColor("#feb47b")};
            case FOREST:
                return new int[]{Color.parseColor("#134e5e"), Color.parseColor("#71b280")};
            case NEON:
                return new int[]{Color.parseColor("#000000"), Color.parseColor("#434343")};
            default:
                return new int[]{Color.parseColor("#667eea"), Color.parseColor("#764ba2")};
        }
    }
}