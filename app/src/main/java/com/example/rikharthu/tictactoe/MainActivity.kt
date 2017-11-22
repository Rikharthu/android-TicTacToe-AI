package com.example.rikharthu.tictactoe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.example.rikharthu.tictactoe.tictac.AIPlayer
import com.example.rikharthu.tictactoe.tictac.Cell
import com.example.rikharthu.tictactoe.tictac.Seed
import com.example.rikharthu.tictactoe.tictac.State
import com.example.rikharthu.tictactoe.views.TicTacView
import timber.log.Timber

class MainActivity : AppCompatActivity(), TicTacView.OnCellClickListener {

    @JvmField
    @BindView(R.id.tic_tac_view)
    internal var ticTacView: TicTacView? = null

    private val board = arrayOf(
            emptyCell(0), emptyCell(1), emptyCell(2),
            emptyCell(3), emptyCell(4), emptyCell(5),
            emptyCell(6), emptyCell(7), emptyCell(8))
    private var aiPlayer = AIPlayer(seed = Seed.CROSS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        ticTacView!!.cellClickListener = this

        // "O", 1, "X",
        // "X", 4, "X",
        //  6, "O", "O"];
        aiMove()
    }

    fun clearBoard() {
        for (cell in board) {
            cell.seed = Seed.EMPTY
        }
    }

    fun emptyCell(index: Int) = Cell(index, Seed.EMPTY)

    override fun onCellClicked(row: Int, column: Int) {
        val index = row * 3 + column
        Timber.d("Click at $row, $column ($index)")
        makeMove(index)
    }

    private fun makeMove(index: Int) {
        if (board[index].seed == Seed.EMPTY) {
            board[index].seed = Seed.NOUGHT
            // TODO check winning
            // TODO refactor
            val row = index / 3
            val column = index % 3
            ticTacView!!.setCellImage(row, column, 2)

            aiMove()
        }
    }

    private fun aiMove() {
        val bestMove = aiPlayer.minimax(board, Seed.CROSS)
        board[bestMove.index].seed = Seed.CROSS
        val row = bestMove.index / 3
        val column = bestMove.index % 3
        ticTacView!!.setCellImage(row, column, 1)
    }

    private fun getState(): State {
        if (aiPlayer.winning(board, Seed.CROSS)) {
            return State.CROSS_WON
        } else if (aiPlayer.winning(board, Seed.NOUGHT)) {
            return State.NOUGHT_WON
        } else if (aiPlayer.emptyIndexes(board).isEmpty()) {
            return State.TIE
        } else {
            return State.PLAYING
        }
    }
}
