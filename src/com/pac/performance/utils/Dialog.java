package com.pac.performance.utils;

import com.pac.performance.R;
import com.pac.performance.utils.CommandControl.CommandType;
import com.pac.performance.utils.interfaces.DialogReturn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Dialog implements Constants {

    public void showDialogList(final String[] modifiedItems,
            final String[] items, final String file,
            final DialogReturn dialogreturn, final CommandType command,
            final Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setItems(modifiedItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int item) {
                mCommandControl.runCommand(items == null ? modifiedItems[item]
                        : items[item], file, command, activity);
                dialogreturn.dialogReturn(modifiedItems[item]);
            }
        }).show();
    }

    public void showDialogGeneric(final String file, String value,
            final DialogReturn dialogreturn, final boolean apply, int input,
            final CommandType command, final Activity activity) {

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);

        final EditText editor = new EditText(activity);
        editor.setGravity(Gravity.CENTER);
        editor.setText(value);

        if (input != 0) editor.setInputType(input);

        layout.addView(editor);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(layout)
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {}
                        })
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (apply) mCommandControl.runCommand(editor.getText()
                                .toString(), file, command, activity);
                        dialogreturn.dialogReturn(editor.getText().toString());
                    }
                }).show();
    }

    public void showSeekBarDialog(final String[] modifiedvalues,
            final String[] values, String currentvalue,
            final DialogReturn dialogreturn, final Activity activity) {

        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_seekbar, null);

        final TextView text = (TextView) view.findViewById(R.id.text_value);
        Button minus = (Button) view.findViewById(R.id.button_minus);
        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekbar);
        Button plus = (Button) view.findViewById(R.id.button_plus);

        text.setText(currentvalue);

        int position = 0;
        for (int i = 0; i < modifiedvalues.length; i++)
            if (modifiedvalues[i].equals(currentvalue)) position = i;

        seekbar.setMax(modifiedvalues.length - 1);
        seekbar.setProgress(position);

        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                text.setText(modifiedvalues[progress]);
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.setProgress(seekbar.getProgress() - 1);
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.setProgress(seekbar.getProgress() + 1);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view)
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {}
                        })
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogreturn.dialogReturn(values[seekbar.getProgress()]);
                    }
                }).show();

    }

}
