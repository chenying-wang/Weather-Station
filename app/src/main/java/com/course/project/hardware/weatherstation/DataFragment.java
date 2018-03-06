package com.course.project.hardware.weatherstation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class DataFragment extends Fragment{

    private Context mContext;
    private RecyclerView mRecyclerView;
    private DataRecyclerViewAdapter mAdapter;
    private GLFragment mGlFragment;
    private int glMode;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what) {
                case Constants.SETTING_CUBE:
                    glMode = msg.arg1;
                    mGlFragment.refreashMode(msg.arg1);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        RelativeLayout mRelativeLayout;
        mContext = getContext();
        mRelativeLayout = (RelativeLayout) inflater.inflate(
                R.layout.data_fragment, container, false);
        mRecyclerView = (RecyclerView) mRelativeLayout.findViewById(R.id.data_recycler_view);

        return mRelativeLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        newGlFragment();
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new DataRecyclerViewAdapter((MainActivity) getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerView(mRecyclerView);
    }

    DataSubFragment getSubFragment(int id) {
        return (DataSubFragment)mAdapter.getFragments().get(id);
    }

    DataRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    int getViewPagerItem() {
        return getAdapter().getViewPagerItem();
    }

    Handler getHandler() {
        return handler;
    }

    GLFragment newGlFragment() {
        mGlFragment = new GLFragment();
        mGlFragment.refreashMode(glMode);
        return getGlFragment();
    }

    GLFragment getGlFragment() {
        return mGlFragment;
    }

}

class DataRecyclerViewAdapter extends RecyclerViewAdapter {

    private ViewPager mViewPager;
    private ArrayList<Fragment> fragments;
    private TempSubDataFragment temperatureFragment;
    private HumSubDataFragment humidityFragment;
    private PresSubDataFragment pressureFragment;
    private AccSubDataFragment accelerationFragment;
    private GyroSubDataFragment gyroSubDataFragment;
    private MagSubDataFragment magSubDataFragment;

    DataRecyclerViewAdapter(MainActivity activity, Fragment fragment) {
        super(activity);

        this.mFragment = fragment;
    }

    @Override
    public DataRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.data_item, parent, false);
        return new DataRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {

        TabLayout mTabLayout;
        ArrayList<String> titles;

        mViewPager = (ViewPager) holder.mView;

        mTabLayout = (TabLayout) mActivity.findViewById(R.id.tab_layout);

        if(temperatureFragment==null) {
            temperatureFragment = new TempSubDataFragment();
            humidityFragment = new HumSubDataFragment();
            pressureFragment = new PresSubDataFragment();
            accelerationFragment = new AccSubDataFragment();
            gyroSubDataFragment = new GyroSubDataFragment();
            magSubDataFragment = new MagSubDataFragment();
        }

        fragments = new ArrayList<>();
        for(int i=0; i<Constants.SUBPAGE_NUM; i++) fragments.add(new Fragment());
        fragments.set(Constants.TEMPERATURE_SUBPAGE, temperatureFragment);
        fragments.set(Constants.HUMIDITY_SUBPAGE, humidityFragment);
        fragments.set(Constants.PRESSURE_SUBPAGE, pressureFragment);
        fragments.set(Constants.ACCELERATION_SUBPAGE, accelerationFragment);
        fragments.set(Constants.GYROSCOPE_SUBPAGE, gyroSubDataFragment);
        fragments.set(Constants.MAGNETIC_SUBPAGE, magSubDataFragment);

        titles = new ArrayList<>();
        for(int i=0; i<Constants.SUBPAGE_NUM; i++) titles.add(new String());
        titles.set(Constants.TEMPERATURE_SUBPAGE, mActivity.getResources().getString(R.string.data_temp));
        titles.set(Constants.HUMIDITY_SUBPAGE, mActivity.getResources().getString(R.string.data_hum));
        titles.set(Constants.PRESSURE_SUBPAGE, mActivity.getResources().getString(R.string.data_pres));
        titles.set(Constants.ACCELERATION_SUBPAGE, mActivity.getResources().getString(R.string.data_acc));
        titles.set(Constants.GYROSCOPE_SUBPAGE, mActivity.getResources().getString(R.string.data_gyro));
        titles.set(Constants.MAGNETIC_SUBPAGE, mActivity.getResources().getString(R.string.data_mag));

        FragmentAdapter adapter = new FragmentAdapter(mFragment.getChildFragmentManager(), fragments, titles);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(Constants.SUBPAGE_NUM - 1);
        mTabLayout.setupWithViewPager(mViewPager, false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mActivity.onDataFragmentViewPagerChanged(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //mViewPager.scrollBy(mViewPager.getScrollX(), -mViewPager.getScrollY());
                mViewPager.setScrollY(mViewPager.getScrollY());
            }

        });

        ((SettingsFragment) mActivity.getFragment(Constants.SETTINGS_PAGE))
                .applyAllSettings();
    }

    @Override
    public int getItemCount() {
        return 1;
    }


    ArrayList<Fragment> getFragments() {
        return fragments;
    }

    ViewPager getViewPager() {
        return mViewPager;
    }

    int getViewPagerItem() {
        return mViewPager.getCurrentItem();
    }
}
