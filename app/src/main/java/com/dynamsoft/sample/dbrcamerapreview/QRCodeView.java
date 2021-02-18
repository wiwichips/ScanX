package com.dynamsoft.sample.dbrcamerapreview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class QRCodeView extends RelativeLayout {

    private int maskColor;
    private int boxViewWidth;
    private int boxViewHeight;
    private int cornerColor;
    private int borderColor;
    private int cornerSize;
    private int cornerLength;
    private int cornerOffset;

    private FrameLayout boxView;
    private TextView textView;
    private OnClickListener lightOnClickListener;

    public QRCodeView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.layout_qr_code_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QRCodeView, defStyleAttr, 0);
        Resources resources = getResources();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_maskColor, resources.getColor(R.color.qr_code_view_mask));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border));
        } else {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_mask, null));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner, null));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border, null));
        }

        cornerOffset = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerOffset, (int) resources.getDimension(R.dimen.size_qr_box_view_corner_offset));
        cornerLength = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerLength, (int) resources.getDimension(R.dimen.length_qr_box_view_corner));
        cornerSize = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerSize, (int) resources.getDimension(R.dimen.size_qr_box_view_corner));
        boxViewWidth = typedArray.getInt(R.styleable.QRCodeView_boxViewWidth, (int) resources.getDimension(R.dimen.width_qr_box_view));
        boxViewHeight = typedArray.getInt(R.styleable.QRCodeView_boxViewHeight, (int) resources.getDimension(R.dimen.height_qr_box_view));

        typedArray.recycle();
        boxView = findViewById(R.id.fl_box_view);
        textView = findViewById(R.id.tv_desc);
///        LayoutParams params = (LayoutParams) boxView.getLayoutParams();
//        params.width = boxViewWidth;
//        params.height = boxViewHeight;
//        boxView.setLayoutParams(params);
//        setBackgroundResource(R.color.qr_code_view_mask);
    }

    public void reSetboxview(int left, int top, int width, int height) {
        MarginLayoutParams margin = new MarginLayoutParams(boxView.getLayoutParams());
        margin.setMargins(left, top, left + width, top + height);
        boxViewWidth = width;
        boxViewHeight = height;
        LayoutParams layoutParams = new LayoutParams(margin);
        boxView.setLayoutParams(layoutParams);
        /// boxView.removeAllViews();
        // boxView.addView(mIvScanLine);
        setBackgroundResource(R.color.qr_code_view_mask);

    }

    @Override
    public void onDraw(Canvas canvas) {
        /** Draw the exterior dark mask*/
        int width = getWidth();
        int height = getHeight();
        float boxViewX = boxView.getX();
        float boxViewY = boxView.getY();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(maskColor);
        // left rect
        canvas.drawRect(0, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);
        // right rect
        canvas.drawRect(boxViewX + boxViewWidth, boxViewY, width, boxViewY + boxViewHeight, paint);
        // top rect
        canvas.drawRect(0, 0, width, boxViewY, paint);
        // bottom rect
        canvas.drawRect(0, boxViewY + boxViewHeight, width, height, paint);

        /** Draw the border lines*/
        paint.setColor(borderColor);
        canvas.drawLine(boxViewX, boxViewY, boxViewX + boxViewWidth, boxViewY, paint);
        canvas.drawLine(boxViewX, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX, boxViewY + boxViewHeight, paint);
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX + boxViewWidth, boxViewY, paint);

        /** Draw the corners*/
        Rect rect = new Rect();
        rect.set((int) boxViewX, (int) boxViewY, (int) boxViewX + boxViewWidth, (int) boxViewY + boxViewHeight);
        paint.setColor(cornerColor);

        /** top the corners*/
        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.top - cornerSize + cornerOffset, rect.left + cornerLength - cornerSize + cornerOffset, rect.top + cornerOffset, paint);
        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.top - cornerSize + cornerOffset, rect.left + cornerOffset, rect.top + cornerLength - cornerSize + cornerOffset, paint);
        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset, rect.top - cornerSize + cornerOffset, rect.right + cornerSize - cornerOffset, rect.top + cornerOffset, paint);
        canvas.drawRect(rect.right - cornerOffset, rect.top - cornerSize + cornerOffset, rect.right + cornerSize - cornerOffset, rect.top + cornerLength - cornerSize + cornerOffset, paint);

        /** bottom the corners*/
        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.bottom - cornerOffset, rect.left + cornerLength - cornerSize + cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.bottom - cornerLength + cornerSize - cornerOffset, rect.left + cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset, rect.bottom - cornerOffset, rect.right + cornerSize - cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
        canvas.drawRect(rect.right - cornerOffset, rect.bottom - cornerLength + cornerSize - cornerOffset, rect.right + cornerSize - cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
    }

    public void setDescription(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }


}