package com.pac.performance;

import java.util.ArrayList;
import java.util.List;

import com.pac.performance.helpers.RootHelper.PartitionType;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.interfaces.Item;
import com.pac.performance.utils.views.HeaderItem;
import com.pac.performance.utils.views.HeaderListView;
import com.pac.performance.utils.views.ListItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Switch;

public class MainActivity extends Activity implements Constants {

    public static int displayHeight;
    public static int displayWidth;

    private ProgressDialog progress;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawer;
    private ListView mDrawerList;
    private Switch mDrawerSwitch;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private final List<Item> items = new ArrayList<Item>();
    private int curposition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check root access
        if (!rootHelper.rootAccess()) {
            mUtils.toast(getString(R.string.no_root), this);
            finish();
        }

        // Check busybox installation
        if (!rootHelper.busyboxInstalled()) {
            mUtils.toast(getString(R.string.no_busybox), this);
            finish();
        }

        setContentView(R.layout.activity_main);

        // Use Asynctask to speed up launching
        new Initialize().execute(savedInstanceState);
    }

    // Initialize all views
    private void setDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerSwitch = (Switch) findViewById(R.id.drawer_switch);

        HeaderListView adapter = new HeaderListView(this, items);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                selectItem(position);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, 0, 0) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerSwitch.setChecked(mUtils.getBoolean("setonboot", false, this));
        mDrawerSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUtils.saveBoolean("setonboot", mDrawerSwitch.isEnabled(),
                        MainActivity.this);
            }
        });
    }

    // Use list to collect all fragments so we can easily organize them
    private void setFragments() {
        items.clear();

        // Information
        items.add(new HeaderItem(getString(R.string.info)));
        items.add(new ListItem(getString(R.string.time_in_state),
                mTimeInStateFragment));
        items.add(new ListItem(getString(R.string.kernel_info),
                mKernelInformationFragment));

        // Fancy Stats
        if (mMemoryStatsFragment.hasSupport() || cpuHelper.getCoreCount() < 9) items
                .add(new HeaderItem(getString(R.string.fancy_stats)));
        // Cpu stats only supports max. 8 cores
        if (cpuHelper.getCoreCount() < 9) items.add(new ListItem(
                getString(R.string.cpu_stats), mCPUStatsFragment));
        if (mMemoryStatsFragment.hasSupport()) items.add(new ListItem(
                getString(R.string.memory_stats), mMemoryStatsFragment));

        // Kernel
        items.add(new HeaderItem(getString(R.string.kernel)));
        items.add(new ListItem(getString(R.string.cpu), mCPUFragment));
        if (cpuVoltageHelper.hasCpuVoltage()) items.add(new ListItem(
                getString(R.string.cpu_voltage), mCPUVoltageFragment));
        if (gpuHelper.hasGpu()) items.add(new ListItem(getString(R.string.gpu),
                mGPUFragment));
        items.add(new ListItem(getString(R.string.io_scheduler),
                mIOSchedulerFragment));
        items.add(new ListItem(getString(R.string.low_memory_killer),
                mLowMemoryKillerFragment));
        items.add(new ListItem(getString(R.string.virtual_machine),
                mVirtualMachineFragment));

        // Utilities
        items.add(new HeaderItem(getString(R.string.utilities)));
        items.add(new ListItem(getString(R.string.custom_commander),
                mCustomCommanderFragment));
        if (rootHelper.getPartitionName(PartitionType.BOOT) != null
                || rootHelper.getPartitionName(PartitionType.RECOVERY) != null
                || rootHelper.getPartitionName(PartitionType.FOTA) != null) items
                .add(new ListItem(getString(R.string.backup), mBackupFragment));
        items.add(new ListItem(getString(R.string.build_prop),
                mBuildpropFragment));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        // Hide custom command switch
        if (mCustomCommanderFragment.actionBarSwitch != null) mCustomCommanderFragment.actionBarSwitch
                .setVisibility(items.get(position).getFragment() == mCustomCommanderFragment ? View.VISIBLE
                        : View.GONE);

        mDrawerList.setItemChecked(curposition, true);

        if (items.get(position).isHeader()) return;

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, items.get(position).getFragment())
                .commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(items.get(position).getTitle());
        mDrawerLayout.closeDrawer(mDrawer);

        curposition = position;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private class Initialize extends AsyncTask<Bundle, Void, Bundle> {

        @Override
        protected void onPreExecute() {
            getActionBar().hide();

            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage(getString(R.string.loading));
            progress.show();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Bundle result) {
            /* TODO: cover all cores when setting file permissions.
             * This provides coverage only for the first CPU, which
             * may or may not be sufficient depending on the device.
             */
            new Thread() {
                public void run() {

                    String[] files = { String.format(CPU_MAX_FREQ, 0),
                            String.format(CPU_MIN_FREQ, 0) };
                    String govFile = String.format(CPU_SCALING_GOVERNOR, 0);

                    for (String file : files)
                        rootHelper.runCommand("chmod 444 " + file);
                    rootHelper.runCommand("chmod 664 " + govFile);

                    setFragments();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            setDrawer();

                            if (result == null) selectItem(curposition);

                            mDrawer.setBackgroundColor(getResources().getColor(
                                    android.R.color.background_dark));
                            getActionBar().show();

                            mDrawerToggle.syncState();
                            mDrawerLayout.openDrawer(mDrawer);

                            progress.hide();
                        }
                    });
                }
            }.start();

            super.onPostExecute(result);
        }

        @Override
        protected Bundle doInBackground(Bundle... params) {
            return params[0];
        }
    }
}
