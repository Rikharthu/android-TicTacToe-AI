package com.example.rikharthu.tictactoe

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintSet
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.rikharthu.tictactoe.tictac.*
import com.example.rikharthu.tictactoe.views.TicTacBoard
import kotlinx.android.synthetic.main.activity_test.*
import timber.log.Timber


class TestActivity : AppCompatActivity(), TicTacBoard.SquarePressedListener, TicTacGame.UpdateListener {

    // TODO handle case when restart is pressed while AIPlayer is calculating it's next move
    // TODO thus game is restarted, but AIPlayer callbacks fires and makes move, updating the board

    private lateinit var game: TicTacGame
    private lateinit var currentPlayer: Player
    var canMove = false
    private lateinit var constraintSet1: ConstraintSet
    private lateinit var constraintSet2: ConstraintSet
    var currentSet = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        setSupportActionBar(toolbar)

        ticTacBoard.squarePressListener = this

        constraintSet1 = ConstraintSet()
        constraintSet1.clone(root)
        constraintSet2 = ConstraintSet()
        constraintSet2.clone(constraintSet1)
        constraintSet2.connect(current_move_line.id, ConstraintSet.START, R.id.cross_icon_label, ConstraintSet.START)
        constraintSet2.connect(current_move_line.id, ConstraintSet.END, R.id.cross_player_label, ConstraintSet.END)
    }

    override fun onResume() {
        super.onResume()
        // TODO also move calculations on a worker thread
        // TODO add concurrency?
        Handler().postDelayed({
            restartGame()
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.tic_tac_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_restart -> {
                restartGame()
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Opening settings", Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }

        return true
    }

    private fun restartGame() {
        ticTacBoard.clear()

        val player1 = Player(Seed.NOUGHT)
        val player2 = MiniMaxAIPlayer(Seed.CROSS)
        player2.simulateDelay = true
        player2.simulatedDelayLength = 1000L
        game = TicTacGame(player1, player2)
        player1.listener = (object:Player.Listener {
            override fun onCanMove() {
                Timber.d("Player 1 can move")
                currentPlayer = player1
                canMove = true
            }

            override fun onCancelMove() {
                // Do nothing
                // TODO
            }

        })
        game.updateListener = this
        game.start()
    }

    override fun onSquarePressed(x: Int, y: Int) {
        if (canMove && game.isEmpty(x, y)) {
            canMove = false
            currentPlayer.move(x, y)
        }
    }

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

    override fun onBoardUpdated(board: Array<Cell>) {
        Timber.d("Board has been updated")
        board.forEach {
            val index = it.index
            val row = index / 3
            val column = index % 3
            when {
                it.seed == Seed.CROSS -> ticTacBoard.drawXAtPosition(row, column)
                it.seed == Seed.NOUGHT -> ticTacBoard.drawOAtPosition(row, column)
                else -> ticTacBoard.clearAtPosition(row, column)
            }
        }
        TransitionManager.beginDelayedTransition(root)
        val constraint = if (currentSet) constraintSet1 else constraintSet2
        constraint.applyTo(root)
        val colorFrom = if (currentSet) resources.getColor(R.color.red) else resources.getColor(R.color.blue)
        val colorTo = if (currentSet) resources.getColor(R.color.blue) else resources.getColor(R.color.red)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds
        colorAnimation.addUpdateListener { animator -> current_move_line.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
        currentSet = !currentSet
    }
}
