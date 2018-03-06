package com.course.project.hardware.weatherstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    private Context mContext;
    private SharedPreferences mPreferences;
    private RecyclerView mRecyclerView;
    private SettingsRecyclerViewAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        RelativeLayout mRelativeLayout;
        mContext = getContext();
        mRelativeLayout = (RelativeLayout) inflater.inflate(
                R.layout.settings_fragment, container, false);
        mRecyclerView = (RecyclerView) mRelativeLayout.findViewById(R.id.settings_recycler_view);

        return mRelativeLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new SettingsRecyclerViewAdapter((MainActivity) getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerView(mRecyclerView);

        Settings.updateSettingList(mAdapter.getSettingList(), mPreferences);

    }

    @Override
    public void onStart() {
        super.onStart();

        Settings.updateSettingList(mAdapter.getSettingList(), mPreferences);
    }

    public SettingsRecyclerViewAdapter getAdapter() {
        return this.mAdapter;
    }

    public SettingsFragment setPreferences(SharedPreferences preferences) {
        this.mPreferences = preferences;
        return this;
    }

    void applyAllSettings() {
        for (Settings.Setting setting : mAdapter.getSettingList()) {
            if(setting.getId() == Constants.SETTING_LANGUAGE) continue;
            mAdapter.processChangedSetting(setting.getId(), setting.getValue());
        }
    }
}

class SettingsRecyclerViewAdapter extends RecyclerViewAdapter{

    private ArrayList<Settings.Setting> settingList;
    private SharedPreferences mPreferences;

    SettingsRecyclerViewAdapter(MainActivity activity, Fragment fragment) {
            super(activity, fragment);

        settingList = new Settings(mActivity).getSettingList();
        mPreferences = mActivity.getSharedPreferences(
                Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public ConnectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.settings_item, parent, false);

        return new SettingsRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {

        Settings.Setting setting;
        String name, valueName;
        setting = settingList.get(position);
        name = setting.getName();
        valueName = setting.getOptionNameByValue(setting.getValue());

        ((TextView)holder.mView.findViewById(R.id.settings_item_name)).setText(name);
        ((TextView)holder.mView.findViewById(R.id.settings_item_value)).setText(valueName);

        holder.mView.setOnClickListener(new OnClickListener(holder.getLayoutPosition()));
    }

    @Override
    public int getItemCount() {
        return settingList.size();
    }

    private class OnClickListener extends RecyclerViewAdapter.OnClickListener{

        OnClickListener(int position) {
            super (position);
        }

        @Override
        public void onClick(View v) {
            Settings.Setting setting = settingList.get(position);
            new SettingsDialogFragment()
                    .setSetting(mFragment, setting)
                    .show(mFragment.getChildFragmentManager(), "Setting");
        }

    }

    void processChangedSetting(int settingId, int selectedOptionId) {

        Settings.Setting changedSetting;
        Handler handler = null;
        Message message;

        changedSetting = Settings.getSettingById(settingId);
        if(changedSetting == null) {
            Log.d("SETTINGS", "SettingId Not Found");
        }

        changedSetting.setValue(selectedOptionId);
        mPreferences.edit()
                .putInt(String.valueOf(changedSetting.getId()), selectedOptionId)
                    .apply();

        switch (settingId) {
            case Constants.SETTING_LANGUAGE:
                mActivity.languageChange(selectedOptionId);
                return;
            case Constants.SETTING_TEMPERATURE:
                handler = (((DataFragment) mActivity
                        .getFragment(Constants.DATA_PAGE))
                        .getSubFragment(Constants.TEMPERATURE_SUBPAGE)).getHandler();
                break;
            case Constants.SETTING_HUMIDITY:
                handler = (((DataFragment) mActivity
                        .getFragment(Constants.DATA_PAGE))
                        .getSubFragment(Constants.HUMIDITY_SUBPAGE)).getHandler();
                break;
            case Constants.SETTING_PRESSURE:
                handler = (((DataFragment) mActivity
                        .getFragment(Constants.DATA_PAGE))
                        .getSubFragment(Constants.PRESSURE_SUBPAGE)).getHandler();
                break;
            case Constants.SETTING_HISTORY:
                handler = (((DataFragment) mActivity
                        .getFragment(Constants.DATA_PAGE))
                        .getSubFragment(Constants.TEMPERATURE_SUBPAGE)).getHandler();
                break;
            case Constants.SETTING_CUBE:
                handler = ((DataFragment) mActivity
                        .getFragment(Constants.DATA_PAGE)).getHandler();
                break;
        }

        notifyDataSetChanged();

        if(handler != null) {
            message = handler.obtainMessage(settingId, selectedOptionId, -1);
            message.sendToTarget();
        }
    }

    ArrayList<Settings.Setting> getSettingList() {
        return settingList;
    }
}

