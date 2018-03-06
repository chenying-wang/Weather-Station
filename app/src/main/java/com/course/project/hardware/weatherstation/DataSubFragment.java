package com.course.project.hardware.weatherstation;

import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

abstract class DataSubFragment extends Fragment{

    protected static int historyLength = 5;
    protected boolean isHere;
    protected MainActivity mActivity;
    protected RelativeLayout mRelativeLayout;
    protected ListView listView;
    protected SimpleAdapter simpleAdapter;
    protected ArrayList<Map<String, Object>> data;
    protected double currentData = Constants.INVALID;
    protected double averageData;
    protected Date currentTime;
    protected int dataUnit;
    protected DecimalFormat decimalFormat;
    private DateFormat dateFormat;

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        isHere = true;
    }

    @CallSuper
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isHere = true;
        }
        else {
            isHere = false;
        }
    }

    protected static void setHistoryLength(int settingOptionId) {
        switch (settingOptionId) {
            case Constants.SETTING_HISTORY_5:
                historyLength = 5;
                break;
            case Constants.SETTING_HISTORY_10:
                historyLength = 10;
                break;
            case Constants.SETTING_HISTORY_15:
                historyLength = 15;
                break;
            case Constants.SETTING_HISTORY_20:
                historyLength = 20;
                break;
        }
    }

    protected void init() {
        isHere = false;
        mActivity = (MainActivity) getActivity();
        data = new ArrayList<>();
        dateFormat = new SimpleDateFormat("HH:mm:ss.SSS",
                ((LanguageApplication) mActivity.getApplication()).getLocale());
    }

    protected boolean addData() {
        int size = data.size();
        if(currentData == Constants.INVALID) return false;
        Map<String, Object> map = new HashMap<>();
        map.put("ACTUAL_DATA", currentData);
        map.put("DATA", formatData(currentData, dataUnit));
        map.put("TIME", dateFormat.format(currentTime));

        if(size == 0) {
            data.add(map);
            refreshAverage();
            return true;
        }
        else if(size < historyLength) {
            data.add(0, map);
            refreshAverage();
            return true;
        }

        while(data.size() > historyLength-1)data.remove(historyLength-1);
        data.add(0, map);
        refreshAverage();
        return false;
    }

    protected void refreshAverage() {
        double average = .0d;
        for (Map<String, Object> historyData : data) {
            average += ((Double) historyData.get("ACTUAL_DATA"));
        }
        average /= ((double) data.size());
        averageData = average;
    }

    abstract protected Handler getHandler();

    abstract protected void refreshData(boolean adjustHeight);

    abstract protected void refreshData();

    abstract protected String formatData(double data, int dataUnit);

}
