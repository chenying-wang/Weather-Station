package com.course.project.hardware.weatherstation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class ConnectFragment extends Fragment {

    private Context mContext;
    private Bluetooth mBluetooth;
    private RelativeLayout mRelativeLayout;
    private RecyclerView mRecyclerView;
    private ConnectRecyclerViewAdapter mAdapter;

    private CompoundButton.OnCheckedChangeListener OnCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        if(mBluetooth.enable() == Constants.BT_IDLE) {
                            buttonView.setChecked(true);
                            buttonView.setText(R.string.on);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        if(mBluetooth.disable() == Constants.BT_OFF) {
                            buttonView.setChecked(false);
                            buttonView.setText(R.string.off);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        mRelativeLayout = (RelativeLayout) inflater.inflate(
                R.layout.connect_fragment, container, false);
        mRecyclerView = (RecyclerView) mRelativeLayout.findViewById(R.id.connect_recycler_view);
        return mRelativeLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Switch mConnectSwitch;
        mConnectSwitch = (Switch) mRelativeLayout.findViewById(R.id.connect_switch);
        mConnectSwitch.setChecked(
                ((MainActivity)getActivity()).getBluetooth().isEnable());
        mConnectSwitch.setText(
                ((MainActivity)getActivity()).getBluetooth().isEnable()?R.string.on:R.string.off);
        mConnectSwitch.setOnCheckedChangeListener(OnCheckedChangeListener);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ConnectRecyclerViewAdapter((MainActivity) getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerView(mRecyclerView);
    }

    public ConnectFragment setBluetooth(Bluetooth bluetooth){
        this.mBluetooth = bluetooth;
        return this;
    }

    public RecyclerViewAdapter getAdapter() {
        return this.mAdapter;
    }

}

class ConnectRecyclerViewAdapter extends RecyclerViewAdapter{

    private Bluetooth mBluetooth;
    private Bluetooth.DevicesList devicesList;

    ConnectRecyclerViewAdapter(MainActivity activity) {
        super(activity);
        mBluetooth = mActivity.getBluetooth();
        devicesList = mBluetooth.getDevicesList();
    }

    @Override
    public ConnectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.connect_item, parent, false);
        return new ConnectRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        String name, hardwareAddress, connected="";
        name = devicesList.getName(position);
        hardwareAddress = devicesList.getAddress(position);
        if(devicesList.getConnectedId() == position) {
            connected = mActivity.getResources().getString(R.string.connected) + " ";
        }
        ((TextView)holder.mView.findViewById(R.id.connect_item_name)).setText(name);
        ((TextView)holder.mView.findViewById(R.id.connect_item_addr)).setText(connected + hardwareAddress);

        holder.mView.setOnClickListener(new OnClickListener(holder.getLayoutPosition()));
    }

    @Override
    public int getItemCount() {
        if(devicesList == null) return 0;
        return devicesList.size();
    }

    private class OnClickListener extends RecyclerViewAdapter.OnClickListener{

        OnClickListener(int position) {
            super(position);
        }

        @Override
        public void onClick(View v) {

            mBluetooth.stopScan();

            Bluetooth bluetooth = mActivity.getBluetooth();
            bluetooth.connect(position);
            devicesList.setConnectedId(position);
            notifyDataSetChanged();

            Snackbar.make(v,
                    mActivity.getResources().getString(R.string.connect_to) + " "
                            + devicesList.getName(position),
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.disconnect, new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mActivity.getBluetooth().disconnect();
                            notifyDataSetChanged();
                        }

                    }).show();
        }

    }

}
