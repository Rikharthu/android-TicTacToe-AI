package com.example.rikharthu.tictactoe.tictac

import com.example.rikharthu.tictactoe.tictac.Seed.EMPTY

class AIPlayer(seed: Seed) {
    private val aiPlayer: Seed = seed
    private val humanPlayer: Seed = if (seed == Seed.CROSS) Seed.NOUGHT else Seed.CROSS

    fun minimax(newBoard: Array<Cell>, player: Seed): Move {
        var availSpots = emptyIndexes(newBoard)

        // checks for the terminal states such as win, lose, and tie and returning a value accordingly
        if (winning(newBoard, humanPlayer)) {
            return Move(-1, -10)
        } else if (winning(newBoard, aiPlayer)) {
            return Move(-1, 10)
        } else if (availSpots.isEmpty()) {
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

            //if collect the score resulted from calling minimax on the opponent of the current player
            val score: Int;
            if (player == aiPlayer) {
                var result = minimax(newBoard, humanPlayer);
                score = result.score
            } else {
                var result = minimax(newBoard, aiPlayer);
                score = result.score
            }

            //reset the spot to empty
            newBoard[availSpots[i].index].seed = EMPTY

            // push the object to the array
            moves.add(Move(index, score))
        }

        // if it is the computer's turn loop over the moves and choose the move with the highest score
        var bestMove = -1 // TODO
        if (player === aiPlayer) {
            var bestScore = -10000
            for (i in moves.indices) {
                if (moves[i].score > bestScore) {
                    bestScore = moves[i].score;
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

    fun emptyIndexes(board: Array<Cell>) = board.filter { it.seed == EMPTY }

    fun winning(board: Array<Cell>, player: Seed) =
            (board[0].seed == player && board[1].seed == player && board[2].seed == player) ||
                    (board[3].seed == player && board[4].seed == player && board[5].seed == player) ||
                    (board[6].seed == player && board[7].seed == player && board[8].seed == player) ||
                    (board[0].seed == player && board[3].seed == player && board[6].seed == player) ||
                    (board[1].seed == player && board[4].seed == player && board[7].seed == player) ||
                    (board[2].seed == player && board[5].seed == player && board[8].seed == player) ||
                    (board[0].seed == player && board[4].seed == player && board[8].seed == player) ||
                    (board[2].seed == player && board[4].seed == player && board[6].seed == player)

}