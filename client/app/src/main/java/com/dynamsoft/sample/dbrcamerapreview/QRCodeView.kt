package com.dynamsoft.sample.dbrcamerapreview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView

class QRCodeView : RelativeLayout {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = Rect()
    private var maskColor = 0
    private var boxViewWidth = 0
    private var boxViewHeight = 0
    private var cornerColor = 0
    private var borderColor = 0
    private var cornerSize = 0
    private var cornerLength = 0
    private var cornerOffset = 0
    private lateinit var boxView: FrameLayout
    private lateinit var textView: TextView

    constructor(context: Context) : super(context) {
        initialize(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        inflate(context, R.layout.layout_qr_code_view, this)
        val resources = resources
        val styledAttributes: TypedArray = context
                .obtainStyledAttributes(attrs, R.styleable.QRCodeView, defStyleAttr, 0)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            maskColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_maskColor,
                    resources.getColor(R.color.qr_code_view_mask))
            cornerColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_boxViewCornerColor,
                    resources.getColor(R.color.qr_code_view_corner))
            borderColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_boxViewBorderColor,
                    resources.getColor(R.color.qr_code_view_border))
        } else {
            maskColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_boxViewCornerColor,
                    resources.getColor(R.color.qr_code_view_mask, null))
            cornerColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_boxViewCornerColor,
                    resources.getColor(R.color.qr_code_view_corner, null))
            borderColor = styledAttributes.getColor(
                    R.styleable.QRCodeView_boxViewBorderColor,
                    resources.getColor(R.color.qr_code_view_border, null))
        }

        cornerOffset = styledAttributes.getInt(
                R.styleable.QRCodeView_boxViewCornerOffset,
                resources.getDimension(R.dimen.size_qr_box_view_corner_offset).toInt())
        cornerLength = styledAttributes.getInt(
                R.styleable.QRCodeView_boxViewCornerLength,
                resources.getDimension(R.dimen.length_qr_box_view_corner).toInt())
        cornerSize = styledAttributes.getInt(
                R.styleable.QRCodeView_boxViewCornerSize,
                resources.getDimension(R.dimen.size_qr_box_view_corner).toInt())
        boxViewWidth = styledAttributes.getInt(
                R.styleable.QRCodeView_boxViewWidth,
                resources.getDimension(R.dimen.width_qr_box_view).toInt())
        boxViewHeight = styledAttributes.getInt(
                R.styleable.QRCodeView_boxViewHeight,
                resources.getDimension(R.dimen.height_qr_box_view).toInt())

        styledAttributes.recycle()
        boxView = findViewById(R.id.fl_box_view)
        textView = findViewById(R.id.tv_desc)
    }

    fun resetBoxView(left: Int, top: Int, width: Int, height: Int) {
        val margin = MarginLayoutParams(boxView.layoutParams)
        margin.setMargins(left, top, left + width, top + height)
        boxViewWidth = width
        boxViewHeight = height
        val layoutParams = LayoutParams(margin)
        boxView.layoutParams = layoutParams
        /// boxView.removeAllViews();
        // boxView.addView(mIvScanLine);
        setBackgroundResource(R.color.qr_code_view_mask)
    }

    public override fun onDraw(canvas: Canvas) {
        /** Draw the exterior dark mask */
        val width = width
        val height = height
        val boxViewX = boxView.x
        val boxViewY = boxView.y
        paint.color = maskColor
        // left rect
        canvas.drawRect(0f, boxViewY, boxViewX, boxViewY + boxViewHeight, paint)
        // right rect
        canvas.drawRect(boxViewX + boxViewWidth, boxViewY, width.toFloat(), boxViewY + boxViewHeight, paint)
        // top rect
        canvas.drawRect(0f, 0f, width.toFloat(), boxViewY, paint)
        // bottom rect
        canvas.drawRect(0f, boxViewY + boxViewHeight, width.toFloat(), height.toFloat(), paint)

        /** Draw the border lines */
        paint.color = borderColor
        canvas.drawLine(boxViewX, boxViewY, boxViewX + boxViewWidth, boxViewY, paint)
        canvas.drawLine(boxViewX, boxViewY, boxViewX, boxViewY + boxViewHeight, paint)
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX, boxViewY + boxViewHeight, paint)
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX + boxViewWidth, boxViewY, paint)

        /** Draw the corners */
        rect[boxViewX.toInt(), boxViewY.toInt(), boxViewX.toInt() + boxViewWidth] = boxViewY.toInt() + boxViewHeight
        paint.color = cornerColor

        /** top the corners */
        canvas.drawRect(
                (rect.left - cornerSize + cornerOffset).toFloat(),
                (rect.top - cornerSize + cornerOffset).toFloat(),
                (rect.left + cornerLength - cornerSize + cornerOffset).toFloat(),
                (rect.top + cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.left - cornerSize + cornerOffset).toFloat(),
                (rect.top - cornerSize + cornerOffset).toFloat(),
                (rect.left + cornerOffset).toFloat(),
                (rect.top + cornerLength - cornerSize + cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.right - cornerLength + cornerSize - cornerOffset).toFloat(),
                (rect.top - cornerSize + cornerOffset).toFloat(),
                (rect.right + cornerSize - cornerOffset).toFloat(),
                (rect.top + cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.right - cornerOffset).toFloat(),
                (rect.top - cornerSize + cornerOffset).toFloat(),
                (rect.right + cornerSize - cornerOffset).toFloat(),
                (rect.top + cornerLength - cornerSize + cornerOffset).toFloat(),
                paint)

        /** bottom the corners */
        canvas.drawRect(
                (rect.left - cornerSize + cornerOffset).toFloat(),
                (rect.bottom - cornerOffset).toFloat(),
                (rect.left + cornerLength - cornerSize + cornerOffset).toFloat(),
                (rect.bottom + cornerSize - cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.left - cornerSize + cornerOffset).toFloat(),
                (rect.bottom - cornerLength + cornerSize - cornerOffset).toFloat(),
                (rect.left + cornerOffset).toFloat(),
                (rect.bottom + cornerSize - cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.right - cornerLength + cornerSize - cornerOffset).toFloat(),
                (rect.bottom - cornerOffset).toFloat(),
                (rect.right + cornerSize - cornerOffset).toFloat(),
                (rect.bottom + cornerSize - cornerOffset).toFloat(),
                paint)
        canvas.drawRect(
                (rect.right - cornerOffset).toFloat(),
                (rect.bottom - cornerLength + cornerSize - cornerOffset).toFloat(),
                (rect.right + cornerSize - cornerOffset).toFloat(),
                (rect.bottom + cornerSize - cornerOffset).toFloat(),
                paint)
    }

    fun setDescription(text: String?) {
        textView.text = text
    }
}