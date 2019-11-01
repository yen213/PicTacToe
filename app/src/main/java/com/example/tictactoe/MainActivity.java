package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Game's main page activity
 **/
public class MainActivity extends AppCompatActivity {
    private boolean mSinglePlayerOption = true;
    // Constant key to map the value of game mode boolean
    public static final String SINGLE_PLAYER = "com.example.tictactoe.SINGLE_PLAYER";
    DialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        // Animate gradient background for the main page
        ConstraintLayout constraintLayout = findViewById(R.id.main_page_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();

        // Show dialog box to user as soon as game opens up
        dialogFragment = new GameDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "game dialog");

        Button mEasyButton = findViewById(R.id.single_player_easy_button);
        Button mHardButton = findViewById(R.id.single_player_hard_button);
        Button mMultiplayerButton = findViewById(R.id.multiplayer_player_button);

        // Set click listeners on the game mode buttons
        // Pass in 0 to the intent for Easy mode selected
        mEasyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GamePageActivity.class);
                intent.putExtra(SINGLE_PLAYER, mSinglePlayerOption);
                intent.putExtra(SINGLE_PLAYER, 0);
                view.getContext().startActivity(intent);
            }
        });

        // Pass in 1 to the intent for Hard mode selected
        mHardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GamePageActivity.class);
                intent.putExtra(SINGLE_PLAYER, mSinglePlayerOption);
                intent.putExtra(SINGLE_PLAYER, 1);
                view.getContext().startActivity(intent);
            }
        });

        // Multiplayer
        mMultiplayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GamePageActivity.class);
                intent.putExtra(SINGLE_PLAYER, !mSinglePlayerOption);
                view.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Display dialog box again when user navigates back to the main page, as the message in it is
     * essential for getting the app to run as intended
     */
    @Override
    public void onResume() {
        super.onResume();
        dialogFragment = new GameDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "game dialog");
    }
}
