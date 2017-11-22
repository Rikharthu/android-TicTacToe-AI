package com.example.rikharthu.tictactoe.tictac

class TicTacAIPlayer(seed: Seed?) : TicTacPlayer(seed) {

    val aiPlayer: AIPlayer = AIPlayer(seed!!)

    override fun move(row: Int, column: Int) {
        super.move(row, column)
    }

    override fun onCanMove() {
        // TODO calculate with minimax
        val index = aiPlayer.minimax(game.board, seed).index
        val row = index / 3
        val column = index % 3
        move(row, column)
    }
}