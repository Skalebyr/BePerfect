package com.example.beperfect;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private List<TaskData> taskList = new ArrayList<>();
    private BroadcastReceiver midnightReceiver;
    private Switch switchSound, switchNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        setupMidnightResetReceiver();
        loadSavedTasks();
    }

    // ======================================== //
    //
    // Инициализация интерфейса
    //
    // ======================================== //

    private void setupUI() {
    }

    public void onButtonClick(View view) {
        showTaskCreationDialog("Новая задача");
    }

    private void showTaskCreationDialog(String title) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.window_create_task);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etInput = dialog.findViewById(R.id.et_input);
        NumberPicker hourPicker = dialog.findViewById(R.id.hour_picker);
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);
        ChipGroup repeatChipGroup = dialog.findViewById(R.id.chip_group_choice);
        ChipGroup pomodoroChipGroup = dialog.findViewById(R.id.chip_group_choice_pomidoro);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        switchSound = dialog.findViewById(R.id.switch1);
        switchNotification = dialog.findViewById(R.id.switch2);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        repeatChipGroup.check(R.id.chip1);
        pomodoroChipGroup.check(R.id.pomidoro_chip1);

        btnSave.setOnClickListener(v -> {
            String taskName = etInput.getText().toString().trim();
            if (!taskName.isEmpty()) {
                createTaskFromInput(etInput, hourPicker, minutePicker, repeatChipGroup, pomodoroChipGroup);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // ======================================== //
    //
    // Обработка задач
    //
    // ======================================== //

    private void createTaskFromInput(EditText etInput, NumberPicker hourPicker,
                                     NumberPicker minutePicker, ChipGroup repeatChipGroup,
                                     ChipGroup pomodoroChipGroup) {
        String taskName = etInput.getText().toString().trim();
        int totalMinutes = hourPicker.getValue() * 60 + minutePicker.getValue();
        String repeatType = getSelectedChipText(repeatChipGroup);
        String pomodoroType = getSelectedChipText(pomodoroChipGroup);

        repeatType = convertRepeatType(repeatType);
        pomodoroType = convertPomodoroType(pomodoroType);

        int[] pomodoroParams = getPomodoroParams(pomodoroType, totalMinutes);

        createNewTask(taskName, pomodoroParams[0], pomodoroParams[1], pomodoroParams[2],
                pomodoroParams[3], switchSound.isChecked(),
                switchNotification.isChecked(), repeatType);
    }

    private void createNewTask(String taskName, int workDuration, int breakDuration,
                               int longBreakDuration, int pomodoroCount,
                               boolean soundEnabled, boolean notificationEnabled,
                               String repeatType) {
        try {
            TaskData newTask = new TaskData(taskName, workDuration, breakDuration,
                    longBreakDuration, pomodoroCount,
                    soundEnabled, notificationEnabled,
                    repeatType);
            taskList.add(newTask);
            addTaskToView(newTask);
            saveTasks();
        } catch (Exception e) {
            Log.e("MainActivity", "Error creating new task", e);
        }
    }

    private void addTaskToView(TaskData task) {
        LinearLayout container = findViewById(R.id.pomodoro_container);
        PomodoroTaskView pomodoroView = new PomodoroTaskView(this);

        pomodoroView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        pomodoroView.setSoundEnabled(task.isSoundEnabled());
        pomodoroView.setNotificationEnabled(task.isNotificationEnabled());
        pomodoroView.setup_task(task.getTaskName(),
                task.getWorkDuration(),
                task.getBreakDuration(),
                task.getLongBreakDuration(),
                task.getPomodoroCount());

        container.addView(pomodoroView);
    }

    // ======================================== //
    //
    // Обработка повторений
    //
    // ======================================== //

    private String getSelectedChipText(ChipGroup chipGroup) {
        int selectedId = chipGroup.getCheckedChipId();
        if (selectedId != View.NO_ID) {
            Chip chip = chipGroup.findViewById(selectedId);
            return chip.getText().toString();
        }
        return "";
    }

    private String convertRepeatType(String uiText) {
        switch (uiText) {
            case "без повторений": return "нет";
            case "каждый день": return "каждый день";
            case "через день": return "через день";
            case "еженедельно": return "раз в неделю";
            default: return "нет";
        }
    }

    private String convertPomodoroType(String uiText) {
        switch (uiText) {
            case "учёба": return "учёба";
            case "работа": return "работа";
            case "прочее": return "прочее";
            case "нет": return "нет";
            default: return "учёба";
        }
    }

    private int[] getPomodoroParams(String pomodoroType, int totalMinutes) {
        switch (pomodoroType) {
            case "учёба":
                return new int[]{25, 5, 10, Math.max(1, totalMinutes / 25)};
            case "работа":
                return new int[]{45, 10, 17, Math.max(1, totalMinutes / 45)};
            case "прочее":
                return new int[]{30, 8, 14, Math.max(1, totalMinutes / 30)};
            case "нет":
                return new int[]{totalMinutes, 0, 0, 1};
            default:
                return new int[]{25, 5, 10, Math.max(1, totalMinutes / 25)};
        }
    }

    // ======================================== //
    //
    // Обработка времени и сохранения
    //
    // ======================================== //

    private void setupMidnightResetReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        midnightReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkForMidnightReset();
            }
        };
        registerReceiver(midnightReceiver, filter);
    }

    private void checkForMidnightReset() {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.HOUR_OF_DAY) == 0 && now.get(Calendar.MINUTE) == 0) {
            resetDailyTasks();
        }
    }

    private void resetDailyTasks() {
        runOnUiThread(() -> {
            LinearLayout container = findViewById(R.id.pomodoro_container);
            container.removeAllViews();

            for (TaskData task : taskList) {
                if (shouldResetTask(task)) {
                    task.setLastResetDate(System.currentTimeMillis());
                    addTaskToView(task);
                } else {
                    taskList.remove(task);
                }
            }

            saveTasks();
        });
    }

    private boolean shouldResetTask(TaskData task) {
        Calendar now = Calendar.getInstance();
        switch (task.getRepeatType()) {
            case "каждый день":
                return true;
            case "через день":
                return daysSinceLastReset(task) >= 2;
            case "раз в неделю":
                return weeksSinceLastReset(task) >= 1;
            default:
                return false;
        }
    }

    private long daysSinceLastReset(TaskData task) {
        long diff = System.currentTimeMillis() - task.getLastResetDate();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }

    private long weeksSinceLastReset(TaskData task) {
        long diff = System.currentTimeMillis() - task.getLastResetDate();
        return TimeUnit.MILLISECONDS.toDays(diff) / 7;
    }

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("TasksPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("tasks", json);
        editor.apply();
    }

    private void loadSavedTasks() {
        SharedPreferences prefs = getSharedPreferences("TasksPrefs", MODE_PRIVATE);
        String json = prefs.getString("tasks", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<TaskData>>(){}.getType();
            taskList = gson.fromJson(json, type);

            for (TaskData task : taskList) {
                if (shouldResetTask(task)) {
                    task.setLastResetDate(System.currentTimeMillis());
                }
                addTaskToView(task);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (midnightReceiver != null) {
            unregisterReceiver(midnightReceiver);
        }
    }
}
