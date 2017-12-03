package com.example.rikharthu.tictactoe.tictac

import android.os.Handler
import android.os.Process
import com.example.rikharthu.tictactoe.tictac.Seed.EMPTY
import timber.log.Timber
import java.util.concurrent.*

class MiniMaxAIPlayer(seed: Seed) : Player(seed) {
    val SIMULATED_DELAY = 1500L // TODO calculate how much is left too
    val simulateDelay = true
    private val handler = Handler()

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


    private val humanPlayer: Seed = if (seed == Seed.CROSS) Seed.NOUGHT else Seed.CROSS

    override fun onCanMove() {
        val runnableTask = {
            // TODO also refactor the algorithm to be concurrent too, instead just passing it to a worker thread
            Timber.d("Calculating next move")
            val index = nextMoveMinimax(game.board, seed).index
            val row = index / 3
            val column = index % 3
            val res = handler.post { onNextMoveCalculated(row, column) }
        }
        executorService.execute(runnableTask)

//        val index = nextMoveMinimax(game.board, seed).index
//        val row = index / 3
//        val column = index % 3
//
//        if (simulateDelay) {
//            handler.postDelayed({ move(row, column) }, SIMULATED_DELAY)
//        } else {
//            move(row, column)
//        }
    }

    private fun onNextMoveCalculated(row: Int, column: Int) {
        Timber.d("Next move has been calculated")
        move(row, column)
    }

    private fun nextMoveMinimax(newBoard: Array<Cell>, player: Seed, depth: Int = 0): Move {
        var availSpots = emptyIndexes(newBoard)

        // checks for the terminal states such as win, lose, and tie and returning a value accordingly
        if (winning(newBoard, humanPlayer)) {
            // opponent is winning
            // if enemy wins, make it do as much moves as possible
            // thus loosing at 3rd move is worse than loosing at 10th move
            return Move(-1, -1000 + depth)
        } else if (winning(newBoard, mSeed)) {
            // AI is winning
            return Move(-1, 1000 - depth)
        } else if (availSpots.isEmpty()) {
            // Tie
            return Move(-1, 0)
        }

        // an array to collect all the objects
        var moves = arrayListOf<Move>()

        // loop through available spots

        for (i in availSpots.indices) {
            //create an object for each and store the index of that spot that was stored as a number in the object's index key
            val index = newBoard[availSpots[i].index].index

            // set the empty spot to the current player
            newBoard[availSpots[i].index].seed = player

            //if collect the score resulted from calling nextMoveMinimax on the opponent of the current player
            val score: Int
            if (player == mSeed) {
                var result = nextMoveMinimax(newBoard, humanPlayer, depth + 1);
                score = result.score
            } else {
                var result = nextMoveMinimax(newBoard, mSeed, depth + 1);
                score = result.score
            }

            //reset the spot to empty
            newBoard[availSpots[i].index].seed = EMPTY

            // push the object to the array
            moves.add(Move(index, score))
        }

        // if it is the computer's turn loop over the moves and choose the move with the highest score
        var bestMove = -1 // TODO
        if (player === mSeed) {
            var bestScore = -10000
            for (i in moves.indices) {
                if (moves[i].score > bestScore) {
                    bestScore = moves[i].score
                    bestMove = i
                }
            }
        } else {
            // else loop over the moves and choose the move with the lowest score
            var bestScore = 10000
            for (i in moves.indices) {
                if (moves[i].score < bestScore) {
                    bestScore = moves[i].score;
                    bestMove = i;
                }
            }
        }

        // return the chosen move (object) from the array to the higher depth
        return moves[bestMove]
    }

    private class BackgroundThreadFactory : ThreadFactory {

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "CustomThread" + sTag
            thread.priority = Process.THREAD_PRIORITY_BACKGROUND

            // A exception handler is created to log the exception from threads
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