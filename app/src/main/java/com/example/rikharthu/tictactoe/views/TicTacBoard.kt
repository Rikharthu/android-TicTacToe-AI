package com.example.rikharthu.tictactoe.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.rikharthu.tictactoe.R
import timber.log.Timber

class TicTacBoard : View {

    private val paint = Paint()
    private val crossPaint = Paint()
    private val noughtPaint = Paint()
    private val highLightPaint = Paint()
    private val partitionRatio = 1 / 3f
    private val count = 3
    private var offsetX = 0f
    private var offsetY = 0f
    private var side = 0f
    private var rectIndex = Pair(0, 0)
    private var touching: Boolean = false
    val moveX = "X"
    val moveY = "O"
    private lateinit var squares: Array<Array<Rect>>
    private lateinit var squareData: Array<Array<String>>
    var squarePressListener: SquarePressedListener? = null

    fun drawXAtPosition(x: Int, y: Int) {
        Timber.d("Drawing X at $x $y")
        squareData[x][y] = moveX
        invalidate(squares[x][y])
    }

    fun drawOAtPosition(x: Int, y: Int) {
        Timber.d("Drawing O at $x $y")
        squareData[x][y] = moveY
        invalidate(squares[x][y])
    }

    fun clearAtPosition(x: Int, y: Int) {
        Timber.d("Clearing at $x $y")
        squareData[x][y] = ""
        invalidate(squares[x][y])
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @SuppressLint("PrivateResource")
    private fun init() {
        paint.color = ContextCompat.getColor(context, R.color.white)
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = resources.displayMetrics.density * 5//line width

        crossPaint.color = Color.RED
        crossPaint.isAntiAlias = true
        crossPaint.typeface = ResourcesCompat.getFont(context, R.font.architects_daughter)
        crossPaint.textSize = resources.displayMetrics.scaledDensity * 100

        noughtPaint.color = Color.BLUE
        noughtPaint.isAntiAlias = true
        noughtPaint.typeface = ResourcesCompat.getFont(context, R.font.architects_daughter)
        noughtPaint.textSize = resources.displayMetrics.scaledDensity * 100


        highLightPaint.color = ContextCompat.getColor(context, R.color.ripple_material_light)
        highLightPaint.style = Paint.Style.FILL
        highLightPaint.isAntiAlias = true

        side = Math.min(width, height).toFloat()
        offsetX = ((width - side) / 2)
        offsetY = ((height - side) / 2)

        // Initialize Tic Tac squares
        squares = Array(3, { Array(3, { Rect() }) })
        squareData = Array(3, { Array(3, { "" }) })

        val xUnit = (side * partitionRatio).toInt() // one unit on x-axis
        val yUnit = (side * partitionRatio).toInt() // one unit on y-axis

        for (j in 0 until count) {
            for (i in 0 until count) {
                squares[i][j] = Rect((i * xUnit + offsetX).toInt(), (j * yUnit + offsetY).toInt(),
                        ((i + 1) * xUnit + offsetX).toInt(), ((j + 1) * yUnit + offsetY).toInt())
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val side = if (widthSize < heightSize) widthSize else heightSize

        val width: Int
        val height: Int

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(side, widthSize)
        } else {
            //Be whatever you want
            width = side
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(side, heightSize)
        } else {
            //Be whatever you want
            height = side
        }
        Timber.d("width: $width, height: $height")
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val centerX = width / 2
        val centerY = height / 2


        if (false) {
            // TODO for debug
            canvas.drawRect(0f, 0f, width, height, paint)
            paint.color = Color.RED
            canvas.drawRect(offsetX, offsetY, width + offsetX, height - offsetY, paint)
            paint.color = Color.BLUE
        }

        // 1. Draw lines
        // Horizontal lines
        canvas.drawLine(offsetX, side * partitionRatio + offsetY,
                side + offsetX, side * partitionRatio + offsetY, paint)
        canvas.drawLine(offsetX, side * 2 * partitionRatio + offsetY,
                side + offsetX, side * 2 * partitionRatio + offsetY, paint)
        // Vertical lines
        canvas.drawLine(side * partitionRatio + offsetX, offsetY,
                side * partitionRatio + offsetX, offsetY + side, paint)
        canvas.drawLine(side * 2 * partitionRatio + offsetX, offsetY,
                side * 2 * partitionRatio + offsetX, offsetY + side, paint)

        // 2. Squares
        drawSquareStates(canvas)

        if (touching) {
            drawHighlightRectangle(canvas)
        }
    }

    private fun drawSquareStates(canvas: Canvas) {
        for ((i, textArray) in squareData.withIndex()) {
            for ((j, text) in textArray.withIndex()) {
                if (text.isNotEmpty()) {
                    drawTextInsideRectangle(canvas, squares[i][j], text,
                            if (squareData[i][j].equals(moveX)) crossPaint else noughtPaint)
                }
            }
        }
    }

    private fun drawHighlightRectangle(canvas: Canvas) {
        canvas.drawRect(squares[rectIndex.first][rectIndex.second], highLightPaint)
    }

    private fun drawTextInsideRectangle(canvas: Canvas, rect: Rect, text: String, paint: Paint = this.paint) {
        val xOffset = paint.measureText(text) * 0.5f
        val yOffset = paint.fontMetrics.ascent * -0.4f
        val textX = rect.exactCenterX() - xOffset
        val textY = rect.exactCenterY() + yOffset
        canvas.drawText(text, textX, textY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rectIndex = getRectIndexesFor(x, y)
                touching = true
                if (rectIndex.first >= 0 && rectIndex.second >= 0) {
                    invalidate(squares[rectIndex.first][rectIndex.second])
                }
            }
            MotionEvent.ACTION_UP -> {
                touching = false
                if (rectIndex.first >= 0 && rectIndex.second >= 0) {
                    invalidate(squares[rectIndex.first][rectIndex.second])

                    val (finalX1, finalY1) = getRectIndexesFor(x, y)
                    if ((finalX1 == rectIndex.first) && (finalY1 == rectIndex.second)) {
                        // if initial touch and final touch is in same rectangle or not
                        squarePressListener?.onSquarePressed(rectIndex.first, rectIndex.second)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                touching = false
            }
        }
        return true
    }

    fun getRectIndexesFor(x: Float, y: Float): Pair<Int, Int> {
        squares.forEachIndexed { i, rects ->
            for ((j, rect) in rects.withIndex()) {
                if (rect.contains(x.toInt(), y.toInt())) {
                    return Pair(i, j)
                }
            }
        }
        return Pair(-1, -1)
    }

    interface SquarePressedListener {
        fun onSquarePressed(i: Int, j: Int)
    }

    fun clear() {
        Timber.d("Clearing board")
        squareData.forEachIndexed { i, rects ->
            for ((j, _) in rects.withIndex()) {
                drawOAtPosition(i, j)
            }
        }
    }
}