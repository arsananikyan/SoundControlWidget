package com.picsarttraining.soundcontrolwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Arsen on 29.03.2016.
 */
public class SoundControlView extends View {

    private static final double speed = 0.11;
    private int piecesCount;
    private Paint circlePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint backgroundCirclePaint;
    private int centerX;
    private int centerY;
    private int circleRadius;
    private double rotationSize;

    private double rotationDest;

    private OnStateChangedListener onStateChangedListener;

    public SoundControlView(Context context) {
        super(context);
    }

    public SoundControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SoundControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        circlePaint = new Paint();
        pointPaint = new Paint();
        backgroundCirclePaint = new Paint();

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SoundControlView,
                0, 0);
        try {
            circlePaint.setColor(Color.parseColor(a.getString(R.styleable.SoundControlView_circleColor)));
            pointPaint.setColor(Color.parseColor(a.getString(R.styleable.SoundControlView_pointerColor)));
            piecesCount = a.getInt(R.styleable.SoundControlView_piecesCount,10);
            backgroundCirclePaint.setColor(Color.parseColor(a.getString(R.styleable.SoundControlView_circleBackgroundColor)));
        }
        finally {
            a.recycle();
        }

        rotationSize = 0;
        rotationDest = Double.NaN;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*Drawing background circle*/
        canvas.drawCircle(centerX, centerY, (getMeasuredHeight()+getMeasuredWidth())/4, backgroundCirclePaint);

        /*Drawing circle*/
        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

        /*Clock dial*/
        int pieRotateSize = 360 / piecesCount;
        for (int i = 0; i < piecesCount; i++) {
            String text = i * 100 / piecesCount + "";
            canvas.drawText(text, centerX - 5, centerY - circleRadius - 5, textPaint);
            canvas.save();
            canvas.rotate(pieRotateSize, centerX, centerY);

        }
        canvas.save();
        canvas.rotate(pieRotateSize, centerX, centerY);
        canvas.restore();

        /*Drawing pointer*/
        RectF rect = new RectF(centerX - 10, centerY - circleRadius, centerX + 10, centerY - circleRadius + 50);
        canvas.rotate((int) Math.toDegrees(rotationSize), centerX, centerY);
        canvas.drawRect(rect, pointPaint);
        canvas.rotate(-(int) Math.toDegrees(rotationSize), centerX, centerY);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                double currentRotation = Math.atan(((double) event.getX() - centerX) / (centerY - event.getY()));
                if (event.getY() > centerY)
                    currentRotation = Math.PI + currentRotation;
                currentRotation = (currentRotation + 2 * Math.PI) % (2 * Math.PI);
                startAnimatedMoveIfNeeded(currentRotation);
                break;

            case MotionEvent.ACTION_UP:
                rotationDest = Double.NaN;
                return false;
            default:
                break;
        }
        return true;
    }

    private void startAnimatedMoveIfNeeded(double dest) {
        if (!Double.isNaN(rotationDest)) {
            rotationDest = dest;
            return;
        }
        rotationDest = dest;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Double.isNaN(rotationDest)) {
                    try {
                        if (Math.abs(rotationDest - rotationSize) > speed) {
                            if (rotationDest > rotationSize)
                                rotationSize += rotationDest - rotationSize < Math.PI ? speed : -speed;
                            else
                                rotationSize += rotationSize - rotationDest < Math.PI ? -speed : speed;
                            rotationSize = (rotationSize + Math.PI * 2) % (Math.PI * 2);
                        } else {
                            rotationSize = rotationDest;
                        }

                        post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                                if (onStateChangedListener != null)
                                    onStateChangedListener.onStateChanged(rotationSize);
                            }
                        });
                        Thread.sleep(1000 / 60);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        circleRadius = (getMeasuredHeight() + getMeasuredWidth()) / 4 - 40;

    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    public interface OnStateChangedListener {
        void onStateChanged(double rotationSize);
    }
}
