package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Activity page where the game is played
 */
public class GamePageActivity extends AppCompatActivity {
    private static final String TAG = GamePageActivity.class.getName();
    private GameViewModel gameViewModel;

    // Views in the current page
    private ImageButton[] imageButtons = new ImageButton[9];
    private TextView mPlayer1TextView;
    private TextView mPlayer2TextView;
    private ImageView mPlayer1ImageView;
    private ImageView mPlayer2ImageView;

    // Constants to identify the two pictures user takes
    private static final int REQUEST_PLAYER1_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PLAYER2_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_page);

        // Identify and set the score and player views
        mPlayer1TextView = findViewById(R.id.player1_score_text);
        mPlayer2TextView = findViewById(R.id.player2_score_text);
        mPlayer1ImageView = findViewById(R.id.player1_image);
        mPlayer2ImageView = findViewById(R.id.player2_image);

        setImageButtons();

        Intent intent = getIntent();

        // Attach GameViewModel to this activity
        gameViewModel = ViewModelProviders.of(this).get(GameViewModel.class);
        gameViewModel.setSinglePlayerOption
                (intent.getBooleanExtra(MainActivity.SINGLE_PLAYER, true));

        // Log the game mode selection
        Log.v(TAG, "Single: " + gameViewModel.getSinglePlayerOption());
        // Log the game mode selected, easy or hard
        Log.v(TAG, "Mode: " + intent.getIntExtra(MainActivity.SINGLE_PLAYER, 0));

        // Take pictures based on the game mode selected. If onCreate() gets called again during a
        // configuration change and user already took a picture, use the previous pictures
        if (gameViewModel.getPlayer1BitmapImage() == null) {
            if (gameViewModel.getSinglePlayerOption()) {
                openCamera(REQUEST_PLAYER1_IMAGE_CAPTURE);
                mPlayer2ImageView.setImageResource(R.drawable.computer);
                gameViewModel.setDifficulty
                        (intent.getIntExtra(MainActivity.SINGLE_PLAYER, 0));
            } else {
                openCamera(REQUEST_PLAYER2_IMAGE_CAPTURE);
                openCamera(REQUEST_PLAYER1_IMAGE_CAPTURE);
            }
        } else {
            if (gameViewModel.getSinglePlayerOption()) {
                mPlayer1ImageView.setImageBitmap(gameViewModel.getPlayer1BitmapImage());
                mPlayer2ImageView.setImageResource(R.drawable.computer);
            } else {
                mPlayer1ImageView.setImageBitmap(gameViewModel.getPlayer1BitmapImage());
                mPlayer2ImageView.setImageBitmap(gameViewModel.getPlayer2BitmapImage());
            }
        }

        onConfigChange();

        // Attach observers to livedata and update player points whenever they change
        gameViewModel.getPlayer1Points().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer points) {
                mPlayer1TextView.setText(String.format(Locale.getDefault(), "%d", points));
            }
        });

        gameViewModel.getPlayer2Points().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer points) {
                mPlayer2TextView.setText(String.format(Locale.getDefault(), "%d", points));
            }
        });
    }

    /** Sets up the ImageButton member variables and their tag */
    private void setImageButtons() {
        for (int i = 0; i < imageButtons.length; i++) {
            String imageViewID = "image_button_" + (i + 1);
            int resourceID = getResources().getIdentifier(imageViewID, "id", getPackageName());
            imageButtons[i] = findViewById(resourceID);
            imageButtons[i].setTag((i + 1));

            // Log the tag of each imageButtons
            Log.v(TAG, "Tag " + imageButtons[i].getTag());
        }
    }

    /**
     * Opens the phone camera to take picture(s)
     *
     * @param requestCode The player(s) the picture is going to be taken for
     */
    private void openCamera(int requestCode) {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pictureIntent, requestCode);
        }
    }

    /**
     * Called after openCamera() successfully executes. Sets the value for the BitmapImage and
     * player ImageView variables
     *
     * @param requestCode The player(s) the picture is going to be taken for
     * @param resultCode  If camera was successful in opening up or not
     * @param data        The camera intent which was passed in from openCamera()
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getExtras().get("data") != null) {
            Bundle extras = data.getExtras();

            switch (requestCode) {
                case REQUEST_PLAYER1_IMAGE_CAPTURE:
                    gameViewModel.setPlayer1BitmapImage((Bitmap) extras.get("data"));
                    mPlayer1ImageView.setImageBitmap(gameViewModel.getPlayer1BitmapImage());
                    break;

                case REQUEST_PLAYER2_IMAGE_CAPTURE:
                    gameViewModel.setPlayer2BitmapImage((Bitmap) extras.get("data"));
                    mPlayer2ImageView.setImageBitmap(gameViewModel.getPlayer2BitmapImage());
                    break;
            }
        } else
            // Go back to main page activity if user does not take picture
            finish();
    }

    /**
     * The onClick function for when an ImageButton is clicked, makes the move by setting the
     * background image.
     *
     * @param view The ImageButton that is being clicked on
     */
    public void placeMove(View view) {
        int tag = Integer.parseInt(view.getTag().toString());

        // Don't place move if current position already has a move in it
        if (gameViewModel.checkTag(tag))
            return;

        // Single Player game logic
        if (gameViewModel.getSinglePlayerOption()) {
            ((ImageButton) view).setImageBitmap(gameViewModel.getPlayer1BitmapImage());
            gameViewModel.setPlayer1Turn();
            moveAI();
        }
        // Multi Player game logic
        else {
            if (gameViewModel.getPlayer1Turn()) {
                ((ImageButton) view).setImageBitmap(gameViewModel.getPlayer1BitmapImage());
                gameViewModel.setPlayer1Turn();
            } else {
                ((ImageButton) view).setImageBitmap(gameViewModel.getPlayer2BitmapImage());
                gameViewModel.setPlayer1Turn();
            }
        }

        // Check for winning conditions after the least required turns needed for a player to win
        if (gameViewModel.getNumOfTurns() > 4)
            checkGameState();
    }

    /** Resets all the points in the game */
    public void resetPoints(View view) {
        gameViewModel.getPlayer1Points().setValue(0);
        gameViewModel.getPlayer2Points().setValue(0);
    }

    /** Resets game to initial state */
    private void resetGameConditions() {
        for (ImageButton imageButton : imageButtons) {
            imageButton.setImageResource(R.color.transparent);
        }
        gameViewModel.resetGame();
    }

    /**
     * When in Single Player mode, uses the Minimax Algorithm in the GameViewModel class
     * to help computer decide where to place a move.
     */
    private void moveAI() {
        if (gameViewModel.getNumOfTurns() == 0)
            return;

        if (gameViewModel.getNumOfTurns() < 9) {
            int[] position = gameViewModel.findBestMove();
            int tag = (position[0] * gameViewModel.getCol()) + (position[1] + 1);
            ImageButton AIImageButton = new ImageButton(this);

            gameViewModel.placeMove(false, position[0], position[1]);

            for (ImageButton button : imageButtons) {
                if ((Integer.parseInt(button.getTag().toString())) == tag) {
                    AIImageButton = button;
                    AIImageButton.setTag(button.getTag());
                    break;
                }
            }

            AIImageButton.setImageResource(R.drawable.computer);

            gameViewModel.checkTag(Integer.parseInt(AIImageButton.getTag().toString()));

            // Log the ImageButton for the AI move
            Log.v(TAG, "AI ImageButton: " + AIImageButton.getTag());
        }

        if (!checkGameState())
            gameViewModel.setPlayer1Turn();
    }

    /**
     * After 5 turns have passed (least number of turns needed for someone to win), checks the
     * current game state for a winner.
     */
    private boolean checkGameState() {
        if (gameViewModel.getNumOfTurns() > 4) {
            int winner = gameViewModel.checkForWinner();

            if (winner == 10) {
                Toast.makeText(this, "Player 1 wins", Toast.LENGTH_LONG).show();
                gameViewModel.player1Wins();
                resetGameConditions();
                return true;
            } else if (winner == -10) {
                Toast.makeText(this, "Player 2 wins", Toast.LENGTH_LONG).show();
                gameViewModel.player2Wins();
                resetGameConditions();
                return true;
            } else if (winner == 0 && gameViewModel.getNumOfTurns() == 9) {
                Toast.makeText(this, "Draw", Toast.LENGTH_LONG).show();
                resetGameConditions();
                return true;
            }
        }
        return false;
    }

    /**
     * Whenever a configuration change occurs and function is called from onCreate(), the board
     * gets repopulated with the images that have been placed on the ImageButton views before the
     * configuration change. Makes it so that it doesn't seem like the board moves reset to the user(s).
     */
    public void onConfigChange() {
        int tag;
        char[][] board = gameViewModel.getBoard();

        for (int row = 0; row < gameViewModel.getRow(); row++) {
            for (int col = 0; col < gameViewModel.getCol(); col++) {
                tag = (row * gameViewModel.getCol()) + (col + 1);

                if (board[row][col] == gameViewModel.getPlayerX()) {
                    for (ImageButton imageButton : imageButtons) {
                        if (Integer.parseInt(imageButton.getTag().toString()) == tag) {
                            imageButton.setImageBitmap(gameViewModel.getPlayer1BitmapImage());
                            break;
                        }
                    }
                } else if (board[row][col] == gameViewModel.getPlayerO()) {
                    for (ImageButton imageButton : imageButtons) {
                        if (Integer.parseInt(imageButton.getTag().toString()) == tag) {
                            if (gameViewModel.getSinglePlayerOption()) {
                                imageButton.setImageResource(R.drawable.computer);
                                break;
                            } else {
                                imageButton.setImageBitmap(gameViewModel.getPlayer2BitmapImage());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}