package com.pac.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pac.performance.utils.CommandControl.CommandType;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.interfaces.DialogReturn;
import com.pac.performance.utils.views.GenericListView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GenericPathReaderActivity extends Activity implements Constants {

    public static final String ARG_TITLE = "title";
    public static final String ARG_PATH = "path";
    public static final String ARG_ERROR = "error";
    private String path;

    private final List<File> files = new ArrayList<File>();
    private final List<String> values = new ArrayList<String>();
    private final List<String> filesString = new ArrayList<String>();

    private ListView list;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ListView(this);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mDialog.showDialogGeneric(
                        files.get(position).getAbsolutePath(),
                        values.get(position), new DialogReturn() {
                            @Override
                            public void dialogReturn(String value) {
                                refresh();
                            }
                        }, true, 0, CommandType.GENERIC,
                        GenericPathReaderActivity.this);
            }
        });
        setContentView(list);

        // Use Asynctask to speed up launching
        new Initialize().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }

    private class Initialize extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            if (!refresh()) finishHim();
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            // Get args
            getActionBar().setTitle(
                    getIntent().getExtras().getString(ARG_TITLE));
            path = getIntent().getExtras().getString(ARG_PATH);
            super.onPreExecute();
        }

    }

    private boolean refresh() {
        // Remove all items first otherwise we will get duplicated items
        files.clear();
        values.clear();
        filesString.clear();

        // Collecting all files and add them to Lists
        File[] fileArray = new File(path).listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                String value = mUtils.readFile(file.getAbsolutePath());
                if (value != null) {
                    files.add(file);
                    values.add(value);
                    filesString.add(file.getName());
                }
            }
            // Setup adapter
            if (files.size() > 0) {
                adapter = new GenericListView(GenericPathReaderActivity.this,
                        filesString, values);
                list.setAdapter(adapter);
                return true;
            }
        }

        return false;
    }

    // Path does not contain any files
    private void finishHim() {
        mUtils.toast(getIntent().getExtras().getString(ARG_ERROR), this);
        finish();
    }

    // Overwrite onBackPressed for animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(enter_anim, exit_anim);
    }

}
