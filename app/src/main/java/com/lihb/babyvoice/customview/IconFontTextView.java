package com.lihb.babyvoice.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.utils.IconFontUtil;


/**
 * Created by yuankai on 15/12/14.
 */
public class IconFontTextView extends android.support.v7.widget.AppCompatTextView {

    private Paint paint;
    private int strokeWidth;
    private int strokeColor;
    private int fillColor;
    private boolean enablePressEffect = true; // 按下半透明效果

    public IconFontTextView(Context context) {
        this(context, null);
    }

    public IconFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setTypeface(IconFontUtil.getIconfont());

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconFontTextView, defStyleAttr, 0);

        strokeColor = a.getColor(R.styleable.IconFontTextView_if_strokeColor, 0);
        strokeWidth = a.getDimensionPixelSize(R.styleable.IconFontTextView_if_strokeWidth, 0);
        fillColor = a.getColor(R.styleable.IconFontTextView_if_fillColor, 0);
        enablePressEffect = a.getBoolean(R.styleable.IconFontTextView_if_enablePressEffect, true);
        a.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        invalidate();
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (!isEnabled()) {
            return;
        }

        if (enablePressEffect) {
            if (pressed) {
                setAlpha(0.5F);
            } else {
                setAlpha(1);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setAlpha(1);
        } else {
            setAlpha(0.2F);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int center = getWidth() / 2;
        if (strokeColor != 0) {

            if (strokeWidth != 0) {
                this.paint.setStyle(Paint.Style.STROKE);
                this.paint.setStrokeWidth(strokeWidth);
            }

            this.paint.setColor(strokeColor);

            if (strokeWidth != -1) {
                canvas.drawCircle(center, center, getWidth() / 2 - strokeWidth / 2, this.paint);
            } else {
                canvas.drawCircle(center, center, getWidth() / 2, this.paint);
            }
        }

        if (fillColor != 0) {
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setColor(fillColor);
            canvas.drawCircle(center, center, getWidth() / 2, this.paint);
        }

        super.onDraw(canvas);
    }
}
