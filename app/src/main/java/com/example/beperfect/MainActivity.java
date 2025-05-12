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


    // создание и показ диалога

    private void showTaskCreationDialog(String text) {
        Dialog dialog = createBaseDialog();
        setupDialogContent(dialog, text);
        dialog.show();
    }


    // Создаём диалог, настройки анимации и размера

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

    /**
     * Настраивает содержимое диалога
     * @param dialog - экземпляр диалога
     * @param initialText - начальный текст задачи
     */
    private void setupDialogContent(Dialog dialog, String initialText) {

        // задаём значения
        ViewGroup contentView = (ViewGroup) dialog.findViewById(android.R.id.content);
        EditText etInput = dialog.findViewById(R.id.et_input);
        NumberPicker hourPicker = dialog.findViewById(R.id.hour_picker);
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        ChipGroup chipGroup = dialog.findViewById(R.id.chip_group_choice);

        // Настраиваем
        setupNumberPickers(hourPicker, minutePicker);
        setupChipSelection(chipGroup, dialog);
        setupInitialText(etInput, initialText);
        setupSaveButton(btnSave, etInput, hourPicker, minutePicker, dialog, chipGroup);
    }

    // Настройка времени
    private void setupNumberPickers(NumberPicker hourPicker, NumberPicker minutePicker) {
        // Часы
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(12);
        hourPicker.setFormatter(value -> String.format("%02d", value));

        // Минуты
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(0);
        minutePicker.setFormatter(value -> String.format("%02d", value));
    }

    /**
     * Настраивает выбор чипов для типа задачи
     */
    private void setupChipSelection(ChipGroup chipGroup, Dialog dialog) {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = dialog.findViewById(checkedIds.get(0));
                selectedChip.setTag(selectedChip.getText()); // Сохраняем выбранный
            }
        });
        // по умолчанию
        chipGroup.check(R.id.chip1);
    }

    // текст задачи
    private void setupInitialText(EditText etInput, String text) {
        if (text != null && !text.isEmpty()) {
            etInput.setText(text);
        }
    }

    // кнопка сохранения
    private void setupSaveButton(Button btnSave, EditText etInput,
                                 NumberPicker hourPicker, NumberPicker minutePicker,
                                 Dialog dialog, ChipGroup chipGroup) {
        btnSave.setOnClickListener(v -> {
            if (validateInput(etInput)) {
                saveTaskData(
                        etInput.getText().toString().trim(),
                        hourPicker.getValue(),
                        minutePicker.getValue(),
                        getSelectedChipText(chipGroup, dialog)
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

    // получаем текст выбранного чипа
    private String getSelectedChipText(ChipGroup chipGroup, Dialog dialog) {
        int selectedId = chipGroup.getCheckedChipId();
        Chip selectedChip = dialog.findViewById(selectedId);
        return selectedChip != null ? selectedChip.getText().toString() : "Soft";
    }

    // СОХРАНЕНИЕ В БД
    private void saveTaskData(String taskText, int hours, int minutes, String taskType) {
        // временное решение
        Log.d("TaskData", String.format(
                "Задача: %s, Время: %02d:%02d, Тип: %s",
                taskText, hours, minutes, taskType
        ));
    }
}