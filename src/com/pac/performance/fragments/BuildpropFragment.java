package com.pac.performance.fragments;

import java.util.ArrayList;
import java.util.List;

import com.pac.performance.R;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.views.GenericListView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class BuildpropFragment extends Fragment implements Constants {

    private ListView list;

    private final List<String> keys = new ArrayList<String>();
    private final List<String> values = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        list = new ListView(getActivity());
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                addKeyDialog(keys.get(position), values.get(position), true);
            }
        });
        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                deleteDialog(keys.get(position), values.get(position));
                return true;
            }
        });
        refresh();

        return list;
    }

    private void refresh() {
        keys.clear();
        values.clear();

        String[] props = mUtils.readFile(BUILD_PROP).split("\\r?\\n");

        if (props.length > 1) {
            for (String prop : props)
                if (!prop.isEmpty() && !prop.startsWith("#")) {
                    String[] propArray = prop.split("=");
                    keys.add(propArray[0]);
                    String value = propArray.length < 2 ? "" : propArray[1];
                    values.add(value + "\n");
                }

            ArrayAdapter<String> adapter = new GenericListView(getActivity(),
                    keys, values);
            list.setAdapter(adapter);
        }
    }

    private void backup() {
        rootHelper.mount(true, "/system");
        rootHelper
                .runCommand("cp -f " + BUILD_PROP + " " + BUILD_PROP + ".bak");
        rootHelper.mount(false, "/system");
        mUtils.toast(getString(R.string.done_backup, BUILD_PROP + ".bak"),
                getActivity());
    }

    private void addKeyDialog(final String key, final String value,
            final boolean modify) {
        LinearLayout dialogLayout = new LinearLayout(getActivity());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.setPadding(30, 20, 30, 20);

        final EditText keyEdit = new EditText(getActivity());
        if (modify) keyEdit.setText(key.trim());
        else keyEdit.setHint(getString(R.string.key));
        final EditText valueEdit = new EditText(getActivity());
        if (modify) valueEdit.setText(value.trim());
        else valueEdit.setHint(getString(R.string.value));

        dialogLayout.addView(keyEdit);
        dialogLayout.addView(valueEdit);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogLayout)
                .setNegativeButton(getString(android.R.string.cancel),
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {}
                        })
                .setPositiveButton(getString(android.R.string.ok),
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (modify) overwrite(key.trim(), value.trim(),
                                        keyEdit.getText().toString().trim(),
                                        valueEdit.getText().toString().trim());
                                else add(keyEdit.getText().toString().trim(),
                                        valueEdit.getText().toString().trim());
                            }
                        }).show();
    }

    private void deleteDialog(final String key, final String value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_prop, key))
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {}
                        })
                .setPositiveButton(getString(android.R.string.ok),
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                overwrite(key.trim(), value.trim(),
                                        "#" + key.trim(), value.trim());
                            }
                        }).show();
    }

    private void add(final String key, final String value) {
        new Thread() {
            public void run() {
                try {
                    rootHelper.runCommand("rm -f " + Constants.TMP_FILE);
                    rootHelper.mount(true, "/system");
                    Thread.sleep(10);
                    rootHelper.runCommand("echo " + key + "=" + value + " >> "
                            + BUILD_PROP);
                    rootHelper.mount(false, "/system");
                    // Create a tmp file so we know when it is finished
                    rootHelper.runCommand("touch " + Constants.TMP_FILE);
                    while (true) {
                        if (mUtils.existFile(Constants.TMP_FILE)) {
                            rootHelper.runCommand("rm -f " + Constants.TMP_FILE);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                }
                            });
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void overwrite(final String oldkey, final String oldValue,
            final String newKey, final String newValue) {
        new Thread() {
            public void run() {
                try {
                    rootHelper.runCommand("rm -f " + Constants.TMP_FILE);
                    rootHelper.mount(true, "/system");
                    Thread.sleep(10);
                    /*
                     * Use sed command to overwrite props. setprop is not
                     * reliable enough
                     */
                    rootHelper.runCommand("sed 's|" + oldkey + "=" + oldValue
                            + "|" + newKey + "=" + newValue
                            + "|g' -i /system/build.prop");
                    rootHelper.mount(false, "/system");
                    // Create a tmp file so we know when it is finished
                    rootHelper.runCommand("touch " + Constants.TMP_FILE);
                    while (true) {
                        if (mUtils.existFile(Constants.TMP_FILE)) {
                            rootHelper.runCommand("rm -f " + Constants.TMP_FILE);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                }
                            });
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.buildprop, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_backup) backup();
        if (item.getItemId() == R.id.menu_add) addKeyDialog(null, null, false);
        return true;
    }
}
