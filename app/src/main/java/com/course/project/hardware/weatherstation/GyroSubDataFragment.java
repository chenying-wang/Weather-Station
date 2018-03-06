package com.course.project.hardware.weatherstation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GyroSubDataFragment extends DataSubFragment {

    private double[] data = new double[3];

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what) {
                case Constants.MESSAGE_GYROSCOPE:
                    for (int i = 0; i < 3; i++) data[i] = (double) ((ArrayList) msg.obj).get(i);
                    refreshData();
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

        mActivity = (MainActivity) getActivity();

        mRelativeLayout = (RelativeLayout) inflater.inflate(
                R.layout.data_fragment_gyroscope, container, false);
        return mRelativeLayout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isHere) {
            FragmentManager fm = getParentFragment().getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.gyro_glview, ((DataFragment) getParentFragment()).newGlFragment());
            ft.commit();
        }
    }

    @Override
    protected Handler getHandler() {
        return handler;
    }

    @Override
    protected void refreshData(boolean adjustHeight) {

        if(!isHere) return;
        TextView textViewX = (TextView) mRelativeLayout.findViewById(R.id.data_gyroscope_x);
        TextView textViewY = (TextView) mRelativeLayout.findViewById(R.id.data_gyroscope_y);
        TextView textViewZ = (TextView) mRelativeLayout.findViewById(R.id.data_gyroscope_z);
        textViewX.setText(formatData(data[0]));
        textViewY.setText(formatData(data[1]));
        textViewZ.setText(formatData(data[2]));
    }

    @Override
    protected void refreshData() {
        refreshData(false);
    }

    @Override
    protected String formatData(double data, int dataUnit) {
        decimalFormat = new DecimalFormat("#0.000");
        return decimalFormat.format(data) + "rad/s";
    }

    protected String formatData(double data) {
        return formatData(data, Constants.INVALID);
    }

}
