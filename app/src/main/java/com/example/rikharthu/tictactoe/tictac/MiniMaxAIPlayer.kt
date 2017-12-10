package com.example.rikharthu.tictactoe.tictac

import android.os.Handler
import android.os.Looper
import android.os.Process
import android.support.annotation.WorkerThread
import com.example.rikharthu.tictactoe.tictac.Seed.EMPTY
import timber.log.Timber
import java.util.*
import java.util.concurrent.*

class MiniMaxAIPlayer(seed: Seed) : Player(seed) {
    var simulateDelay = false
    var randomFirstMove = true
    var simulatedDelayLength = 1500L
    private val uiHandler = Handler(Looper.getMainLooper())

    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    private val KEEP_ALIVE_TIME = 1L
    private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    private val taskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
    private val executorService: ExecutorService = ThreadPoolExecutor(NUMBER_OF_CORES,
            NUMBER_OF_CORES * 2,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            taskQueue,
            BackgroundThreadFactory())

    /**
     * Enemy player seed, also know as the Minimizer
     */
    private val enemyPlayer: Seed = if (seed == Seed.CROSS) Seed.NOUGHT else Seed.CROSS

    private var calculationStartedMillis: Long = 0

    override fun onCanMove() {
        val runnableTask = {
            Timber.d("Calculating next move")
            // TODO refactor the algorithm to be concurrent too, instead just passing it to a worker thread
            /* FIXME
            there is a potential bug, if onCanMove() is called multiple times before onNextMove finished
            thus updating calculationStartedMillis
            Possible fix is to calculate time right here and then pass it along as argument to onNextMoveCalculated()
             */

            val t1 = System.currentTimeMillis()
            val index: Int
            if (randomFirstMove && emptyIndexes(game.board).size == 9) {
                index = Random().nextInt(9)
            } else {
                index = nextMoveMinimax(game.board, seed).index
            }
            val t2 = System.currentTimeMillis()
            val delay: Long
            if (simulateDelay) {
                val timeLeft = simulatedDelayLength - (t2 - t1)
                delay = if (timeLeft <= 0)
                    0
                else
                    timeLeft
            } else {
                delay = 0
            }
            val row = index / 3
            val column = index % 3

            // Post to UI thread
            uiHandler.postDelayed({ move(row, column) }, delay)
            Timber.d("Calcultions took: ${t2 - t1}ms")
        }
        executorService.execute(runnableTask)
    }

    override fun onCancelMove() {
        // TODO cancel calculations and pending mUiHandler tasks
        // https://stackoverflow.com/questions/13929618/stop-a-runnable-submitted-to-executorservice
    }

    @WorkerThread
    private fun nextMoveMinimax(board: Array<Cell>, player: Seed, depth: Int = 0): Move {
        var availableSpots = emptyIndexes(board)

        // Check for terminal state
        if (isWinning(board, enemyPlayer)) {
            // opponent is winning (Minimizing)
            // If enemy wins, make it do as much moves as possible
            // thus loosing at 3rd move is worse than loosing at 10th move
            return Move(-1, -1000 + depth)
        } else if (isWinning(board, mSeed)) {
            // AI is winning (Maximizing)
            return Move(-1, 1000 - depth)
        } else if (availableSpots.isEmpty()) {
            // Tie
            return Move(-1, 0)
        }

        var moves = arrayListOf<Move>()

        // loop through available spots
        for (i in availableSpots.indices) {
            // index corresponds to board position (both row and column)
            val index = board[availableSpots[i].index].index

            // set the empty spot to the current player
            board[availableSpots[i].index].seed = player

            // Calculate possible next moves by calling minimax on enemy and on self
            val score: Int
            if (player == mSeed) {
                // What is enemy's next best move
                score = nextMoveMinimax(board, enemyPlayer, depth + 1).score
            } else {
                score = nextMoveMinimax(board, mSeed, depth + 1).score
            }

            //reset the spot to empty
            board[availableSpots[i].index].seed = EMPTY

            moves.add(Move(index, score))
        }

        // If it's AI turn, find the moves with highest score (Maximize)
        var bestMove = -1
        if (player === mSeed) {
            var bestScore = -10000
            for (i in moves.indices) {
                if (moves[i].score > bestScore) {
                    bestScore = moves[i].score
                    bestMove = i
                }
            }
//            Timber.d("Best move for Maximizer is $bestMove with a score of $bestScore")
        } else {
            // Enemy, it's minimizing
            var bestScore = 10000
            for (i in moves.indices) {
                if (moves[i].score < bestScore) {
                    bestScore = moves[i].score
                    bestMove = i
                }
            }
//            Timber.d("Best move for Minimizer is $bestMove with a score of $bestScore")
        }

        // return the chosen move (object) from the array to the higher depth
        return moves[bestMove]
    }

    private class BackgroundThreadFactory : ThreadFactory {

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "CustomThread" + sTag
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND

            // A exception uiHandler is created to log the exception from threads
            thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, ex ->
                Timber.e(thread.name + " encountered an error: " + ex.message)
            }
            return thread
        }

        companion object {
            private val sTag = 1
        }
    }
}