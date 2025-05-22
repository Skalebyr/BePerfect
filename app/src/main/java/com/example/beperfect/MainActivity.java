package com.example.beperfect;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class MainActivity extends AppCompatActivity {

    private Switch switchSound;
    private Switch switchNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

// ============================================================================================ //
    //
    // ОСНОВНЫЕ МЕТОДЫ ОКНА
    //
// ============================================================================================ //

    // для OnClick
    public void on_button_click(View view) {
        showTaskCreationDialog("Новая задача");
    }

    // создание, показа диалога
    private void showTaskCreationDialog(String text) {
        Dialog dialog = create_base_dialog();
        setup_dialog_content(dialog, text);
        dialog.show();
    }

    //базовый диалог, настройки анимации, размера
    private Dialog create_base_dialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.window_create_task);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    //Настраивает содержимое диалога
    private void setup_dialog_content(Dialog dialog, String initialText) {
        // обозначение
        EditText etInput = dialog.findViewById(R.id.et_input);
        NumberPicker hourPicker = dialog.findViewById(R.id.hour_picker);
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        // Чипы повторений
        ChipGroup repeatChipGroup = dialog.findViewById(R.id.chip_group_choice);
        // Чипы Pomodoro
        ChipGroup pomodoroChipGroup = dialog.findViewById(R.id.chip_group_choice_pomidoro);

        // Инициализация Switch
        switchSound = dialog.findViewById(R.id.switch1);
        switchNotification = dialog.findViewById(R.id.switch2);

        // Настройка фигни
        setup_number_pickers(hourPicker, minutePicker);

        // Настройка чипов
        setup_repeat_chips(repeatChipGroup, dialog);
        setup_pomidoro_chips(pomodoroChipGroup, dialog);

        // Настройка Switch
        setup_switch();

        // Настройка кнопки сохранения
        setup_save_button(btnSave, etInput, hourPicker, minutePicker,
                dialog, repeatChipGroup, pomodoroChipGroup);
    }

    //настройка выбора времени
    private void setup_number_pickers(NumberPicker hourPicker, NumberPicker minutePicker) {
        // Часы (0-23)
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(12); // По умолчанию 12:00
        hourPicker.setFormatter(value -> String.format("%02d", value));

        // Минуты (0-59)
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(0);
        minutePicker.setFormatter(value -> String.format("%02d", value));
    }

    //Настраиваем чипы повторений
    private void setup_repeat_chips(ChipGroup repeatChipGroup, Dialog dialog) {
        repeatChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = dialog.findViewById(checkedIds.get(0));
                selectedChip.setTag(selectedChip.getText());
            }
        });
        repeatChipGroup.check(R.id.chip1); // по умолчанию
    }

    //Настраиваем чипы Pomodoro
    private void setup_pomidoro_chips(ChipGroup pomodoroChipGroup, Dialog dialog) {
        pomodoroChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = dialog.findViewById(checkedIds.get(0));
                String selectedType = selectedChip.getText().toString();

                // Динамически меняем подсказку в EditText
                EditText etInput = dialog.findViewById(R.id.et_input);
                switch (selectedType) {
                    case "учёба":
                        etInput.setHint("Тема занятия");
                        break;
                    case "работа":
                        etInput.setHint("Проект/задача");
                    default:
                        etInput.setHint("Новая задача");
                }
            }
        });
    }

// ============================================================================================ //
    //
    // МЕТОДЫ ДЛЯ РАБОТЫ SWITCH ПЕРЕКЛЮЧАТЕЛЕЙ
    //
// ============================================================================================ //

    private void setup_switch() {
        if (switchSound != null && switchNotification != null) {
            configure_switch(switchSound, "Звук pomidoro");
            configure_switch(switchNotification, "Оповещение pomidoro");
        }
    }

    // Метод для настройки отдельного Switch
    private void configure_switch(Switch switchView, String switchName) {
        // устанавливаем начальные цвета
        update_switch(switchView, switchView.isChecked());

        // обработчик изменений
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            update_switch(switchView, isChecked);
        });
    }

    // Метод для обновления внешнего вида Switch
    private void update_switch(Switch switchView, boolean isChecked) {
        int trackColor = isChecked ? R.color.soft_pink_beige : R.color.fraze_grew;
        int thumbColor = isChecked ? R.color.white : R.color.text_secondary_dark;

        switchView.getTrackDrawable().setTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, trackColor))
        );
        switchView.getThumbDrawable().setTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, thumbColor))
        );
    }

// ============================================================================================ //
    //
    // МЕТОДЫ ДЛЯ РАБОТЫ С СОХРАНЕНИЕМ ЗАДАЧИ
    //
// ============================================================================================ //

    // Настраивает кнопкц сохранения задачи
    private void setup_save_button(Button btnSave, EditText etInput,
                                   NumberPicker hourPicker, NumberPicker minutePicker,
                                   Dialog dialog, ChipGroup repeatChipGroup,
                                   ChipGroup pomodoroChipGroup) {
        btnSave.setOnClickListener(v -> {
            if (valide_input_task(etInput)) {
                save_to_data(
                        etInput.getText().toString().trim(),
                        hourPicker.getValue(),
                        minutePicker.getValue(),
                        get_select_chip_text(repeatChipGroup, dialog),
                        get_select_chip_text(pomodoroChipGroup, dialog)
                );
                dialog.dismiss();
            }
        });
    }


    // проверка на аутизм
    private boolean valide_input_task(EditText etInput) {
        if (etInput.getText().toString().trim().isEmpty()) {
            etInput.setError("Введите текст задачи");
            return false;
        }
        return true;
    }

    //получаем текст выбранного чипа
    private String get_select_chip_text(ChipGroup chipGroup, Dialog dialog) {
        int selectedId = chipGroup.getCheckedChipId();
        Chip selectedChip = dialog.findViewById(selectedId);
        return selectedChip != null ? selectedChip.getText().toString() : "";
    }

    // сейвим данные задачи
    private void save_to_data(String taskText, int hours, int minutes,
                              String repeatType, String pomodoroType) {
        // Конвертируем часы и минуты в минуты
        int totalMinutes = hours * 60 + minutes;
        int pomodoroCount;
        int workDuration;
        int breakDuration;
        int longBreakDuration;

        // Устанавливаем временные интервалы в зависимости от типа задачи
        switch (pomodoroType) {
            case "учёба":
                workDuration = 25;
                breakDuration = 5;
                longBreakDuration = 10;
                break;
            case "работа":
                workDuration = 45;
                breakDuration = 10;
                longBreakDuration = 17;
                break;
            case "прочее":
                workDuration = 30;
                breakDuration = 8;
                longBreakDuration = 14;
                break;
            case "нет":
                // Для типа "нет" создаем одну длинную задачу без перерывов
                createPomodoroTimer(taskText, totalMinutes, 0, 0, 1,
                        switchSound.isChecked(), switchNotification.isChecked());
                return;
            default:
                workDuration = 25;
                breakDuration = 5;
                longBreakDuration = 10;
        }

        pomodoroCount = Math.max(1, totalMinutes / workDuration);
        createPomodoroTimer(taskText, workDuration, breakDuration, longBreakDuration,
                pomodoroCount, switchSound.isChecked(), switchNotification.isChecked());
    }

    private int calculate_pomodoro_count(int totalMinutes, String pomodoroType) {
        // Базовая логика: 1 Pomodoro = 25 мин работы + 5 мин отдыха
        return (pomodoroType.equals("нет")) ? 0 : Math.max(1, totalMinutes / 25);
    }

    private void createPomodoroTimer(String taskName, int workDuration, int breakDuration,
                                     int longBreakDuration, int pomodoroCount,
                                     boolean isSoundEnabled, boolean isNotificationEnabled) {
        LinearLayout container = findViewById(R.id.pomodoro_container);
        if (container == null) return;

        PomodoroTaskView pomodoroView = new PomodoroTaskView(this);
        pomodoroView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        pomodoroView.setSoundEnabled(isSoundEnabled);
        pomodoroView.setNotificationEnabled(isNotificationEnabled);
        pomodoroView.setup_task(taskName, workDuration, breakDuration, longBreakDuration, pomodoroCount);
        container.addView(pomodoroView);

        // ===========================================
        // СОХРАНЕНИЕ В БД
        // ===========================================
    }
}