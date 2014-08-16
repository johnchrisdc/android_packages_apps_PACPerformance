package com.pac.performance.fragments;

import com.pac.performance.R;
import com.pac.performance.utils.CommandControl.CommandType;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.interfaces.DialogReturn;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class GPUFragment extends PreferenceFragment implements Constants {

    private final Handler hand = new Handler();

    private Preference mGpu2dCurFreq;
    private Preference mGpu2dMaxFreq;
    private Preference mGpu2dGovernor;

    private String[] mAvailable2dFreqs;

    private Preference mGpu3dCurFreq;
    private Preference mGpu3dMaxFreq;
    private Preference mGpu3dGovernor;

    private String[] mAvailable3dFreqs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PreferenceScreen root = getPreferenceManager()
                .createPreferenceScreen(getActivity());

        new Thread() {
            public void run() {
                if (gpuHelper.hasGpu2dFreqs()) {
                    mAvailable2dFreqs = new String[gpuHelper.getGpu2dFreqs().length];
                    for (int i = 0; i < gpuHelper.getGpu2dFreqs().length; i++)
                        mAvailable2dFreqs[i] = (Integer.parseInt(gpuHelper
                                .getGpu2dFreqs()[i]) / 1000000)
                                + getString(R.string.mhz);
                }

                if (gpuHelper.hasGpu3dFreqs()) {
                    mAvailable3dFreqs = new String[gpuHelper.getGpu3dFreqs().length];
                    for (int i = 0; i < gpuHelper.getGpu3dFreqs().length; i++)
                        mAvailable3dFreqs[i] = (Integer.parseInt(gpuHelper
                                .getGpu3dFreqs()[i]) / 1000000)
                                + getString(R.string.mhz);
                }

                if (gpuHelper.hasGpu2dCurFreq()) {
                    root.addPreference(prefHelper.setPreferenceCategory(
                            getString(R.string.gpu_stats), getActivity()));

                    mGpu2dCurFreq = prefHelper.setPreference(
                            getString(R.string.gpu_2d_cur_freq),
                            (gpuHelper.getGpu2dCurFreq() / 1000000)
                                    + getString(R.string.mhz), getActivity());

                    root.addPreference(mGpu2dCurFreq);
                }

                if (gpuHelper.hasGpu3dCurFreq()) {
                    root.addPreference(prefHelper.setPreferenceCategory(
                            getString(R.string.gpu_stats), getActivity()));

                    mGpu3dCurFreq = prefHelper.setPreference(
                            getString(R.string.gpu_3d_cur_freq),
                            (gpuHelper.getGpu3dCurFreq() / 1000000)
                                    + getString(R.string.mhz), getActivity());

                    root.addPreference(mGpu3dCurFreq);
                }

                if (gpuHelper.hasGpu2dMaxFreq() || gpuHelper.hasGpu2dGovernor()
                        || gpuHelper.hasGpu3dMaxFreq()
                        || gpuHelper.hasGpu3dGovernor()) root
                        .addPreference(prefHelper.setPreferenceCategory(
                                getString(R.string.parameters), getActivity()));

                if (gpuHelper.hasGpu2dMaxFreq()) {
                    mGpu2dMaxFreq = prefHelper.setPreference(
                            getString(R.string.gpu_2d_max_freq),
                            (gpuHelper.getGpu2dMaxFreq() / 1000000)
                                    + getString(R.string.mhz), getActivity());

                    root.addPreference(mGpu2dMaxFreq);
                }

                if (gpuHelper.hasGpu3dMaxFreq()) {
                    mGpu3dMaxFreq = prefHelper.setPreference(
                            getString(R.string.gpu_3d_max_freq),
                            (gpuHelper.getGpu3dMaxFreq() / 1000000)
                                    + getString(R.string.mhz), getActivity());

                    root.addPreference(mGpu3dMaxFreq);
                }

                if (gpuHelper.hasGpu2dGovernor()) {
                    mGpu2dGovernor = prefHelper.setPreference(
                            getString(R.string.gpu_2d_governor),
                            gpuHelper.getGpu2dGovernor(), getActivity());

                    root.addPreference(mGpu2dGovernor);
                }

                if (gpuHelper.hasGpu3dGovernor()) {
                    mGpu3dGovernor = prefHelper.setPreference(
                            getString(R.string.gpu_3d_governor),
                            gpuHelper.getGpu3dGovernor(), getActivity());

                    root.addPreference(mGpu3dGovernor);
                }
            }
        }.start();

        setPreferenceScreen(root);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {

        if (preference == mGpu2dMaxFreq && gpuHelper.hasGpu2dFreqs()) mDialog
                .showDialogList(mAvailable2dFreqs, gpuHelper.getGpu2dFreqs(),
                        gpuHelper.GPU_2D_MAX_FREQ, new DialogReturn() {
                            @Override
                            public void dialogReturn(String value) {
                                preference.setSummary(value);
                            }
                        }, CommandType.GENERIC, getActivity());

        if (preference == mGpu2dGovernor) mDialog.showDialogList(
                gpuHelper.getGpu2dGovernors(), null,
                gpuHelper.GPU_2D_SCALING_GOVERNOR, new DialogReturn() {
                    @Override
                    public void dialogReturn(String value) {
                        try {
                            Thread.sleep(10);
                            preference.setSummary(gpuHelper.getGpu2dGovernor());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, CommandType.GENERIC, getActivity());

        if (preference == mGpu3dMaxFreq && gpuHelper.hasGpu3dFreqs()) mDialog
                .showDialogList(mAvailable3dFreqs, gpuHelper.getGpu3dFreqs(),
                        gpuHelper.GPU_3D_MAX_FREQ, new DialogReturn() {
                            @Override
                            public void dialogReturn(String value) {
                                preference.setSummary(value);
                            }
                        }, CommandType.GENERIC, getActivity());

        if (preference == mGpu3dGovernor) mDialog.showDialogList(
                gpuHelper.getGpu3dGovernors(), null,
                gpuHelper.GPU_3D_SCALING_GOVERNOR, new DialogReturn() {
                    @Override
                    public void dialogReturn(String value) {
                        try {
                            Thread.sleep(10);
                            preference.setSummary(gpuHelper.getGpu3dGovernor());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, CommandType.GENERIC, getActivity());

        return true;
    }

    @Override
    public void onResume() {
        hand.post(run);
        super.onResume();
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {

            if (mGpu2dCurFreq != null) mGpu2dCurFreq.setSummary((gpuHelper
                    .getGpu2dCurFreq() / 1000000) + getString(R.string.mhz));
            if (mGpu2dMaxFreq != null) mGpu2dMaxFreq.setSummary((gpuHelper
                    .getGpu2dMaxFreq() / 1000000) + getString(R.string.mhz));

            if (mGpu3dCurFreq != null) mGpu3dCurFreq.setSummary((gpuHelper
                    .getGpu3dCurFreq() / 1000000) + getString(R.string.mhz));
            if (mGpu3dMaxFreq != null) mGpu3dMaxFreq.setSummary((gpuHelper
                    .getGpu3dMaxFreq() / 1000000) + getString(R.string.mhz));

            hand.postDelayed(run, 1000);
        }
    };

    public void onDestroy() {
        hand.removeCallbacks(run);
        super.onDestroy();
    };
}
