package com.example.beperfect;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.CountDownTimer;


import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import java.util.ArrayList;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.widget.Toast;


public class PomodoroTaskView extends LinearLayout {

    private String taskName;
    private int workDurationMs;
    private int breakDurationMs;
    private int longBreakDurationMs;
    private int pomodoroCount;


    private Paint particlePaint;
    private ArrayList<PointF> particles = new ArrayList<>();
    private ValueAnimator confettiAnimator;
    private boolean isConfettiRunning = false;
    private boolean isNotificationEnabled = true;


    private LinearLayout pomodoroContainer;
    private TextView taskTitle;
    private TextView timerText;
    private ProgressBar timerProgress;
    private Button controlButton;
    private boolean isSoundEnabled = true;


    private CountDownTimer currentTimer;
    private boolean isTimerRunning = false;
    private int currentPomodoroIndex = 0;
    private boolean isWorkPhase = true;
    private long remainingTimeMs = 0;
    private LinearLayout currentTimerContainer;


    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    // =================================================//
//
//  КОНСТРУКТОРЫ И ИНИЦИАЛИЗАЦИЯ
//
// =================================================//

    public PomodoroTaskView(Context context) {
        super(context);
        init(context);
    }

    public PomodoroTaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        setBackgroundResource(R.drawable.pomodoro_background);

        taskTitle = new TextView(context);
        taskTitle.setTextSize(20);
        taskTitle.setTextColor(Color.BLACK);
        taskTitle.setGravity(Gravity.CENTER);
        addView(taskTitle);

        particlePaint = new Paint();
        particlePaint.setColor(Color.GREEN);
        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setAntiAlias(true);

        pomodoroContainer = new LinearLayout(context);
        pomodoroContainer.setOrientation(VERTICAL);
        addView(pomodoroContainer);
    }

// =================================================//
//
//  НАСТРОЙКА И УПРАВЛЕНИЕ ЗАДАЧАМИ
//
// =================================================//

    public void setNotificationEnabled(boolean enabled) {
        this.isNotificationEnabled = enabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.isSoundEnabled = enabled;
    }

    public void setup_task(String taskName, int workDurationMin, int breakDurationMin,
                           int longBreakDurationMin, int pomodoroCount) {
        try {
            // 1. Сброс состояния таймера
            reset_state();

            // 2. Установка параметров задачи
            this.taskName = taskName;
            this.workDurationMs = workDurationMin * 60 * 1000;
            this.breakDurationMs = breakDurationMin * 60 * 1000;
            this.longBreakDurationMs = longBreakDurationMin * 60 * 1000;
            this.pomodoroCount = pomodoroCount;

            // 3. Обновление UI
            taskTitle.setText(taskName);
            pomodoroContainer.removeAllViews();

            // 4. Скрытие цитаты (главное изменение)
            if (getContext() instanceof Activity) {
                Activity activity = (Activity) getContext();
                LinearLayout quoteContainer = activity.findViewById(R.id.quote_container);
                if (quoteContainer != null) {
                    activity.runOnUiThread(() -> quoteContainer.setVisibility(View.GONE));
                }
            }

            // 5. Создание блоков помидоров
            for (int i = 0; i < pomodoroCount; i++) {
                add_pomodoro_block(true, false);
                if (i < pomodoroCount - 1) {
                    add_pomodoro_block(false, false);
                }
            }

            // 6. Настройка таймера для первого блока
            if (pomodoroCount > 0) {
                setup_timer_controls_for_first_Block();
            }

        } catch (Exception e) {
            Log.e("PomodoroTaskView", "Error in setup_task: " + e.getMessage(), e);
            // Можно добавить Toast или другой способ показа ошибки
            if (getContext() != null) {
                Toast.makeText(getContext(), "Ошибка создания задачи: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reset_state() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        isTimerRunning = false;
        currentPomodoroIndex = 0;
        isWorkPhase = true;
        remainingTimeMs = 0;
        currentTimerContainer = null;
    }

// =================================================//
//
//  ОТРИСОВКА БЛОКОВ
//
// =================================================//

    private void add_pomodoro_block(boolean isWorkBlock, boolean isCompleted) {
        LinearLayout blockContainer = new LinearLayout(getContext());
        blockContainer.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(8);
        blockContainer.setLayoutParams(params);

        View block = new View(getContext());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(8));
        shape.setColor(isCompleted ?
                (isWorkBlock ? Color.GREEN : Color.BLUE) :
                Color.GRAY);
        block.setBackground(shape);

        LayoutParams blockParams = new LayoutParams(
                dpToPx(30),
                isWorkBlock ? dpToPx(100) : dpToPx(50)
        );
        blockParams.setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        block.setLayoutParams(blockParams);

        blockContainer.addView(block);

        if (!isCompleted && currentTimerContainer == null) {
            setup_timer_controls(blockContainer);
        }

        pomodoroContainer.addView(blockContainer);
    }

// =================================================//
//
//  УПРАВЛЕНИЕ ТАЙМЕРОМ
//
// =================================================//

    private void start_timer(long duration) {
        if (currentTimer != null) {
            currentTimer.cancel();
        }

        currentTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;
                update_timer_UI(millisUntilFinished, duration);
            }

            @Override
            public void onFinish() {
                remainingTimeMs = 0;
                show_timer_complete_dialog();
            }
        }.start();

        isTimerRunning = true;
        update_control_button_icon();
    }

    public void pause_timer() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        isTimerRunning = false;
        update_control_button_icon();
    }

    private void toggle_timer() {
        if (isTimerRunning) {
            pause_timer();
        } else {
            long duration = remainingTimeMs > 0 ? remainingTimeMs :
                    (isWorkPhase ? workDurationMs :
                            ((currentPomodoroIndex + 1) % 4 == 0 ? longBreakDurationMs : breakDurationMs));
            start_timer(duration);
        }
    }

// =================================================//
//
//  ОБНОВЛЕНИЕ ИНТЕРФЕЙСА
//
// =================================================//

    private void update_timer_UI(long millisUntilFinished, long totalDuration) {
        int seconds = (int) (millisUntilFinished / 1000);
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String timeLeft = String.format("%02d:%02d:%02d", hours, minutes, secs);
        if (timerText != null) {
            timerText.setText(timeLeft);
        }

        int progress = (int) (millisUntilFinished * 100 / totalDuration);
        if (timerProgress != null) {
            timerProgress.setProgress(progress);
        }
    }

    private void update_control_button_icon() {
        if (controlButton != null) {
            Drawable icon = ContextCompat.getDrawable(getContext(),
                    isTimerRunning ? R.drawable.ic_pause : R.drawable.ic_play);

            int iconSize = dpToPx(24);
            icon.setBounds(0, 0, iconSize, iconSize);
            controlButton.setCompoundDrawables(icon, null, null, null);
        }
    }

// =================================================//
//
//  АНИМАЦИИ И ЭФФЕКТЫ
//
// =================================================//

    private void animate_block_color(View block, int targetColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(8));
        block.setBackground(shape);

        ValueAnimator colorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.TRANSPARENT,
                targetColor
        );

        colorAnimator.setDuration(1000);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        colorAnimator.addUpdateListener(animator -> {
            GradientDrawable animShape = new GradientDrawable();
            animShape.setShape(GradientDrawable.RECTANGLE);
            animShape.setCornerRadius(dpToPx(8));
            animShape.setColor((int) animator.getAnimatedValue());
            block.setBackground(animShape);
        });

        colorAnimator.start();
    }

    private void show_confetti_animation(View anchorView, boolean isWorkPhase) {
        ViewGroup rootView = (ViewGroup) getRootView();
        if (rootView == null) return;

        ConfettiView confettiView = new ConfettiView(getContext());
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int[] rootLocation = new int[2];
        rootView.getLocationOnScreen(rootLocation);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                anchorView.getWidth() + 300,
                anchorView.getHeight() + 300
        );
        params.leftMargin = location[0] - rootLocation[0] - 150;
        params.topMargin = location[1] - rootLocation[1] - 150;

        rootView.addView(confettiView, params);
        confettiView.startParticleAnimation(isWorkPhase, () -> {
            rootView.removeView(confettiView);
        });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isConfettiRunning) {
            for (PointF particle : particles) {
                particlePaint.setColor(Color.argb(200,
                        (int)(Math.random() * 255),
                        (int)(Math.random() * 255),
                        (int)(Math.random() * 255)));
                canvas.drawCircle(particle.x, particle.y, 8, particlePaint);
                particle.y -= 10;
                particle.x += (Math.random() - 0.5) * 5;
            }
        }
    }

// =================================================//
//
//  ДИАЛОГИ И УВЕДОМЛЕНИЯ
//
// =================================================//

    private void show_timer_complete_dialog() {
        play_alarm_sound();

        if (isNotificationEnabled) {
            showNotification();
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_timer_complete);
        dialog.setCancelable(false);

        Button doneButton = dialog.findViewById(R.id.dialog_button);
        doneButton.setOnClickListener(v -> {
            stop_alarm_sound();
            dialog.dismiss();
            continue_after_dialog();
        });

        dialog.setOnDismissListener(d -> {
            stop_alarm_sound();
        });
        dialog.show();
    }

    private void showNotification() {
        Context context = getContext();
        if (context == null) return;

        String channelId = "pomodoro_channel";
        String channelName = "Pomodoro Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Pomodoro Timer")
                .setContentText("Таймер вышел!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e("Pomodoro", "Ошибка показа уведомления", e);
        }
    }


    private void play_alarm_sound() {
        if (!isSoundEnabled) return;

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.alarm_sound);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 1000};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception e) {
            Log.e("Pomodoro", "Ошибка воспроизведения звука", e);
        }
    }

    private void stop_alarm_sound() {
        if (!isSoundEnabled) return;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("Pomodoro", "Ошибка остановки медиаплеера", e);
            }
            mediaPlayer = null;
        }
        if (vibrator != null) {
            try {
                vibrator.cancel();
            } catch (Exception e) {
                Log.e("Pomodoro", "Ошибка остановки вибрации", e);
            }
        }
    }

// =================================================//
//
//  ЛОГИКА ПОМОДОРО
//
// =================================================//

    private void proceed_to_next_phase() {
        remove_all_timer_views();
        int blockIndex = get_current_block_index();
        if (blockIndex < pomodoroContainer.getChildCount()) {
            View block = ((LinearLayout)pomodoroContainer.getChildAt(blockIndex)).getChildAt(0);
            int targetColor = isWorkPhase ?
                    ContextCompat.getColor(getContext(), R.color.green) :
                    ContextCompat.getColor(getContext(), R.color.blue);
            animate_block_color(block, targetColor);
            show_confetti_animation(block, isWorkPhase);
        }

        // Для типа "нет" просто завершаем задачу
        if (pomodoroCount == 1 && workDurationMs > 0 && breakDurationMs == 0) {
            currentPomodoroIndex++;
            return;
        }

        if (isWorkPhase) {
            if (currentPomodoroIndex < pomodoroCount - 1) {
                isWorkPhase = false;
            } else {
                isWorkPhase = true;
                currentPomodoroIndex++;
            }
        } else {
            isWorkPhase = true;
            currentPomodoroIndex++;
        }

        if (hasNextPhase()) {
            int nextIndex = get_current_block_index();
            if (nextIndex < pomodoroContainer.getChildCount()) {
                setup_timer_controls((LinearLayout) pomodoroContainer.getChildAt(nextIndex));
                start_timer(isWorkPhase ? workDurationMs :
                        (currentPomodoroIndex == pomodoroCount - 1 ? longBreakDurationMs : breakDurationMs));
            }
        }
    }

    private void continue_after_dialog() {
        if (remainingTimeMs == 0) {
            proceed_to_next_phase();
        } else {
            long duration = remainingTimeMs > 0 ? remainingTimeMs :
                    (isWorkPhase ? workDurationMs :
                            (currentPomodoroIndex == pomodoroCount - 1 ? longBreakDurationMs : breakDurationMs));
            start_timer(duration);
        }
    }

// =================================================//
//
//  ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
//
// =================================================//

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setup_timer_controls_for_first_Block() {
        if (pomodoroContainer.getChildCount() > 0) {
            setup_timer_controls((LinearLayout) pomodoroContainer.getChildAt(0));
            update_timer_UI(workDurationMs, workDurationMs);
        }
    }

    private void setup_timer_controls(LinearLayout blockContainer) {
        if (currentTimerContainer != null) {
            ((ViewGroup)currentTimerContainer.getParent()).removeView(currentTimerContainer);
        }

        currentTimerContainer = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.timer_controls, blockContainer, false);

        timerText = currentTimerContainer.findViewById(R.id.timer_text);
        controlButton = currentTimerContainer.findViewById(R.id.control_button);
        timerProgress = currentTimerContainer.findViewById(R.id.timer_progress);

        update_timer_UI(isWorkPhase ? workDurationMs :
                        (currentPomodoroIndex == pomodoroCount - 1 ? longBreakDurationMs : breakDurationMs),
                isWorkPhase ? workDurationMs :
                        (currentPomodoroIndex == pomodoroCount - 1 ? longBreakDurationMs : breakDurationMs));

        update_control_button_icon();
        controlButton.setOnClickListener(v -> toggle_timer());

        blockContainer.addView(currentTimerContainer);
    }

    private void remove_all_timer_views() {
        for (int i = 0; i < pomodoroContainer.getChildCount(); i++) {
            View child = pomodoroContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout blockContainer = (LinearLayout) child;
                if (blockContainer.getChildCount() > 1) {
                    blockContainer.removeViewAt(1);
                }
            }
        }
        currentTimerContainer = null;
    }

    private int get_current_block_index() {
        if (currentPomodoroIndex < pomodoroCount - 1) {
            return currentPomodoroIndex * 2 + (isWorkPhase ? 0 : 1);
        }
        return currentPomodoroIndex * 2;
    }

    private boolean hasNextPhase() {
        return currentPomodoroIndex < pomodoroCount;
    }
}