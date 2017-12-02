package com.example.rikharthu.tictactoe.views


import android.content.Context
import android.graphics.Color
import android.support.annotation.Size
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.rikharthu.tictactoe.R
import timber.log.Timber

class TicTacView : ConstraintLayout {

    private var mCrossColor: Int = 0
    private var mCircleColor: Int = 0
    @Size(3)
    private var mStatuses: Array<IntArray>? = null
    var cellClickListener: OnCellClickListener? = null

    @JvmField
    @BindViews(R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5, R.id.cell6, R.id.cell7, R.id.cell8, R.id.cell9)
    internal var mCellImages: Array<ImageView>? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val sideMeasureSpec = if (width < height) widthMeasureSpec else heightMeasureSpec
        super.onMeasure(sideMeasureSpec, sideMeasureSpec)
    }

    private fun init() {
        // Inflate view
        View.inflate(context, R.layout.view_tic_tac, this)
        ButterKnife.bind(this)

        mStatuses = Array(3) { IntArray(3) }

        mCrossColor = Color.rgb(255, 0, 0)
        mCircleColor = Color.rgb(0, 0, 255)
    }

    @OnClick(R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5, R.id.cell6, R.id.cell7, R.id.cell8, R.id.cell9)
    internal fun onCellClicked(cellImage: ImageView) {
        if (cellClickListener != null) {
            val tag = cellImage.tag as String
            val row = Integer.parseInt(tag.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
            val column = Integer.parseInt(tag.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            Timber.d("Click at %d %d", row, column)
            cellClickListener!!.onCellClicked(row, column)
        }
    }

    fun setCellImage(row: Int, column: Int, seed: Int) {
        val cellIndex = row * 3 + column
        val cellImage = mCellImages!![cellIndex]
        when (seed) {
            0 ->
                // empty
                cellImage.setImageDrawable(null)
            1 ->
                // cross
                cellImage.setImageResource(R.drawable.cross)
            2 ->
                // nought
                cellImage.setImageResource(R.drawable.circle)
        }
    }

    interface OnCellClickListener {
        fun onCellClicked(row: Int, column: Int)
    }

    companion object {

        // Line Statuses
        // FIRST_ROW, SECOND_ROW,THIRD_ROW,FIRST_COLUMN, SECOND_COLUMN,THIRD_COLUMN,FORWARD_SLASH,BACKWARD_SLASH
        val LINE_NONE = 1000
        val LINE_FIRST_ROW = 1001
        val LINE_SECOND_ROW = 1002
        val LINE_THIRD_ROW = 1003
        val LINE_FIRST_COLUMN = 1004
        val LINE_SECOND_COLUMN = 1005
        val LINE_THIRD_COLUMN = 1006
        val LINE_FORWARD_SLASH = 1007
        val LINE_BACKWARD_SLASH = 1008
        val NONE = 2

        internal val CLEAR: ButterKnife.Action<ImageView> = ButterKnife.Action { view, index -> view.setImageDrawable(null) }
    }

    fun clear() {
        ButterKnife.apply(mCellImages!!, CLEAR)
    }
}