package com.example.beperfect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class MainActivity extends AppCompatActivity {

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

    // для OnClick
    public void on_button_click(View view) {
        showTaskCreationDialog("Новая задача");
    }

    /**
     * Основной метод создания и показа диалога
     */
    private void showTaskCreationDialog(String text) {
        Dialog dialog = createBaseDialog();
        setupDialogContent(dialog, text);
        dialog.show();
    }

    //базовый диалог, настройки анимации, размера
    private Dialog createBaseDialog() {
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
    private void setupDialogContent(Dialog dialog, String initialText) {
        // обозначение
        EditText etInput = dialog.findViewById(R.id.et_input);
        NumberPicker hourPicker = dialog.findViewById(R.id.hour_picker);
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        // Чипы повторений
        ChipGroup repeatChipGroup = dialog.findViewById(R.id.chip_group_choice);
        // Чипы Pomodoro
        ChipGroup pomodoroChipGroup = dialog.findViewById(R.id.chip_group_choice_pomidoro);

        // Настройка фигни
        setupNumberPickers(hourPicker, minutePicker);

        // Настройка чипов
        setupRepeatChips(repeatChipGroup, dialog);
        setupPomodoroChips(pomodoroChipGroup, dialog);

        // Настройка кнопки сохранения
        setupSaveButton(btnSave, etInput, hourPicker, minutePicker,
                dialog, repeatChipGroup, pomodoroChipGroup);
    }


    //настройка выбора времени
    private void setupNumberPickers(NumberPicker hourPicker, NumberPicker minutePicker) {
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

    private void setupRepeatChips(ChipGroup repeatChipGroup, Dialog dialog) {
        repeatChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = dialog.findViewById(checkedIds.get(0));
                selectedChip.setTag(selectedChip.getText());
            }
        });
        repeatChipGroup.check(R.id.chip1); // по умолчанию
    }


    //Настраиваем чипы Pomodoro

    private void setupPomodoroChips(ChipGroup pomodoroChipGroup, Dialog dialog) {
        pomodoroChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = dialog.findViewById(checkedIds.get(0));
                selectedChip.setTag(selectedChip.getText());
            }
        });
        pomodoroChipGroup.check(R.id.pomidoro_chip4);
    }


     // Настраивает кнопкц сохранения задачи
    private void setupSaveButton(Button btnSave, EditText etInput,
                                 NumberPicker hourPicker, NumberPicker minutePicker,
                                 Dialog dialog, ChipGroup repeatChipGroup,
                                 ChipGroup pomodoroChipGroup) {
        btnSave.setOnClickListener(v -> {
            if (validateInput(etInput)) {
                saveTaskData(
                        etInput.getText().toString().trim(),
                        hourPicker.getValue(),
                        minutePicker.getValue(),
                        getSelectedChipText(repeatChipGroup, dialog),
                        getSelectedChipText(pomodoroChipGroup, dialog)
                );
                dialog.dismiss();
            }
        });
    }

    // проверка на аутизм
    private boolean validateInput(EditText etInput) {
        if (etInput.getText().toString().trim().isEmpty()) {
            etInput.setError("Введите текст задачи");
            return false;
        }
        return true;
    }

    //Получаем текст выбранного чипа
    private String getSelectedChipText(ChipGroup chipGroup, Dialog dialog) {
        int selectedId = chipGroup.getCheckedChipId();
        Chip selectedChip = dialog.findViewById(selectedId);
        return selectedChip != null ? selectedChip.getText().toString() : "";
    }

    // сейвим данные задачи
    private void saveTaskData(String taskText, int hours, int minutes,
                              String repeatType, String pomodoroType) {
        Log.d("SAVE_TASK", "Сохранение задачи:\n" +
                "Текст: " + taskText + "\n" +
                "Время: " + String.format("%02d:%02d", hours, minutes) + "\n" +
                "Повтор: " + repeatType + "\n" +
                "Pomodoro: " + pomodoroType);

        // ===========================================
        // СОХРАНЕНИЕ В БД
        // ===========================================
    }
}