package com.course.project.hardware.weatherstation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Date;

public class TempSubDataFragment extends DataSubFragment {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            boolean adjHeight;
            switch(msg.what) {
                case Constants.MESSAGE_TEMPERATURE:
                    adjHeight = addData();
                    currentData = (Double) msg.obj;
                    currentTime = new Date();
                    refreshData(adjHeight);
                    break;
                case Constants.SETTING_TEMPERATURE:
                    dataUnit = msg.arg1;
                    break;
                case Constants.SETTING_HISTORY:
                    //DataSubFragment.historyLength = msg.arg1;
                    DataSubFragment.setHistoryLength(msg.arg1);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        init();

        mRelativeLayout = (RelativeLayout) inflater.inflate(
                R.layout.data_fragment_temperature, container, false);
        return mRelativeLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = (ListView) mRelativeLayout.findViewById(R.id.data_temperature_list);
        simpleAdapter = new SimpleAdapter(mActivity, data,
                R.layout.temperature_listview_item,
                new String[]{"DATA", "TIME"},
                new int[]{R.id.data_temperature_list_data, R.id.data_temperature_list_time});
        listView.setAdapter(simpleAdapter);
        listView.setEnabled(false);

        ListView anchor = (ListView) mRelativeLayout.findViewById(R.id.data_temperature_anchor);
        anchor.setAdapter(simpleAdapter);
    }

    @Override
    protected Handler getHandler() {
        return handler;
    }

    @Override
    protected void refreshData(boolean adjustHeight) {
        if(!isHere) return;

        TextView dataView = (TextView) mRelativeLayout
                .findViewById(R.id.data_temperature);
        TextView averageView = (TextView) mRelativeLayout
                .findViewById(R.id.data_average_temperature);
        dataView.setText(formatData(currentData, dataUnit));
        averageView.setText(formatData(averageData, dataUnit));
        simpleAdapter.notifyDataSetChanged();

        if(adjustHeight) {
            ViewPager viewPager = ((DataFragment) mActivity.getFragment(Constants.DATA_PAGE)).getAdapter()
                    .getViewPager();
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewPager.getLayoutParams();
            float scale = mActivity.getResources().getDisplayMetrics().density;
            layoutParams.height = (int) ((272f + 72f * data.size()) * scale + 0.5f);
            viewPager.setLayoutParams(layoutParams);
        }

    }

    @Override
    protected void refreshData() {
        refreshData(false);
    }

    @Override
    protected String formatData(double data, int dataUnit) {
        switch (dataUnit) {
            case Constants.SETTING_TEMPERATURE_CELSIUS:
                decimalFormat = new DecimalFormat("#0.00");
                return decimalFormat.format(data) + " °C";
            case Constants.SETTING_TEMPERATURE_FAHRENHEIT:
                decimalFormat = new DecimalFormat("#0.00");
                return decimalFormat.format(32d+1.8d*data) + " °F";
            default:
                return "FORMAT_ERROR";
        }
    }

}
