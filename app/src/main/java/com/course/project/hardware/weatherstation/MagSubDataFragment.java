package com.course.project.hardware.weatherstation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MagSubDataFragment extends DataSubFragment {

    private double[] data = new double[3];

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what) {
                case Constants.MESSAGE_MAGNETIC:
                    for (int i = 0; i < 3; i++) data[i] = (double) ((ArrayList) msg.obj).get(i);
                    refreshData();
                    break;
            }

            super.handleMessage(msg);
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
                R.layout.data_fragment_magnetic, container, false);
        return mRelativeLayout;
    }

    @Override
    protected Handler getHandler() {
        return handler;
    }

    @Override
    protected void refreshData(boolean adjustHeight) {
        //GLFragment glFragment = ((DataFragment) getParentFragment()).getGlFragment();
        //if(glFragment!=null) glFragment.refreashAcc(new float[]{(float) g[0], (float) g[1], (float) g[2]});

        if(!isHere) return;
        TextView textViewX = (TextView) mRelativeLayout.findViewById(R.id.data_magnetic_x);
        TextView textViewY = (TextView) mRelativeLayout.findViewById(R.id.data_magnetic_y);
        TextView textViewZ = (TextView) mRelativeLayout.findViewById(R.id.data_magnetic_z);
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
        decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(data) + "μT";
    }

    protected String formatData(double data) {
        return formatData(data, Constants.INVALID);
    }

}
