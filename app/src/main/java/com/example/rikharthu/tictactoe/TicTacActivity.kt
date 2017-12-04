package com.example.rikharthu.tictactoe

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintSet
import android.support.transition.TransitionManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.rikharthu.tictactoe.tictac.*
import com.example.rikharthu.tictactoe.utils.getAllFilesInAssetByExtension
import com.example.rikharthu.tictactoe.views.TicTacBoard
import kotlinx.android.synthetic.main.activity_tic_tac.*
import timber.log.Timber
import java.util.*


class TicTacActivity : AppCompatActivity(), TicTacBoard.SquarePressedListener, TicTacGame.UpdateListener {

    // TODO handle case when restart is pressed while AIPlayer is calculating it's next move
    // TODO thus game is restarted, but AIPlayer callbacks fires and makes move, updating the board

    companion object {
        val KEY_PLAYER_SEED = "key_player_seed"
    }

    private lateinit var game: TicTacGame
    private lateinit var currentPlayer: Player
    private lateinit var playerSeed: Seed
    var canMove = false
    private lateinit var constraintSet1: ConstraintSet
    private lateinit var constraintSet2: ConstraintSet
    private var mediaPlayer: MediaPlayer? = null
    var currentSet = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        // TODO null check?
        playerSeed = intent.getSerializableExtra(KEY_PLAYER_SEED) as Seed

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

    private fun playChalkSound() {
        val chalkSounds = getAllFilesInAssetByExtension(this, "sounds/chalk", ".mp3")
        val chalkSoundFileName = chalkSounds!![Random().nextInt(chalkSounds.size)]
        val descriptor = assets.openFd("sounds/chalk/$chalkSoundFileName")

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setVolume(1.0f, 1.0f)
        } else {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
        val start = descriptor.startOffset
        val end = descriptor.length
        mediaPlayer!!.setDataSource(descriptor.fileDescriptor, start, end)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
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
            android.R.id.home -> {
                onGoBack(true)
            }
            else -> {
            }
        }

        return true
    }

    private fun onGoBack(showPrompt: Boolean) {
        if (showPrompt) {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        finish()
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        // do nothing
                    }
                }
            }
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to end your game and return to the menu?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        onGoBack(true)
    }

    private fun restartGame() {
        ticTacBoard.clear()

        val player1 = Player(playerSeed)
        val player2 = MiniMaxAIPlayer(if (playerSeed == Seed.CROSS) Seed.NOUGHT else Seed.CROSS)
        player2.simulateDelay = true
        player2.simulatedDelayLength = 1000L
        game = TicTacGame(player1, player2)
        player1.listener = (object : Player.Listener {
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

    private var noughtScore: Int = 0
    private var crossScore: Int = 0

    override fun onStateChanged(state: State) {
        Timber.d("State changed to $state")
        val message: String
        when (state) {
            State.NOUGHT_WON -> {
                // TODO store player and ai score instead of nought and cross score?
                // TODO show dialog to restart the game
                noughtScore++
                Toast.makeText(this, "Nought Won!", Toast.LENGTH_SHORT).show()
                updateScoreboard()
                message = "Nought wins!"
            }
            State.CROSS_WON -> {
                crossScore++
                Toast.makeText(this, "Cross Won!", Toast.LENGTH_SHORT).show()
                updateScoreboard()
                message = "Cross wins!"
            }
            State.TIE -> message = "Tie!"
            else -> {
                message = ""
            }
        }

        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                restartGame()
            }
        }
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
                .setPositiveButton("New Game", dialogClickListener)
                .show()
    }

    private fun updateScoreboard() {
        crossScoreTv.text = crossScore.toString()
        noughtScoreTv.text = noughtScore.toString()
    }

    override fun onBoardUpdated(board: Array<Cell>) {
        Timber.d("Board has been updated")
        playChalkSound()
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
