package com.pac.performance.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.pac.performance.R;
import com.pac.performance.utils.Constants;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CPUStatsFragment extends Fragment implements Constants {

    private View rootView;
    private LinearLayout layout;
    private LinearLayout cpulayout;
    private final Handler hand = new Handler();
    private GraphView[] graphView;
    private GraphViewSeries[] series;
    private TextView[] cpus;
    private TextView usageText;

    private String top;
    private String[] modifiedFreqs;

    private Integer[][] y;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fancy_stats, container, false);
        layout = (LinearLayout) rootView.findViewById(R.id.layout);
        cpulayout = (LinearLayout) rootView.findViewById(R.id.info_layout);
        usageText = (TextView) rootView.findViewById(R.id.text_right);

        recreate();

        return rootView;
    }

    @SuppressWarnings("deprecation")
    private void recreate() {

        if (layout != null) layout.removeAllViews();
        if (cpulayout != null) cpulayout.removeAllViews();

        y = new Integer[cpuHelper.getCoreCount()][10];
        for (int i = 0; i < y.length; i++)
            for (int x = 0; x < y[i].length; x++)
                y[i][x] = 0;

        LinearLayout[] l = new LinearLayout[cpuHelper.getCoreCount() / 2];
        for (int i = 0; i < cpuHelper.getCoreCount() / 2; i++) {
            l[i] = new LinearLayout(getActivity());
            l[i].setGravity(Gravity.CENTER);
            layout.addView(l[i]);
        }

        if (cpuHelper.getCoreCount() == 1) {
            l = new LinearLayout[1];
            l[0] = new LinearLayout(getActivity());
            l[0].setGravity(Gravity.CENTER);
            layout.addView(l[0]);
        }

        Display display = ((WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();

        int displaywidth = display.getWidth();
        int displayheight = display.getHeight();

        int width = displaywidth;
        int height = ((int) Math.round(displayheight / 1.3));

        int textsize = 20;

        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                width = displaywidth;
                height = ((int) Math.round(displayheight / 1.5));
                textsize = 15;
                break;
        }

        if (cpuHelper.getCoreCount() > 1) {
            width = width / 2;
            height = height / cpuHelper.getCoreCount()
                    * cpuHelper.getCoreCount() / (cpuHelper.getCoreCount() / 2);
        }

        modifiedFreqs = new String[cpuHelper.getCpuFreqs().length];
        List<String> freqs = new ArrayList<String>();
        for (int x = modifiedFreqs.length - 1; x >= 0; x--)
            freqs.add(cpuHelper.getCpuFreqs()[x]);
        for (int i = 0; i < modifiedFreqs.length; i++)
            modifiedFreqs[i] = (Integer.parseInt(freqs.get(i)) / 1000)
                    + getString(R.string.mhz);

        graphView = new GraphView[cpuHelper.getCoreCount()];
        series = new GraphViewSeries[cpuHelper.getCoreCount()];
        for (int i = 0; i < cpuHelper.getCoreCount(); i++) {
            graphView[i] = new LineGraphView(getActivity(), getString(
                    R.string.core, i));
            ((LineGraphView) graphView[i]).setDrawBackground(true);

            graphView[i].setLayoutParams(new ViewGroup.LayoutParams(width,
                    height));

            GraphViewData[] data = new GraphViewData[y[i].length];
            for (int x = 0; x < y[0].length; x++)
                data[x] = new GraphViewData(x, y[i][x]);
            series[i] = new GraphViewSeries(data);

            graphView[i].setHorizontalLabels(new String[] {});

            graphView[i].setVerticalLabels(modifiedFreqs);
            graphView[i].setManualYAxisBounds(
                    cpuHelper.getCpuFreqs().length - 1, 0);

            graphView[i].getGraphViewStyle().setVerticalLabelsWidth(100);
            graphView[i].getGraphViewStyle().setTextSize(textsize);

            graphView[i].addSeries(series[i]);
        }

        for (int i = 0; i < 2; i++) {
            switch (cpuHelper.getCoreCount()) {
                case 2:
                    l[0].addView(graphView[i]);
                    break;
                case 4:
                    l[0].addView(graphView[i]);
                    l[1].addView(graphView[i + 2]);
                    break;
                case 6:
                    l[0].addView(graphView[i]);
                    l[1].addView(graphView[i + 2]);
                    l[2].addView(graphView[i + 4]);
                case 8:
                    l[0].addView(graphView[i]);
                    l[1].addView(graphView[i + 2]);
                    l[2].addView(graphView[i + 4]);
                    l[3].addView(graphView[i + 6]);
                    break;
            }
        }

        if (cpuHelper.getCoreCount() == 1) l[0].addView(graphView[0]);

        cpus = new TextView[cpuHelper.getCoreCount()];
        for (int i = 0; i < cpuHelper.getCoreCount(); i += 2) {

            LinearLayout cpul = new LinearLayout(getActivity());

            cpus[i] = new TextView(getActivity());
            cpul.addView(cpus[i]);

            if (cpuHelper.getCoreCount() != 1) {
                TextView space = new TextView(getActivity());
                space.setText(" ");
                cpul.addView(space);

                cpus[i + 1] = new TextView(getActivity());
                cpul.addView(cpus[i + 1]);
            }

            cpulayout.addView(cpul);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        recreate();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        hand.post(run);
        hand.post(run2);
        super.onResume();
    }

    Runnable run2 = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < cpuHelper.getCoreCount(); i++) {
                for (int x = 0; x < y[0].length - 1; x++)
                    y[i][x] = y[i][x + 1];

                int curfreq = cpuHelper.getCurFreq(i);
                Context context = getActivity();
                if (context != null) {
                    cpus[i].setText(curfreq == 0 ? getString(R.string.core, i)
                            + ": " + getString(R.string.offline) : getString(
                            R.string.core, i)
                            + ": "
                            + (curfreq / 1000)
                            + getString(R.string.mhz));

                    List<Integer> freqs = new ArrayList<Integer>();

                    for (String freq : cpuHelper.getCpuFreqs())
                        freqs.add(Integer.parseInt(freq));

                    y[i][y[i].length - 1] = freqs.indexOf(curfreq);

                    GraphViewData[] data = new GraphViewData[y[i].length];
                    for (int x = 0; x < y[0].length; x++)
                        data[x] = new GraphViewData(x, y[i][x]);

                    series[i].resetData(data);
                }
            }
            hand.postDelayed(run2, 1000);
        }
    };

    Runnable run = new Runnable() {
        @Override
        public void run() {

            new Thread() {
                public void run() {
                    try {
                        top = rootHelper.getOutput("top -n 1 -m 15", false);

                        Context context = getActivity();
                        if (context != null) ((Activity) context)
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (top != null && !top.equals("error")) {
                                            int usage = 0;
                                            String[] loads = top.trim().split(
                                                    "Name")[1].split(" ");
                                            for (String load : loads) {
                                                if (load.contains("%")) {
                                                    String usages = load
                                                            .split("%")[0];
                                                    usage += Integer
                                                            .parseInt(usages);
                                                }
                                            }
                                            if (usageText != null) usageText
                                                    .setText(getString(R.string.usage)
                                                            + ": "
                                                            + usage
                                                            + "%");
                                            top = null;

                                            hand.post(run);
                                        }
                                    }
                                });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
    };

    public void onDestroy() {
        hand.removeCallbacks(run);
        hand.removeCallbacks(run2);
        super.onDestroy();
    };

}
