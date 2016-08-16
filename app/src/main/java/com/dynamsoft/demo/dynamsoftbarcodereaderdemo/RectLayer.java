package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/5/25.
 */
public class RectLayer extends ImageView {
    public RectLayer(Context context) {
        this(context, null);
    }

    public RectLayer(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RectLayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        //this.setAlpha(0.3f);
    }

    private Rect rect;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = getWidth();
        int height = getHeight();
        rect = new Rect(width/12, height/5, width * 11 / 12, height *7 /10);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setAlpha(76);
        canvas.drawRect(0, 0, width, rect.top, p);
        canvas.drawRect(0, rect.bottom, width, height, p);
        canvas.drawRect(0, rect.top, rect.left, rect.bottom, p);
        canvas.drawRect(rect.right, rect.top, width, rect.bottom, p);

        p.setColor(Color.RED);
        p.setAlpha(255);
        p.setStrokeWidth(3);
        int lineVerticalPos = rect.top + (rect.height() >> 1);
        canvas.drawLine(rect.left + 10, lineVerticalPos, rect.right - 10, lineVerticalPos, p);

        p.setColor(Color.WHITE);
        p.setStrokeWidth(3);
        float[] vertice = {rect.left, rect.top, rect.right, rect.top,
                rect.right, rect.top, rect.right, rect.bottom,
                rect.right, rect.bottom, rect.left, rect.bottom,
                rect.left, rect.top, rect.left, rect.bottom};
        canvas.drawLines(vertice, p);

        p.setARGB(255, 255, 141, 22);
        p.setStrokeWidth(5);
        canvas.drawLine(rect.left, rect.top, rect.left + 40, rect.top, p);
        canvas.drawLine(rect.right - 40, rect.top, rect.right, rect.top, p);
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + 40, p);
        canvas.drawLine(rect.right, rect.bottom - 40, rect.right, rect.bottom, p);
        canvas.drawLine(rect.right - 40, rect.bottom, rect.right, rect.bottom, p);
        canvas.drawLine(rect.left, rect.bottom, rect.left + 40, rect.bottom, p);
        canvas.drawLine(rect.left, rect.bottom - 40, rect.left, rect.bottom, p);
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + 40, p);
    }

}
