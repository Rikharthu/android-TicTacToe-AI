package com.example.rikharthu.tictactoe.tictac


class TicTacGame(player1: Player, player2: Player) {

    val board: Array<Cell>
    val nought: Player
    val cross: Player
    val currentPlayer: Player? = null
    var updateListener: UpdateListener? = null
    var state: State = State.PLAYING
        get() = state()
        private set


    init {
        if (player1.seed == player2.seed) {
            throw IllegalArgumentException("Player seeds can't be the same")
        } else if (player1.seed == Seed.EMPTY || player2.seed == Seed.EMPTY) {
            throw IllegalArgumentException("Player seed can't be EMPTY")
        }

        if (player1.seed == Seed.NOUGHT) {
            nought = player1
            cross = player2
        } else {
            nought = player2
            cross = player1
        }

        nought.game = this
        cross.game = this

        board = arrayOf(
                emptyCell(0), emptyCell(1), emptyCell(2),
                emptyCell(3), emptyCell(4), emptyCell(5),
                emptyCell(6), emptyCell(7), emptyCell(8))
    }

    fun isEmpty(row: Int, column: Int) = board[cellIndex(row, column)].seed == Seed.EMPTY

    fun start() {
        // cross goes first
        cross.onCanMove()
    }

    fun onNoughtMove(row: Int, column: Int) {
        // TODO check if valid move
        // TODO add checks for isWinning/tie conditions
        val index = cellIndex(row, column)
        board[index].seed = Seed.NOUGHT
        updateListener?.onBoardUpdated(board)
        val newState = state()
        if (newState != State.PLAYING) {
            updateListener?.onStateChanged(newState)
        } else {
            cross.onCanMove()
        }
    }

    fun state(): State {
        if (winning(board, Seed.NOUGHT)) {
            return State.NOUGHT_WON
        } else if (winning(board, Seed.CROSS)) {
            return State.CROSS_WON
        } else if (emptyIndexes(board).isEmpty()) {
            return State.TIE
        }
        return State.PLAYING
    }

    fun winning(board: Array<Cell>, player: Seed) =
            (board[0].seed == player && board[1].seed == player && board[2].seed == player) ||
                    (board[3].seed == player && board[4].seed == player && board[5].seed == player) ||
                    (board[6].seed == player && board[7].seed == player && board[8].seed == player) ||
                    (board[0].seed == player && board[3].seed == player && board[6].seed == player) ||
                    (board[1].seed == player && board[4].seed == player && board[7].seed == player) ||
                    (board[2].seed == player && board[5].seed == player && board[8].seed == player) ||
                    (board[0].seed == player && board[4].seed == player && board[8].seed == player) ||
                    (board[2].seed == player && board[4].seed == player && board[6].seed == player)

    fun emptyIndexes(board: Array<Cell>) = board.filter { it.seed == Seed.EMPTY }

    fun onCrossMove(row: Int, column: Int) {
        val index = cellIndex(row, column)
        board[index].seed = Seed.CROSS
        updateListener?.onBoardUpdated(board)
        val newState = state()
        if (newState != State.PLAYING) {
            updateListener?.onStateChanged(newState)
        } else {
            nought.onCanMove()
        }
    }

    private fun cellIndex(row: Int, column: Int): Int {
        val index = row * 3 + column
        return index
    }

    fun clear() {
        board.forEach { it.seed = Seed.EMPTY }
    }

    fun emptyCell(index: Int) = Cell(index, Seed.EMPTY)

    interface UpdateListener {
        fun onBoardUpdated(board: Array<Cell>)
        fun onStateChanged(state: State)
    }
}