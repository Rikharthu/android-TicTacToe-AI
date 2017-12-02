package com.example.rikharthu.tictactoe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.rikharthu.tictactoe.tictac.*
import com.example.rikharthu.tictactoe.views.TicTacView
import timber.log.Timber

class TicTacActivity : AppCompatActivity(), TicTacView.OnCellClickListener, TicTacGame.UpdateListener {
    override fun onStateChanged(state: State) {
        Timber.d("State changed to $state")
        when (state) {
            State.NOUGHT_WON -> Toast.makeText(this, "Nought Won!", Toast.LENGTH_SHORT).show()
            State.CROSS_WON -> Toast.makeText(this, "Cross Won!", Toast.LENGTH_SHORT).show()
            State.TIE -> Toast.makeText(this, "Tie!", Toast.LENGTH_SHORT).show()
            else -> {
                // Do nothing
            }
        }
    }

    var canMove = false

    @JvmField
    @BindView(R.id.tic_tac_view)
    internal var ticTacView: TicTacView? = null
    private lateinit var game: TicTacGame
    private lateinit var currentPlayer: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac)
        ButterKnife.bind(this)

        val player1 = Player(Seed.NOUGHT)
        val player2 = MiniMaxAIPlayer(Seed.CROSS)
        Timber.d("player1 = ${player1.seed}, player2 = ${player2.seed}")

        game = TicTacGame(player1, player2)
        player1.listener = (Player.Listener {
            Timber.d("Player 1 can move")
            currentPlayer = player1
            canMove = true
        })
        game.updateListener = this
        game.start()

        ticTacView!!.cellClickListener = this
    }

    @OnClick(R.id.restartBtn)
    fun onRestartBtnClicked() {
        restartGame()
    }

    private fun restartGame() {
        Timber.d("Restarting game")
        ticTacView!!.clear()
        val player1 = Player(Seed.NOUGHT)
        val player2 = MiniMaxAIPlayer(Seed.CROSS)
        Timber.d("player1 = ${player1.seed}, player2 = ${player2.seed}")

        game = TicTacGame(player1, player2)
        player1.listener = (Player.Listener {
            Timber.d("Player 1 can move")
            currentPlayer = player1
            canMove = true
        })
        game.updateListener = this
        game.start()
    }

    override fun onCellClicked(row: Int, column: Int) {
        if (canMove && game.isEmpty(row, column)) {
            canMove = false
            currentPlayer.move(row, column)
        }
    }

    override fun onBoardUpdated(board: Array<Cell>) {
        board.forEach {
            val index = it.index
            val row = index / 3
            val column = index % 3
            ticTacView!!.setCellImage(row, column,
                    if (it.seed == Seed.CROSS) 1 else if (it.seed == Seed.NOUGHT) 2 else 0)
        }
    }
}
