package com.example.beperfect;

import java.util.Calendar;

public class TaskData {
    private String taskName;
    private int workDuration;
    private int breakDuration;
    private int longBreakDuration;
    private int pomodoroCount;
    private boolean isSoundEnabled;
    private boolean isNotificationEnabled;
    private String repeatType;
    private long lastResetDate;

    // Конструктор и геттеры/сеттеры
    public TaskData(String taskName, int workDuration, int breakDuration,
                    int longBreakDuration, int pomodoroCount,
                    boolean isSoundEnabled, boolean isNotificationEnabled,
                    String repeatType) {
        this.taskName = taskName;
        this.workDuration = workDuration;
        this.breakDuration = breakDuration;
        this.longBreakDuration = longBreakDuration;
        this.pomodoroCount = pomodoroCount;
        this.isSoundEnabled = isSoundEnabled;
        this.isNotificationEnabled = isNotificationEnabled;
        this.repeatType = repeatType;
        this.lastResetDate = System.currentTimeMillis();
    }

    // Добавьте геттеры и сеттеры для всех полей
    public boolean shouldResetToday() {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastResetDate);
        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR) {
            switch (repeatType) {
                case "каждый день":
                    return true;
                case "через день":
                    return (now.get(Calendar.DAY_OF_YEAR) - lastCal.get(Calendar.DAY_OF_YEAR) >= 2;
                case "раз в неделю":
                    return now.get(Calendar.WEEK_OF_YEAR) != lastCal.get(Calendar.WEEK_OF_YEAR);
                default:
                    return false;
            }
        }
        return false;
    }
}