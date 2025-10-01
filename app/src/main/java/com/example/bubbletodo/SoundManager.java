package com.example.bubbletodo;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private Context context;
    private boolean soundEnabled = true;

    // Sound IDs
    private int popSoundId;
    private int burstSoundId;
    private int completeSoundId;
    private int addSoundId;
    private int clickSoundId;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initSoundPool();
        loadSounds();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    private void initSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
    }

    private void loadSounds() {
        // Try to load actual sound files from res/raw/
        // If they don't exist, set IDs to -1 to prevent crashes
        try {
            popSoundId = soundPool.load(context, R.raw.pop_sound, 1);
        } catch (Exception e) {
            popSoundId = -1;
        }

        try {
            burstSoundId = soundPool.load(context, R.raw.burst_sound, 1);
        } catch (Exception e) {
            burstSoundId = -1;
        }

        try {
            completeSoundId = soundPool.load(context, R.raw.complete_sound, 1);
        } catch (Exception e) {
            completeSoundId = -1;
        }

        try {
            addSoundId = soundPool.load(context, R.raw.add_sound, 1);
        } catch (Exception e) {
            addSoundId = -1;
        }

        try {
            clickSoundId = soundPool.load(context, R.raw.click_sound, 1);
        } catch (Exception e) {
            clickSoundId = -1;
        }
    }

    public void playPopSound() {
        if (soundEnabled && popSoundId != -1) {
            soundPool.play(popSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void playBurstSound() {
        if (soundEnabled && burstSoundId != -1) {
            soundPool.play(burstSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void playCompleteSound() {
        if (soundEnabled && completeSoundId != -1) {
            soundPool.play(completeSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void playAddSound() {
        if (soundEnabled && addSoundId != -1) {
            soundPool.play(addSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void playClickSound() {
        if (soundEnabled && clickSoundId != -1) {
            soundPool.play(clickSoundId, 0.5f, 0.5f, 0, 0, 1.0f);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}