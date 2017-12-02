package com.example.rikharthu.tictactoe.tictac

fun emptyIndexes(board: Array<Cell>) = board.filter { it.seed == Seed.EMPTY }

fun winning(board: Array<Cell>, player: Seed) =
        (board[0].seed == player && board[1].seed == player && board[2].seed == player) ||
                (board[3].seed == player && board[4].seed == player && board[5].seed == player) ||
                (board[6].seed == player && board[7].seed == player && board[8].seed == player) ||
                (board[0].seed == player && board[3].seed == player && board[6].seed == player) ||
                (board[1].seed == player && board[4].seed == player && board[7].seed == player) ||
                (board[2].seed == player && board[5].seed == player && board[8].seed == player) ||
                (board[0].seed == player && board[4].seed == player && board[8].seed == player) ||
                (board[2].seed == player && board[4].seed == player && board[6].seed == player)