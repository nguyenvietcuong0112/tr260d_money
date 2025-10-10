package com.budgettracker.personalfinance.trackmoney.smartbudget.utils;

import android.view.MotionEvent;
import android.view.View;

public class AssistiveTouch implements View.OnTouchListener {
    private float dX;
    private float dY;
    private float startX;
    private float startY;
    private boolean isMoving = false;
    private static final float CLICK_THRESHOLD = 10f;
    private float newX;
    private float newY;
    private int screenWidth;
    private int screenHeight;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                startX = event.getRawX();
                startY = event.getRawY();
                isMoving = false;
                screenWidth = view.getRootView().getWidth();
                screenHeight = view.getRootView().getHeight();
                return true;

            case MotionEvent.ACTION_MOVE:
                newX = event.getRawX() + dX;
                newY = event.getRawY() + dY;

                newX = Math.max(0, Math.min(screenWidth - view.getWidth(), newX));
                newY = Math.max(0, Math.min(screenHeight - view.getHeight(), newY));

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();

                if (Math.abs(event.getRawX() - startX) > CLICK_THRESHOLD ||
                        Math.abs(event.getRawY() - startY) > CLICK_THRESHOLD) {
                    isMoving = true;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (!isMoving) {
                    view.performClick();
                }

                int marginPx = (int) (10 * view.getResources().getDisplayMetrics().density);
                float finalX = newX > screenWidth / 2
                        ? screenWidth - view.getWidth() - marginPx
                        : marginPx;

                view.animate()
                        .x(finalX)
                        .setDuration(200)
                        .start();
                return true;

            default:
                return false;
        }
    }
}