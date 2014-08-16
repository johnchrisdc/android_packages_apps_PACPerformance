package com.pac.performance.fragments;

import com.pac.performance.utils.CommandControl.CommandType;
import com.pac.performance.utils.Constants;
import com.pac.performance.utils.interfaces.DialogReturn;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class VirtualMachineFragment extends PreferenceFragment implements
        Constants {

    private Preference[] mVMs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
                getActivity());

        mVMs = new Preference[virtualmachineHelper.getVMfiles().size()];
        for (int i = 0; i < virtualmachineHelper.getVMfiles().size(); i++) {
            mVMs[i] = prefHelper.setPreference(virtualmachineHelper
                    .getVMfiles().get(i).replace("_", " "),
                    virtualmachineHelper.getVMValue(virtualmachineHelper
                            .getVMfiles().get(i)), getActivity());

            root.addPreference(mVMs[i]);
        }

        setPreferenceScreen(root);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {

        for (int i = 0; i < virtualmachineHelper.getVMfiles().size(); i++)
            if (preference == mVMs[i]) mDialog.showDialogGeneric(VM_PATH + "/"
                    + virtualmachineHelper.getVMfiles().get(i), preference
                    .getSummary().toString(), new DialogReturn() {
                @Override
                public void dialogReturn(String value) {
                    preference.setSummary(value);
                }
            }, true, 0, CommandType.GENERIC, getActivity());

        return true;
    }

}
