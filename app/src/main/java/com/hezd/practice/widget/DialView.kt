package com.hezd.practice.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.hezd.practice.R
import kotlin.math.min

/**
 * @author hezd
 * @date 2023/9/25 15:05
 * @description 拨号盘
 *  自定义View示例
 */
class DialView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius: Float = 0f
    private val paint = Paint().apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var width: Int = 0
    private var height: Int = 0
    private val positionPoint: PointF = PointF(0f, 0f)
    private val fanSpeed = FanSpeed.OFF

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2 * 0.8f
        width = w
        height = h

    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(width / 2.0f, height / 2.0f, radius, paint)
    }
}

private enum class FanSpeed(val label: Int) {
    OFF(R.string.off),
    LOW(R.string.low),
    MEDIUM(R.string.medium),
    HIGH(R.string.high)
}