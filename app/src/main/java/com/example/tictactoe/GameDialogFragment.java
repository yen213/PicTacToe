package com.example.tictactoe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Class for opening up the dialog box in the main page
 */
public class GameDialogFragment extends DialogFragment {

    // Required empty constructor
    public GameDialogFragment() {
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppDialogTheme);
        builder.setCancelable(false); // Make dialog not closeable through back button
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog passing in null for the parent view because
        // its going in the dialog layout instead
        builder.setView(inflater.inflate(R.layout.dialog_fragment, null))
                // Add positive button
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    /**
     * Make the dialog box only be closeable when the 'OK' button is pressed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_fragment, container, true);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        setCancelable(false);

        return view;
    }
}
