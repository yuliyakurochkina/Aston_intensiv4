package com.example.aston_intensiv4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class Clock(context: Context, attrs: AttributeSet) :
    View(context, attrs) {
    // Кисти для циферблата, делений и цифр
    private var circleBrush: Paint? = null
    private var pointerBrush: Paint? = null
    private var numberBrush: Paint? = null

    // Ширина линии и радиус циферблата
    private var clockRingWidth: Float = 0f
    private var clockRadius: Float = 0f

    // Ширина и длина каждого секундного деления, ширина и длина каждого пятого секундного деления
    private var secondPointWidth: Float = 0f
    private var secondPointLength: Float = 0f
    private var fiveSecondPointWidth: Float = 0f
    private var fiveSecondPointLength: Float = 0f

    // Ширина часовой, минутной и секундной стрелок
    private var hourHandWidth: Float = 0f
    private var minuteHandWidth: Float = 0f
    private var secondHandWidth: Float = 0f

    // Цвета
    private var clockRingColor = 0
    private var hourHandColor = 0
    private var minuteHandColor = 0
    private var secondHandColor = 0
    private var numbersColor = 0

    // Ширина и высота часов, начало осей координат
    private var clockWidth = 0
    private var clockHeight = 0
    private var clockCenterX = 0
    private var clockCenterY = 0

    // Текущее время
    private var currentHour: Float = 0f
    private var currentMinute: Float = 0f
    private var currentSecond: Float = 0f

    private val timer = Timer()
    private val task: TimerTask = object : TimerTask() {
        override fun run() {
            if (currentSecond == 360f) {
                currentSecond = 0f
            }
            if (currentMinute == 360f) {
                currentMinute = 0f
            }
            if (currentHour == 360f) {
                currentHour = 0f
            }
            currentSecond += 6
            currentMinute += 0.1f
            currentHour += 1.0f / 120
            postInvalidate()
        }
    }

    private fun start() {
        timer.schedule(task, 0, 1000)
    }

    private fun setTime(hour: Int, minute: Int, second: Int) {
        currentHour = if (hour >= 12) {
            (hour + minute * 1.0f / 60f + second * 1.0f / 3600f - 12) * 30f - 180
        } else {
            (hour + minute * 1.0f / 60f + second * 1.0f / 3600f) * 30f - 180
        }
        currentMinute = (minute + second * 1.0f / 60f) * 6f - 180
        currentSecond = second * 6f - 180
    }

    init {
        init(context, attrs)
        initPaint()
        val calendar = Calendar.getInstance()
        val hours = calendar[Calendar.HOUR]
        val minutes = calendar[Calendar.MINUTE]
        val seconds = calendar[Calendar.SECOND]
        setTime(hours, minutes, seconds)
        start()
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Clock)
        clockRingWidth = typedArray.getDimension(
            R.styleable.Clock_clockRingWidth,
            SizeUtils.dp2px(context, 4f).toFloat()
        )
        secondPointWidth = typedArray.getDimension(
            R.styleable.Clock_secondPointWidth,
            SizeUtils.dp2px(context, 1f).toFloat()
        )
        secondPointLength = typedArray.getDimension(
            R.styleable.Clock_secondPointLength,
            SizeUtils.dp2px(context, 8f).toFloat()
        )
        fiveSecondPointWidth = typedArray.getDimension(
            R.styleable.Clock_fiveSecondPointWidth,
            SizeUtils.dp2px(context, 2f).toFloat()
        )
        fiveSecondPointLength = typedArray.getDimension(
            R.styleable.Clock_fiveSecondPointLength,
            SizeUtils.dp2px(context, 14f).toFloat()
        )
        hourHandWidth = typedArray.getDimension(
            R.styleable.Clock_hourHandWidth,
            SizeUtils.dp2px(context, 6f).toFloat()
        )
        minuteHandWidth = typedArray.getDimension(
            R.styleable.Clock_minuteHandWidth,
            SizeUtils.dp2px(context, 4f).toFloat()
        )
        secondHandWidth = typedArray.getDimension(
            R.styleable.Clock_secondHandWidth,
            SizeUtils.dp2px(context, 2f).toFloat()
        )
        clockRingColor = typedArray.getColor(R.styleable.Clock_circleColor, Color.BLACK)
        hourHandColor = typedArray.getColor(R.styleable.Clock_hourHandColor, Color.RED)
        minuteHandColor = typedArray.getColor(R.styleable.Clock_minuteHandColor, Color.GREEN)
        secondHandColor = typedArray.getColor(R.styleable.Clock_secondHandColor, Color.BLACK)
        numbersColor = typedArray.getColor(R.styleable.Clock_numbersColor, Color.BLACK)
        typedArray.recycle()
    }

    private fun initPaint() {
        circleBrush = Paint()
        circleBrush!!.isAntiAlias = true
        circleBrush!!.style = Paint.Style.STROKE
        pointerBrush = Paint()
        pointerBrush!!.isAntiAlias = true
        pointerBrush!!.style = Paint.Style.FILL_AND_STROKE
        pointerBrush!!.strokeCap = Paint.Cap.ROUND
        numberBrush = Paint()
        numberBrush!!.style = Paint.Style.FILL
        numberBrush!!.textSize = 60f
        numberBrush!!.color = numbersColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasureSize(true, widthMeasureSpec)
        val height = getMeasureSize(false, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(width: Int, height: Int, prevWidth: Int, prevHeigth: Int) {
        super.onSizeChanged(width, height, prevWidth, prevHeigth)
        clockWidth = width
        clockHeight = height
        clockCenterX = width / 2
        clockCenterY = height / 2
        clockRadius = ((width / 2).toFloat() * 0.8).toFloat()
    }

    private fun getMeasureSize(isWidth: Boolean, measureSpec: Int): Int {
        var result = 0
        val specialSize = MeasureSpec.getSize(measureSpec)
        when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> result = if (isWidth) {
                suggestedMinimumWidth
            } else {
                suggestedMinimumHeight
            }
            MeasureSpec.AT_MOST -> result =
                if (isWidth) specialSize.coerceAtMost(clockWidth) else specialSize.coerceAtMost(
                    clockHeight
                )
            MeasureSpec.EXACTLY -> result = specialSize
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(clockCenterX.toFloat(), clockCenterY.toFloat())
        drawCircle(canvas)
        drawNumbers(canvas)
        drawPointer(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        circleBrush!!.strokeWidth = clockRingWidth
        circleBrush!!.color = clockRingColor
        canvas.drawCircle(0f, 0f, clockRadius, circleBrush!!)
        for (i in 0..59) {
            if (i % 5 == 0) {
                circleBrush!!.strokeWidth = fiveSecondPointWidth
                circleBrush!!.color = hourHandColor
                canvas.drawLine(
                    0f,
                    -clockRadius + clockRingWidth / 2,
                    0f,
                    -clockRadius + fiveSecondPointLength,
                    circleBrush!!
                )
            } else {
                circleBrush!!.strokeWidth = secondPointWidth
                circleBrush!!.color = secondHandColor
                canvas.drawLine(
                    0f, -clockRadius + clockRingWidth / 2, 0f, -clockRadius + secondPointLength,
                    circleBrush!!
                )
            }
            canvas.rotate(6f)
        }
    }

    private fun drawNumbers(canvas: Canvas) {
        for (i in 0..11) {
            canvas.save()
            if (i == 0) {
                val textBound = Rect()
                canvas.translate(
                    0f,
                    -clockRadius + fiveSecondPointLength + secondPointLength + clockRingWidth
                )
                val text = "12"
                numberBrush!!.getTextBounds(text, 0, text.length, textBound)
                canvas.drawText(
                    text,
                    (-textBound.width() / 2).toFloat(),
                    (textBound.height() / 2).toFloat(),
                    numberBrush!!
                )
            } else {
                val textBound = Rect()
                canvas.translate(
                    0f,
                    -clockRadius + fiveSecondPointLength + secondPointLength + clockRingWidth
                )
                val text = i.toString() + ""
                numberBrush!!.getTextBounds(text, 0, text.length, textBound)
                canvas.rotate((-i * 30).toFloat())
                canvas.drawText(
                    text, (-textBound.width() / 2).toFloat(), (
                            textBound.height() / 2).toFloat(), numberBrush!!
                )
            }
            canvas.restore()
            canvas.rotate(30f)
        }
    }

    private fun drawPointer(canvas: Canvas) {
        canvas.save()
        pointerBrush!!.color = hourHandColor
        pointerBrush!!.strokeWidth = hourHandWidth
        canvas.rotate(currentHour, 0f, 0f)
        canvas.drawLine(0f, -20f, 0f, (clockRadius * 0.45).toFloat(), pointerBrush!!)
        canvas.restore()
        canvas.save()
        pointerBrush!!.color = minuteHandColor
        pointerBrush!!.strokeWidth = minuteHandWidth
        canvas.rotate(currentMinute, 0f, 0f)
        canvas.drawLine(0f, -20f, 0f, (clockRadius * 0.6).toFloat(), pointerBrush!!)
        canvas.restore()
        canvas.save()
        pointerBrush!!.color = secondHandColor
        pointerBrush!!.strokeWidth = secondHandWidth
        canvas.rotate(currentSecond, 0f, 0f)
        canvas.drawLine(0f, -40f, 0f, (clockRadius * 0.75).toFloat(), pointerBrush!!)
        canvas.restore()
        canvas.drawCircle(0f, 0f, hourHandWidth / 2, pointerBrush!!)
    }

    private object SizeUtils {
        fun dp2px(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density + 0.5).toInt()
        }
    }
}