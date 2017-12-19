package com.cb.circletimerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.*
import android.view.View
import java.util.*


/**
 *Copyright 2017 Camillo Bucciarelli
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
class CircleTimerView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        readAttributesAndSetupFields(context.obtainStyledAttributes(attrs, R.styleable.CircleTimerView))
    }

    //  --------------------------------------------------------------------------------------------
    //      default values
    //  --------------------------------------------------------------------------------------------
    private val timeDivider = SparseIntArray()
    private val timeLabel = SparseArray<String>()
    private val timeCountdownInMillisecondDef = 0
    private val totalTimeInMillisecondDef = 0
    private val swipeAngleDef = 360f
    private val swipeAngleAddDef = 0f
    private val additiveModeDef = false
    private val labelSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, context.resources.displayMetrics)
    private val labelColorDef = Color.GRAY
    private val valueTopSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
    private val valueTopColorDef = Color.GRAY
    private val valueBottomSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
    private val valueBottomColorDef = Color.LTGRAY
    private val labelForWeeksDef = "WK"
    private val labelForDaysDef = "DAY"
    private val labelForHoursDef = "HR"
    private val labelForMinutesDef = "MIN"
    private val labelForSecondsDef = "SEC"
    private val lineSeparatorColorDef = Color.parseColor("#b0bec5")
    private val lineSeparatorStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, context.resources.displayMetrics)
    private val backCircleColorDef = Color.parseColor("#b0bec5")
    private val backCircleStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics)
    private val foregroundCircleColorDef = Color.parseColor("#263238")
    private val foregroundCircleStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics)
    private val foregroundCircleStartValueDef = -90f

    //  --------------------------------------------------------------------------------------------
    //      properties
    //  --------------------------------------------------------------------------------------------
    private val rect: RectF by lazy {
        RectF(
                (width / 2).toFloat() - radius(),
                (height / 2).toFloat() - radius(),
                (width / 2).toFloat() + radius(),
                (height / 2).toFloat() + radius()
        )
    }

    private val timer = Timer()
    private val additiveTimerTask = object : TimerTask() {
        override fun run() {
            Log.d("timer...", "actual second: " + totalTimeInMillisecond)
            totalTimeInMillisecond = ++totalTimeInMillisecond
        }
    }
    private val subtractiveTimerTask = object : TimerTask() {
        override fun run() {
            when (totalTimeInMillisecond) {
                0 -> {
                    Log.d("timer...", "Timer 0... cancelled")
                    timer.cancel()
                    timer.purge()
                }
                else -> {
                    Log.d("timer...", "actual second: " + totalTimeInMillisecond)
                    totalTimeInMillisecond = --totalTimeInMillisecond
                }
            }
        }
    }

    var timeCountdownInMillisecond = timeCountdownInMillisecondDef
        set(value) {
            field = value
            postInvalidate()
        }
    var totalTimeInMillisecond = totalTimeInMillisecondDef
        set(value) {
            field = value
            n = -1
            setTimeValue(totalTimeInMillisecond)
            foregroundMovement()
            setTimeValue(value)
            postInvalidate()
        }
    var labelTop = ""
    var valueTop = ""
    var labelBottom = ""
    var valueBottom = ""
    var swipeAngle = swipeAngleDef
        set(value) {
            field = value
            postInvalidate()
        }
    var additiveMode = additiveModeDef
        set(value) {
            field = value
            postInvalidate()
        }

    private var foregroundCircleStartValue = foregroundCircleStartValueDef
    private var n = -1
    private val lineSeparatorPaint = Paint()
    private val backCirclePaint = Paint()
    private val foregroundCirclePaint = Paint()
    private val labelPaint = Paint()
    private val valueTopPaint = Paint()
    private val valueBottomPaint = Paint()

    //  --------------------------------------------------------------------------------------------
    //      initializer property method
    //  --------------------------------------------------------------------------------------------
    private fun readAttributesAndSetupFields(attrs: TypedArray) {
        timeCountdownInMillisecond = attrs.getInteger(R.styleable.CircleTimerView_timeCountdownInMillisecond, timeCountdownInMillisecondDef)
        totalTimeInMillisecond = attrs.getInt(R.styleable.CircleTimerView_totalTimeInMillisecond, totalTimeInMillisecondDef)
        additiveMode = attrs.getBoolean(R.styleable.CircleTimerView_additiveMode, additiveModeDef)
        swipeAngle = if (additiveMode) swipeAngleAddDef else swipeAngleDef
        timeLabel.put(0, attrs.getString(R.styleable.CircleTimerView_labelForSeconds, labelForSecondsDef))
        timeLabel.put(1, attrs.getString(R.styleable.CircleTimerView_labelForMinutes, labelForMinutesDef))
        timeLabel.put(2, attrs.getString(R.styleable.CircleTimerView_labelForHours, labelForHoursDef))
        timeLabel.put(3, attrs.getString(R.styleable.CircleTimerView_labelForDays, labelForDaysDef))
        timeLabel.put(4, attrs.getString(R.styleable.CircleTimerView_labelForWeeks, labelForWeeksDef))
        timeDivider.put(1, 1000)
        timeDivider.put(2, 60000)
        timeDivider.put(3, 60000)
        timeDivider.put(4, 24000)
        timeDivider.put(5, 7000)
        n = -1
        paintSetup(
                lineSeparatorPaint,
                attrs.getColor(R.styleable.CircleTimerView_lineSeparatorColor, lineSeparatorColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_lineSeparatorStrokeWidth, lineSeparatorStrokeWidthDef)
        )
        paintSetup(
                backCirclePaint,
                attrs.getColor(R.styleable.CircleTimerView_backCircleColor, backCircleColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_backCircleStrokeWidth, backCircleStrokeWidthDef)
        )
        paintSetup(
                foregroundCirclePaint,
                attrs.getColor(R.styleable.CircleTimerView_foregroundCircleColor, foregroundCircleColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_foregroundCircleStrokeWidth, foregroundCircleStrokeWidthDef)
        )
        textSetup(
                labelPaint,
                attrs.getColor(R.styleable.CircleTimerView_labelColor, labelColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_labelSize, labelSizeDef)
        )
        textSetup(
                valueTopPaint,
                attrs.getColor(R.styleable.CircleTimerView_valueTopColor, valueTopColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_valueTopSize, valueTopSizeDef),
                true
        )
        textSetup(
                valueBottomPaint,
                attrs.getColor(R.styleable.CircleTimerView_valueBottomColor, valueBottomColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_valueBottomSize, valueBottomSizeDef),
                true
        )
        foregroundMovement()
        postInvalidate()
    }

    private fun paintSetup(paint: Paint, color: Int, strokeWidth: Float) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
    }

    private fun textSetup(paint: Paint, color: Int, size: Float, bold: Boolean = false) {
        paint.color = color
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        if (bold) paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    //  --------------------------------------------------------------------------------------------
    //      draw timer
    //  --------------------------------------------------------------------------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //  line separator
        canvas.drawLine(
                (width / 2).toFloat() - radius(),
                (height / 2).toFloat(),
                (width / 2).toFloat() + radius(),
                (height / 2).toFloat(),
                lineSeparatorPaint
        )

        //  back circle
        canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                radius(),
                backCirclePaint
        )

        //  foreground circle
        canvas.drawArc(
                rect,
                foregroundCircleStartValue,
                swipeAngle,
                false,
                foregroundCirclePaint
        )

        // top label
        canvas.drawText(
                labelTop,
                (width / 2).toFloat(),
                (height / 2).toFloat() - (radius() / 3) * 2,
                labelPaint
        )

        // top value
        canvas.drawText(
                valueTop,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - ((valueTopPaint.descent() + valueTopPaint.ascent()) / 2)
                        - (radius() / 3),
                valueTopPaint
        )

        // bottom value
        canvas.drawText(
                valueBottom,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - ((valueBottomPaint.descent() + valueBottomPaint.ascent()) / 2)
                        + (radius() / 3),
                valueBottomPaint
        )

        // bottom label
        canvas.drawText(
                labelBottom,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - (labelPaint.descent() + labelPaint.ascent())
                        + (radius() / 3) * 2,
                labelPaint
        )
    }

    private fun radius(): Float {
        return when (width < height) {
            true -> (width / 3).toFloat()
            else -> (height / 3).toFloat()
        }
    }


    private fun setTimeValue(timeValue: Int) {



//        n++
//
//        if (n == timeDivider.size()) return
//
//        Log.w("set time...","n: "+n)
//
//        Log.w("set time...","timeDivider.get(n): "+timeDivider.get(n))
//
//        if (timeDivider.get(n) != 0) {
//            labelBottom = label(n)
//            valueBottom = (timeValue % timeDivider.get(n)).toString()
//
//            when (timeValue / timeDivider.get(n) > timeDivider.get(n)) {
//                true -> {
//                    Log.e("set time...","recall ")
//                    setTimeValue(timeValue / timeDivider.get(n))
//                }
//                else -> {
//                    labelTop = label(n)
//                    valueTop = (timeValue / timeDivider.get(n)).toString()
//                }
//            }
//        }
    }

    private fun foregroundMovement() {
        swipeAngle = (swipeAngleDef * totalTimeInMillisecond) / timeCountdownInMillisecond
    }

    private fun label(position: Int) = timeLabel.get(position-1)

    fun startTimer() {
        Log.w("timer...", "Timer additive mode... " + additiveMode)
        timer.schedule(
                when (additiveMode) {
                    true -> additiveTimerTask
                    else -> subtractiveTimerTask
                },
                1,
                1
        )
    }

    fun stopTimer() {
        Log.w("timer...", "Timer cancelled")
        timer.cancel()
        timer.purge()
    }

}

//  --------------------------------------------------------------------------------------------
//      extension of typedArray get string that accept default
//  --------------------------------------------------------------------------------------------
private fun TypedArray.getString(stringId: Int, default: String): String {
    return when (this.getString(stringId)) {
        null -> default
        else -> this.getString(stringId)
    }
}
