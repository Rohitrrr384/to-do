package com.example.bubbletodo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskManager {
    private static final String PREFS_NAME = "bubble_tasks";
    private static final String TASKS_KEY = "saved_tasks";

    private Context context;
    private SharedPreferences preferences;
    private Gson gson;
    private List<EnhancedBubbleTask> tasks;

    public TaskManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.tasks = new ArrayList<>();
        loadTasks();
    }

    public void addTask(EnhancedBubbleTask task) {
        tasks.add(task);
        saveTasks();
    }

    public void removeTask(EnhancedBubbleTask task) {
        tasks.remove(task);
        saveTasks();
    }

    public void updateTask(EnhancedBubbleTask task) {
        saveTasks();
    }

    public List<EnhancedBubbleTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<EnhancedBubbleTask> getTasksByCategory(MainActivity.TaskCategory category) {
        List<EnhancedBubbleTask> categoryTasks = new ArrayList<>();
        for (EnhancedBubbleTask task : tasks) {
            if (task.getCategory() == category) {
                categoryTasks.add(task);
            }
        }
        return categoryTasks;
    }

    public List<EnhancedBubbleTask> getPinnedTasks() {
        List<EnhancedBubbleTask> pinnedTasks = new ArrayList<>();
        for (EnhancedBubbleTask task : tasks) {
            if (task.isPinned()) {
                pinnedTasks.add(task);
            }
        }
        return pinnedTasks;
    }

    private void saveTasks() {
        String tasksJson = gson.toJson(tasks);
        preferences.edit().putString(TASKS_KEY, tasksJson).apply();
    }

    private void loadTasks() {
        String tasksJson = preferences.getString(TASKS_KEY, "");
        if (!tasksJson.isEmpty()) {
            Type listType = new TypeToken<List<EnhancedBubbleTask>>(){}.getType();
            tasks = gson.fromJson(tasksJson, listType);
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
        }
    }

    public void clearAllTasks() {
        tasks.clear();
        saveTasks();
    }

    // Enhanced BubbleTask with more features
    public static class EnhancedBubbleTask {
        private String text;
        private MainActivity.TaskCategory category;
        private boolean isPinned;
        private boolean isCompleted;
        private Date createdDate;
        private Date dueDate;
        private int priority; // 1-3 (1=low, 2=medium, 3=high)
        private String notes;
        private long reminderId;

        public EnhancedBubbleTask(String text, MainActivity.TaskCategory category) {
            this.text = text;
            this.category = category;
            this.isPinned = false;
            this.isCompleted = false;
            this.createdDate = new Date();
            this.priority = 2; // Default medium priority
            this.notes = "";
            this.reminderId = -1;
        }

        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public MainActivity.TaskCategory getCategory() { return category; }
        public void setCategory(MainActivity.TaskCategory category) { this.category = category; }
        public boolean isPinned() { return isPinned; }
        public void setPinned(boolean pinned) { isPinned = pinned; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public Date getCreatedDate() { return createdDate; }
        public Date getDueDate() { return dueDate; }
        public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public long getReminderId() { return reminderId; }
        public void setReminderId(long reminderId) { this.reminderId = reminderId; }

        public int getBubbleSize() {
            // Size based on priority: high=larger, low=smaller
            switch (priority) {
                case 3: return 200; // High priority
                case 2: return 160; // Medium priority
                case 1: return 130; // Low priority
                default: return 160;
            }
        }
    }
}