package com.example.beperfect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Random;

public class ConfettiView extends View {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private final Paint particlePaint = new Paint();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private ValueAnimator animator;
    private final Random random = new Random();

    // Настройки анимации
    private static final int TOTAL_PARTICLES = 20;
    private static final long ANIM_DURATION = 1500;
    private static final float MAX_DISTANCE_RATIO = 0.4f;

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setAntiAlias(true);
    }

    // Изменяем метод на нестатический
    public void startParticleAnimation(boolean isWorkPhase, Runnable onComplete) {
        // Очищаем предыдущую анимацию
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        particles.clear();

        post(() -> {
            // Проверяем, что View ещё прикреплена
            if (getWidth() == 0 || getHeight() == 0) {
                if (onComplete != null) onComplete.run();
                return;
            }

            // Создаем частицы
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float maxDistance = Math.min(getWidth(), getHeight()) * MAX_DISTANCE_RATIO;

            for (int i = 0; i < TOTAL_PARTICLES; i++) {
                particles.add(new Particle(
                        centerX, centerY, random, i, maxDistance, isWorkPhase
                ));
            }

            // Настраиваем аниматор
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(ANIM_DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());

            animator.addUpdateListener(animation -> {
                updateParticles(animation.getAnimatedFraction());
                invalidate();
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    post(() -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        removeSafe();
                    });
                }
            });

            animator.start();
        });
    }

    private void removeSafe() {
        post(() -> {
            if (getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
        });
    }

    private void updateParticles(float globalProgress) {
        for (Particle p : particles) {
            float delay = p.index * 0.05f;
            float particleProgress = Math.max(0, (globalProgress - delay) / (1f - delay));

            if (particleProgress <= 0.5f) {
                float explosionProgress = particleProgress * 2f;
                p.x = p.startX + p.directionX * explosionProgress * p.maxDistance;
                p.y = p.startY + p.directionY * explosionProgress * p.maxDistance;
            } else {
                float returnProgress = (particleProgress - 0.5f) * 2f;
                p.x = p.startX + (p.x - p.startX) * (1f - returnProgress);
                p.y = p.startY + (p.y - p.startY) * (1f - returnProgress);
            }

            p.alpha = (particleProgress <= 0.5f) ?
                    particleProgress * 2f :
                    (1f - (particleProgress - 0.5f) * 2f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) return;

        for (Particle p : particles) {
            particlePaint.setColor(p.color);
            particlePaint.setAlpha((int) (p.alpha * 255));
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }
    }

    private class Particle {
        float x, y;
        float startX, startY;
        float directionX, directionY;
        float radius = 10f;
        float alpha = 1f;
        float maxDistance;
        int color;
        int index;
        boolean isWorkParticle;

        Particle(float startX, float startY, Random random, int index,
                 float maxDistance, boolean isWorkParticle) {
            this.startX = startX;
            this.startY = startY;
            this.x = startX;
            this.y = startY;
            this.index = index;
            this.maxDistance = maxDistance;
            this.isWorkParticle = isWorkParticle;

            double angle = random.nextDouble() * Math.PI * 2;
            this.directionX = (float) Math.cos(angle);
            this.directionY = (float) Math.sin(angle);

            if (isWorkParticle) {
                this.color = Color.HSVToColor(new float[]{
                        90 + random.nextFloat() * 60,
                        0.6f,
                        0.95f
                });
            } else {
                this.color = Color.HSVToColor(new float[]{
                        180 + random.nextFloat() * 60,
                        0.5f,
                        0.97f
                });
            }

            this.radius = 8f + random.nextFloat() * 8f;
        }
    }
}