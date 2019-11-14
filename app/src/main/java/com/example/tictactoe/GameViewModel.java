package com.example.tictactoe;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Class for handling the game's data, algorithms, and states
 */
public class GameViewModel extends ViewModel {
    // Constants
    private static final int noOneWins = -1;
    private static final int ROW = 3;
    private static final int COL = 3;
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';
    private static final String TAG = GameViewModel.class.getName();

    // Game data
    private char[][] mBoard = new char[ROW][COL];
    private MutableLiveData<Integer> mPlayer1Points;
    private MutableLiveData<Integer> mPlayer2Points;
    private Bitmap mPlayer1BitmapImage;
    private Bitmap mPlayer2BitmapImage;
    private int mTurns = 0;
    private boolean mPlayer1Turn = true;
    private boolean mSinglePlayerOption;
    private int mDifficulty;

    // Tags for all the ImageButtons (the 9 positions on the board), used to control game logic
    private static final int TAG_ARRAY_LENGTH = 9;
    private int[] tagArray = new int[TAG_ARRAY_LENGTH];
    private int tagArrayPosition = 0;

    /** Default constructor */
    public GameViewModel() {
        setBoard();
        mPlayer1Points = new MutableLiveData<>(0);
        mPlayer2Points = new MutableLiveData<>(0);
    }

    /**
     * Looks at the current state of the game board (mBoard) for a winning condition in any of the
     * rows, columns, or diagonals. Algorithm checks square board of size N by N.
     *
     * @return 10 if PLAYER_X wins, -10 if PLAYER_O wins, 0 otherwise
     */
    public int checkForWinner() {
        int playerXCount = 0;
        int playerOCount = 0;

        // Check negative sloped diagonal
        for (int i = 0; i < COL; i++) {
            if (!(mBoard[i][i] == ' ')) {
                if (mBoard[i][i] == PLAYER_X)
                    playerXCount++;
                else
                    playerOCount++;
            }
        }

        if (returnWinnerScore(playerXCount, playerOCount) != noOneWins)
            return returnWinnerScore(playerXCount, playerOCount);

        playerOCount = 0;
        playerXCount = 0;

        // Check positive sloped diagonal
        for (int i = 0; i < COL; i++) {
            if (!(mBoard[i][COL - (i + 1)] == ' ')) {
                if (mBoard[i][COL - (i + 1)] == PLAYER_X)
                    playerXCount++;
                else
                    playerOCount++;
            }
        }

        if (returnWinnerScore(playerXCount, playerOCount) != noOneWins)
            return returnWinnerScore(playerXCount, playerOCount);

        playerOCount = 0;
        playerXCount = 0;

        // Check every row
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                if (!(mBoard[row][col] == ' ')) {
                    if (mBoard[row][col] == PLAYER_X)
                        playerXCount++;
                    else
                        playerOCount++;
                }
            }

            if (returnWinnerScore(playerXCount, playerOCount) != noOneWins)
                return returnWinnerScore(playerXCount, playerOCount);

            playerOCount = 0;
            playerXCount = 0;
        }

        // Check every column
        for (int col = 0; col < COL; col++) {
            for (int row = 0; row < ROW; row++) {
                if (!(mBoard[row][col] == ' ')) {
                    if (mBoard[row][col] == PLAYER_X)
                        playerXCount++;
                    else
                        playerOCount++;
                }
            }

            if (returnWinnerScore(playerXCount, playerOCount) != noOneWins)
                return returnWinnerScore(playerXCount, playerOCount);

            playerOCount = 0;
            playerXCount = 0;
        }
        return 0;
    }

    /**
     * Checks the number of moves placed in a row, column, or diagonal by a certain player. If
     * winning move, returns the respective player's score.
     *
     * @param plrX  Count of how many moves Player X has placed
     * @param plrO  Count of how many moves Player O has placed
     *
     * @return      10 for X wins, -10 for O wins, member variable noOneWins otherwise
     */
    private int returnWinnerScore(int plrX, int plrO)
    {
        if (plrX == COL)
            return 10;
        else if (plrO == COL)
            return -10;
        else
            return noOneWins;
    }

    /**
     * Minimax Algorithm to look for a terminal game state (win, lose, or tie), with respect to the
     * current game state. Player trying to win is the maximizing player looking for a positive score
     * and player trying to stop the maximizing player, the minimizer, is looking for a negative
     * score. Both player's scores are adjusted based on the number of turns it took to get that
     * score.
     *
     * @param turns            Current turn or number of moves made so far
     * @param maximizingPlayer Player who is trying to maximize or minimize the score
     * @return The score for the respective player
     */
    private int miniMax(int turns, boolean maximizingPlayer) {
        int score = checkForWinner();

        // Check for terminal cases (recursive call exit conditions)
        if (score == 10)
            return score;
        else if (score == -10)
            return score;
        else if (turns == 9)
            return 0;

        if (maximizingPlayer) {
            int bestValue = Integer.MIN_VALUE;

            for (int row = 0; row < ROW; row++) {
                for (int col = 0; col < COL; col++) {
                    if (mBoard[row][col] == ' ') {
                        mBoard[row][col] = PLAYER_X;
                        bestValue = Math.max
                                (bestValue, miniMax(turns + 1, false));
                        mBoard[row][col] = ' ';
                    }
                }
            }
            return bestValue - turns;
        } else {
            int bestValue = Integer.MAX_VALUE;

            for (int row = 0; row < ROW; row++) {
                for (int col = 0; col < COL; col++) {
                    if (mBoard[row][col] == ' ') {
                        mBoard[row][col] = PLAYER_O;
                        bestValue = Math.min
                                (bestValue, miniMax(turns + 1, true));
                        mBoard[row][col] = ' ';
                    }
                }
            }
            return bestValue + turns;
        }
    }

    /**
     * Uses the miniMax() to calculate the score of every possible move, with respect to the current
     * game state. Compares the scores of all the moves and returns the move with the best score.
     * This function is used by the game's single player mode to make a move. Game always ties or
     * wins when in hard mode and wins, ties, or loses in easy mode.
     *
     * @return The index (row, col) of where to place the move on the mBoard
     */
    public int[] findBestMove() {
        int bestScore = Integer.MAX_VALUE;
        int worstScore = Integer.MIN_VALUE;
        int[] movePositionHard = new int[2];
        int[] movePositionEasy = new int[2];

        // Easy mode
        if (mDifficulty == 0) {
            for (int row = 0; row < ROW; row++) {
                for (int col = 0; col < COL; col++) {
                    if (mBoard[row][col] == ' ') {
                        mBoard[row][col] = PLAYER_X;

                        int currentScore = miniMax(mTurns, false);

                        mBoard[row][col] = ' ';

                        if (currentScore > worstScore) {
                            movePositionEasy[0] = row;
                            movePositionEasy[1] = col;
                            worstScore = currentScore;
                        }
                    }
                }
            }
            return movePositionEasy;
        }
        // Hard mode
        else {
            for (int row = 0; row < ROW; row++) {
                for (int col = 0; col < COL; col++) {
                    if (mBoard[row][col] == ' ') {
                        mBoard[row][col] = PLAYER_O;

                        int currentScore = miniMax(mTurns, true);

                        mBoard[row][col] = ' ';

                        if (currentScore == -10) {
                            movePositionHard[0] = row;
                            movePositionHard[1] = col;

                            return movePositionHard;
                        } else if (currentScore < bestScore) {
                            movePositionHard[0] = row;
                            movePositionHard[1] = col;
                            bestScore = currentScore;
                        }
                    }
                }
            }
            return movePositionHard;
        }
    }

    /**
     * Checks if the ImageButton that gets pressed has already been pressed/holds a move (picture)
     * in it. Using this functions allows for the images in the ImageButtons to not be changed when
     * they are pressed again. If button doesn't hold a move, play the move at the current position
     * and update game data.
     *

     1 | 2 | 3    Hows the tags for the ImageButtons are set up and ordered on the screen.
     ---------
     4 | 5 | 6
     ---------
     7 | 8 | 9

     * @param tag The button that was clicked on
     * @return True if the current button has a move in it, false otherwise
     */
    public boolean checkTag(int tag) {
        int tagIndex = tag - 1;

        // If the tag of the button is in the tagArray[], then return true, else add that tag
        // into the tagArray[]
        for (int i = 0; i < tagArray.length; i++) {
            // Log all the buttons that currently have a move in them
            Log.v(TAG, "tagArray[" + i + "]: " + tagArray[i]);

            if (tag == tagArray[i])
                return true;
        }

        int row = tagIndex / COL;
        int col = tagIndex % COL;

        placeMove(mPlayer1Turn, row, col);
        tagArray[tagArrayPosition] = tag;

        // Log the position in the tagArray[] the tag is being placed into
        Log.v(TAG, "Value inserted to tagArray[" + tagArrayPosition + "]: "
                + tagArray[tagArrayPosition]);

        tagArrayPosition++;
        mTurns++;

        return false;
    }

    /** Sets all the spaces on the board to empty */
    public void setBoard() {
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                mBoard[row][col] = ' ';
            }
        }
    }

    /**
     * Sets a move onto the game mBoard
     *
     * @param turn_X If it's X's turn, mark the mBoard space as 'X', 'O' otherwise
     * @param row    The row to place the move
     * @param col    The column to place the move
     */
    public void placeMove(boolean turn_X, int row, int col) {
        if (turn_X)
            mBoard[row][col] = PLAYER_X;
        else
            mBoard[row][col] = PLAYER_O;
    }

    /** Resets the board and game conditions to initial state */
    public void resetGame() {
        tagArray = new int[TAG_ARRAY_LENGTH];
        tagArrayPosition = 0;
        mTurns = 0;
        mPlayer1Turn = true;
        setBoard();
    }

    /** Update the points for player 1 when they win */
    public void player1Wins() {
        mPlayer1Points.setValue(mPlayer1Points.getValue() + 1);
    }

    /** Update the points for player 2 when they win */
    public void player2Wins() {
        mPlayer2Points.setValue(mPlayer2Points.getValue() + 1);
    }

    /**
     * Getters and setters for member variables below
     */
    public int getCol() {
        return COL;
    }

    public int getRow() {
        return ROW;
    }

    public MutableLiveData<Integer> getPlayer1Points() {
        if (mPlayer1Points == null)
            mPlayer1Points = new MutableLiveData<>(0);

        return mPlayer1Points;
    }

    public MutableLiveData<Integer> getPlayer2Points() {
        if (mPlayer2Points == null)
            mPlayer2Points = new MutableLiveData<>(0);

        return mPlayer2Points;
    }

    public Bitmap getPlayer1BitmapImage() {
        return mPlayer1BitmapImage;
    }

    public void setPlayer1BitmapImage(Bitmap mPlayer1BitmapImage) {
        this.mPlayer1BitmapImage = mPlayer1BitmapImage;
    }

    public Bitmap getPlayer2BitmapImage() {
        return mPlayer2BitmapImage;
    }

    public void setPlayer2BitmapImage(Bitmap mPlayer2BitmapImage) {
        this.mPlayer2BitmapImage = mPlayer2BitmapImage;
    }

    public int getNumOfTurns() {
        return mTurns;
    }

    public boolean getPlayer1Turn() {
        return mPlayer1Turn;
    }

    public void setPlayer1Turn() {
        mPlayer1Turn = !mPlayer1Turn;
    }

    public boolean getSinglePlayerOption() {
        return mSinglePlayerOption;
    }

    public void setSinglePlayerOption(boolean mSinglePlayerOption) {
        this.mSinglePlayerOption = mSinglePlayerOption;
    }

    public char[][] getBoard() {
        return mBoard;
    }

    public char getPlayerX() {
        return PLAYER_X;
    }

    public char getPlayerO() {
        return PLAYER_O;
    }

    public void setDifficulty(int difficulty) {
        // Easy: 0          Hard: 1
        mDifficulty = difficulty;
    }
}
