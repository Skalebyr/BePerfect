package com.example.beperfect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        create_task("Новая задача");
    }

    private void create_task(String text) {
        // 1. Создаем Dialog
        final Dialog dialog = new Dialog(this);

        // 2. Устанавливаем ваш layout
        dialog.setContentView(R.layout.window_create_task);

        // 3. Настраиваем прозрачный фон окна
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 4. Находим элементы (используем правильные ID!)
        EditText etInput = dialog.findViewById(R.id.et_input);  // Совпадает с XML
        NumberPicker hourPicker = dialog.findViewById(R.id.hour_picker);  // Совпадает с XML
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);  // Совпадает с XML
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // 5. Настройка NumberPicker
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(12); // Значение по умолчанию
        hourPicker.setFormatter(value -> String.format("%02d", value));

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(0);
        minutePicker.setFormatter(value -> String.format("%02d", value));

        // 6. Обработчики кнопок
        btnSave.setOnClickListener(v -> {
            String taskText = etInput.getText().toString();
            int hours = hourPicker.getValue();
            int minutes = minutePicker.getValue();

            // Здесь обработка сохранения
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 7. Показываем диалог
        dialog.show();
    }
}