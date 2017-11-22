package com.example.rikharthu.tictactoe.tictac

data class Cell(val index: Int, var seed: Seed) {
    override fun toString(): String {
        return when (seed) {
            Seed.CROSS -> "X"
            Seed.NOUGHT -> "O"
            Seed.EMPTY -> index.toString()
        }
    }
}