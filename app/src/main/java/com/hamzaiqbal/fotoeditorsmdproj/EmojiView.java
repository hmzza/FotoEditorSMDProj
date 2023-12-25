package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

public class EmojiView extends androidx.appcompat.widget.AppCompatImageView {
    private static final int INVALID_POINTER_ID = -1;
    private Drawable emoji;
    private float lastTouchX, lastTouchY;
    private int activePointerId = INVALID_POINTER_ID;
    private float scaleFactor = 1f;
    private float rotation = 0f;

    public EmojiView(Context context, Drawable emoji) {
        super(context);
        this.emoji = emoji;
        setImageDrawable(emoji);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = event.getActionIndex();
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                lastTouchX = x;
                lastTouchY = y;
                activePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(activePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                if (!isScaling(event)) {
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;

                    setX(getX() + dx);
                    setY(getY() + dy);
                }

                lastTouchX = x;
                lastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private boolean isScaling(MotionEvent event) {
        return event.getPointerCount() > 1;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
        canvas.rotate(rotation, getWidth() / 2f, getHeight() / 2f);
        emoji.setBounds(0, 0, getWidth(), getHeight());
        emoji.draw(canvas);
        canvas.restore();
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        invalidate();
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidate();
    }
}
