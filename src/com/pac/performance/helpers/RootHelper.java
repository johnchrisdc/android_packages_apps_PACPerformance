package com.pac.performance.helpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.annotation.SuppressLint;
import android.util.Log;

import com.pac.performance.utils.Constants;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class RootHelper implements Constants {

    public enum PartitionType {
        BOOT, RECOVERY, FOTA
    }

    private String bootPartition;
    private String recoveryPartition;
    private String fotaPartition;

    private String[] bootPartitionNames = new String[] { "boot", "kernel", "LX" };
    private String[] recoveryPartitionNames = new String[] { "recovery", "SS" };
    private String[] fotaPartitionNames = new String[] { "FOTAKernel" };

    public void runCommand(String command) {
        try {
            RootTools.getShell(true).add(new CommandCapture(0, command))
                    .commandCompleted(0, 0);
            if (command.contains(Constants.TMP_FILE)) RootTools.getShell(true)
                    .add(new CommandCapture(0, "chmod 777 " + Constants.TMP_FILE))
                    .commandCompleted(0, 0);
            Log.d(TAG, command);
        } catch (IOException e) {
            Log.e(TAG, "failed to run " + command);
        } catch (TimeoutException ignored) {
            Log.e(TAG, "Timeout: Cannot gain root access");
        } catch (RootDeniedException e) {
            Log.e(TAG, "Root access denied");
        }
    }

    public boolean rootAccess() {
        return RootTools.isAccessGiven();
    }

    public boolean busyboxInstalled() {
        return RootTools.isBusyboxAvailable();
    }

    public void writePartition(String path, String partition) {
        runCommand("dd if=" + path + " of=" + partition);
    }

    public void getPartition(String path, String partition) {
        runCommand("dd if=" + partition + " of=" + path);
    }

    public void mount(boolean writeable, String mountpoint) {
        runCommand(writeable ? "mount -o rw,remount " + mountpoint
                : "mount -o ro,remount " + mountpoint);
    }

    // Thanks to Performance Control creators for this code!
    public boolean moduleActive(String module) {
        String output = null;
        try {
            output = getOutput("echo `ps | grep " + module
                    + " | grep -v \"grep " + module + "\" | awk '{print $1}'`",
                    true);
        } catch (IOException e) {
            Log.e(TAG, "failed to get status of " + module);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output != null && output.length() > 0 && !output.equals("error");
    }

    // Thanks to Performance Control creators for this code!
    @SuppressWarnings("deprecation")
    public String getOutput(String command, boolean debug) throws IOException,
            InterruptedException {
        Process process = Runtime.getRuntime().exec("sh");
        final DataOutputStream processStream = new DataOutputStream(
                process.getOutputStream());
        processStream.writeBytes("exec " + command + "\n");
        processStream.flush();

        int exit = 1;
        String output = null;
        if (process != null) {
            exit = process.waitFor();

            StringBuffer buffer = null;
            final DataInputStream inputStream = new DataInputStream(
                    process.getInputStream());

            if (inputStream.available() > 0) {
                buffer = new StringBuffer(inputStream.readLine());
                while (inputStream.available() > 0)
                    buffer.append("\n").append(inputStream.readLine());
            }
            inputStream.close();
            if (buffer != null) output = buffer.toString();
        }

        if (debug) Log.d(TAG, "Output of " + command + ": " + output);

        return exit != 1 && exit == 0 ? output : "error";
    }

    @SuppressLint("DefaultLocale")
    public String getPartitionName(PartitionType partition) {
        /*
         * Read out partitions from /dev/block/platform. That is the easiest way
         * and reading fstab is not always possible.
         */
        if ((partition == PartitionType.BOOT && bootPartition == null)
                || (partition == PartitionType.RECOVERY && recoveryPartition == null)
                || (partition == PartitionType.FOTA && fotaPartition == null)) {

            String[] partitionNames = null;
            switch (partition) {
                case BOOT:
                    partitionNames = bootPartitionNames;
                    break;
                case RECOVERY:
                    partitionNames = recoveryPartitionNames;
                    break;
                case FOTA:
                    partitionNames = fotaPartitionNames;
                    break;
            }

            if (partitionNames != null) {
                String path = mUtils.existFile(PARTITON_PATH + "/omap") ? PARTITON_PATH
                        + "/omap"
                        : PARTITON_PATH;
                if (mUtils.existFile(path)) {
                    File[] platforms = new File(path).listFiles();
                    if (platforms.length > 0) for (File platform : platforms)
                        if (platform.isDirectory()) for (File emmc : platform
                                .listFiles())
                            if (emmc.getName().equals("by-name")) for (File part : emmc
                                    .listFiles())
                                for (String names : partitionNames)
                                    if (names.equals(part.getName())
                                            || names.toUpperCase().equals(
                                                    part.getName())) {
                                        if (partition == PartitionType.BOOT) bootPartition = part
                                                .getAbsolutePath();
                                        if (partition == PartitionType.RECOVERY) recoveryPartition = part
                                                .getAbsolutePath();
                                        if (partition == PartitionType.FOTA) fotaPartition = part
                                                .getAbsolutePath();
                                    }
                }
            }
        }

        if (partition == PartitionType.BOOT) return bootPartition;
        if (partition == PartitionType.RECOVERY) return recoveryPartition;
        if (partition == PartitionType.FOTA) return fotaPartition;
        return null;
    }
}
