package com.pac.performance.helpers;

import com.pac.performance.utils.Constants;

public class CPUVoltageHelper implements Constants {

    private String[] mCpuFreqs;

    public String[] getVoltages() {
        if (mUtils.existFile(String.format(CPU_VOLTAGE, 0))) {
            String value = mUtils.readFile(String.format(CPU_VOLTAGE, 0));
            if (value != null) {
                String[] lines = value.split(" mV");
                String[] voltages = new String[lines.length];
                for (int i = 0; i < lines.length; i++)
                    voltages[i] = lines[i].split("mhz: ")[1];
                return voltages;
            }
        }
        return null;
    }

    public String[] getFreqs() {
        if (mCpuFreqs == null) if (mUtils.existFile(String.format(CPU_VOLTAGE,
                0))) {
            String value = mUtils.readFile(String.format(CPU_VOLTAGE, 0));
            if (value != null) {
                String[] lines = value.split(" mV");
                mCpuFreqs = new String[lines.length];
                for (int i = 0; i < lines.length; i++)
                    mCpuFreqs[i] = lines[i].split("mhz: ")[0].trim();
            }
        }
        return mCpuFreqs;
    }

    public boolean hasCpuVoltage() {
        return mUtils.existFile(String.format(CPU_VOLTAGE, 0));
    }

}
