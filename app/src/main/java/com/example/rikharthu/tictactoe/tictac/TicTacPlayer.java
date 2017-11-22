package com.example.rikharthu.tictactoe.tictac;

public class TicTacPlayer {

    private TicTacGame mGame;
    private Listener mListener;
    private Seed mSeed;
    private boolean mCanMove;

    public TicTacPlayer(Seed seed) {
        mSeed = seed;
    }

    public void move(int row, int column) {
        if (mSeed == Seed.CROSS) {
            mGame.onCrossMove(row, column);
        } else {
            mGame.onNoughtMove(row, column);
        }
        mCanMove = false;
    }

    protected void onCanMove() {
        mCanMove = true;
        mListener.onCanMove();
    }

    public TicTacGame getGame() {
        return mGame;
    }

    public void setGame(TicTacGame game) {
        mGame = game;
    }

    public Seed getSeed() {
        return mSeed;
    }

    public void setSeed(Seed seed) {
        mSeed = seed;
    }

    public Listener getListener() {
        return mListener;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onCanMove();
    }
}
