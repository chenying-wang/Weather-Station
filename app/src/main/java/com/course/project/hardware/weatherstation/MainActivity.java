package com.course.project.hardware.weatherstation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String EXTRA_PAGE = "PREVIOUS_ITEM_ID";

    private boolean languageCheck;
    private Bluetooth mBluetooth;
    private Bluetooth.DevicesList devicesList;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private CustomViewPager mViewPager;
    private TabLayout mTabLayout;
    private FloatingActionButton mFAB;
    private ArrayList<Fragment> fragments;
    private boolean dataStatus[] = new boolean[Constants.SUBPAGE_NUM];
    private int dataItemId = 0;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            DataFragment dataFragment = (DataFragment) getFragment(Constants.DATA_PAGE);
            Handler dataHandler = null;
            Message forwardMsg;
            int dataId = Constants.INVALID;

            switch(msg.what) {
                case Constants.MESSAGE_TEMPERATURE:
                    dataId = Constants.TEMPERATURE_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.TEMPERATURE_SUBPAGE)
                            .getHandler();
                    break;
                case Constants.MESSAGE_HUMIDITY:
                    dataId = Constants.HUMIDITY_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.HUMIDITY_SUBPAGE)
                            .getHandler();
                    break;
                case Constants.MESSAGE_PRESSURE:
                    dataId = Constants.PRESSURE_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.PRESSURE_SUBPAGE)
                            .getHandler();
                    break;
                case Constants.MESSAGE_ACCELERATION:
                    dataId = Constants.ACCELERATION_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.ACCELERATION_SUBPAGE)
                            .getHandler();
                    break;
                case Constants.MESSAGE_GYROSCOPE:
                    dataId = Constants.GYROSCOPE_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.GYROSCOPE_SUBPAGE)
                            .getHandler();
                    break;
                case Constants.MESSAGE_MAGNETIC:
                    dataId = Constants.MAGNETIC_SUBPAGE;
                    dataHandler = dataFragment.getSubFragment(Constants.MAGNETIC_SUBPAGE)
                            .getHandler();
                    break;
            }

            if(dataHandler!=null) {
                forwardMsg = dataHandler.obtainMessage(msg.what, msg.obj);
                if(dataStatus[dataId]) forwardMsg.sendToTarget();
            }

            super.handleMessage(msg);
        }

    };

    private ViewPager.OnPageChangeListener pageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                        case Constants.DATA_PAGE:
                            mTabLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            mTabLayout.setVisibility(View.GONE);
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }

            };

    private NavigationView.OnNavigationItemSelectedListener navListener =
            new NavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_connect:
                            setViewPagerItem(Constants.CONNECT_PAGE);
                            break;
                        case R.id.nav_data:
                            setViewPagerItem(Constants.DATA_PAGE);
                            break;
                        case R.id.nav_settings:
                            setViewPagerItem(Constants.SETTINGS_PAGE);
                            break;
                        case R.id.nav_about:
                            setViewPagerItem(Constants.ABOUT_PAGE);
                            break;
                    }
                    mDrawerLayout.closeDrawer(mNavigationView);
                    return true;
                }

            };

    private FloatingActionButton.OnClickListener fabListener =
            new FloatingActionButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pageId = mViewPager.getCurrentItem();
                    switch(pageId) {
                        case Constants.CONNECT_PAGE:
                            int status = mBluetooth.scan();
                            switch (status) {
                                case Constants.BT_DISCOVERY:
                                    showSnackbar(getResources().getString(R.string.discovering),
                                            Snackbar.LENGTH_INDEFINITE);
                                    break;
                                case Constants.BT_NOT_FOUND:
                                    showSnackbar(getResources().getString(R.string.bluetooth_not_found));
                                    break;
                                case Constants.BT_OFF:
                                    showSnackbar(getResources().getString(R.string.open_bluetooth_first));
                                    break;
                                case Constants.BT_IDLE:
                                    showSnackbar(getResources().getString(R.string.start_discovery_failed));
                                    break;
                            }
                            break;
                        case Constants.DATA_PAGE:
                            onDataFragmentViewPagerChanged();
                            dataStatus[dataItemId] = !dataStatus[dataItemId];
                            onDataFragmentViewPagerChanged();
                            break;
                        case Constants.SETTINGS_PAGE:
                            //...
                            break;
                        case Constants.ABOUT_PAGE:
                            //...
                            break;
                    }
              }

            };

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale locale;
        languageCheck = false;
        locale = ((LanguageApplication)newBase.getApplicationContext()).getLocale();
        if(locale == null) languageCheck = true;
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, locale));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences preferences =
                getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        if(languageCheck) {
            languageChange(preferences
                    .getInt(String.valueOf(Constants.SETTING_LANGUAGE),
                            Constants.SETTING_LANGUAGE_DEFAULT));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        for(int i=0; i<Constants.SUBPAGE_NUM; i++) dataStatus[i]=true;
        View mNavHeader;
        TextView mDeviceName;
        TextView mDeviceAddress;
        mBluetooth = new Bluetooth(this);
        if(mBluetooth.init() != Constants.BT_NOT_FOUND)
            devicesList = mBluetooth.getDevicesList();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.setPriority(Integer.MAX_VALUE);

        mToolbar = (Toolbar) this.findViewById(R.id.tool_bar);
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) this.findViewById(R.id.navigation);
        mViewPager = (CustomViewPager) this.findViewById(R.id.view_pager);
        mViewPager.setPagingEnabled(false);
        mTabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        mFAB = (FloatingActionButton) this.findViewById(R.id.fab);
        mTabLayout.setVisibility(View.GONE);

        mToolbar.setTitle(R.string.app_name);
        mNavHeader = mNavigationView.inflateHeaderView(R.layout.nav_header);
        mDeviceName = (TextView) mNavHeader.findViewById(R.id.device_name);
        mDeviceAddress = (TextView) mNavHeader.findViewById(R.id.device_address);
        mDeviceName.setText(mBluetooth.getName());
        mDeviceAddress.setText(mBluetooth.getAddress());

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.d("Exception", e.toString());
        }

        fragments = new ArrayList<>();
        for (int i=0; i<Constants.PAGE_NUM; i++) fragments.add(new Fragment());
        fragments.set(Constants.CONNECT_PAGE, new ConnectFragment()
                .setBluetooth(mBluetooth));
        fragments.set(Constants.DATA_PAGE, new DataFragment());
        fragments.set(Constants.SETTINGS_PAGE, new SettingsFragment()
                .setPreferences(preferences));
        fragments.set(Constants.ABOUT_PAGE, new AboutFragment());

        ArrayList<String> titles = new ArrayList<>();
        for(int i=0; i<Constants.PAGE_NUM; i++) titles.add(new String());
        titles.set(Constants.CONNECT_PAGE, getString(R.string.nav_connect));
        titles.set(Constants.DATA_PAGE, getString(R.string.nav_data));
        titles.set(Constants.SETTINGS_PAGE, getString(R.string.nav_settings));
        titles.set(Constants.ABOUT_PAGE, getString(R.string.nav_about));
        FragmentAdapter adapter =
            new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(Constants.PAGE_NUM - 1);
        mViewPager.setLayoutDirection(ViewPager.LAYOUT_DIRECTION_LOCALE);

        mViewPager.addOnPageChangeListener(pageChangeListener);
        mFAB.setOnClickListener(fabListener);
        mNavigationView.setNavigationItemSelectedListener(navListener);

        int previousItemeId = getIntent().getIntExtra(EXTRA_PAGE, Constants.CONNECT_PAGE);
        setViewPagerItem(previousItemeId);
        switch (previousItemeId) {
            case Constants.CONNECT_PAGE:
                mNavigationView.setCheckedItem(R.id.nav_connect);
                break;
            case Constants.DATA_PAGE:
                mNavigationView.setCheckedItem(R.id.nav_data);
                break;
            case Constants.SETTINGS_PAGE:
                mNavigationView.setCheckedItem(R.id.nav_settings);
                break;
            case Constants.ABOUT_PAGE:
                mNavigationView.setCheckedItem(R.id.nav_about);
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new TimerThread()).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.ACCESS_BLUETOOTH:
                if (hasAllPermissionsGranted(grantResults)) {
                    Log.d("PERMISSION", "BLUETOOTH PERMISSION GRANTED");
                }
                else {
                    Log.d("PERMISSION", "BLUETOOTH PERMISSION DENIED");
                }
                break;
            case Constants.ACCESS_LOCATION:
                if (hasAllPermissionsGranted(grantResults)) {
                    Log.d("PERMISSION", "LOCATION PERMISSION GRANTED");
                }
                else {
                    Log.d("PERMISSION", "LOCATION PERMISSION DENIED");
                }
                break;
        }
    }

    public Fragment getFragment(int id) {
        return fragments.get(id);
    }

    public Bluetooth getBluetooth() {
        return this.mBluetooth;
    }

    public Handler getHandler () {
        return mHandler;
    }

    public Bluetooth.DevicesList getDevicesList() {
        return this.devicesList;
    }

    void languageChange(int optionId) {
        Locale locale = null;

        switch(optionId) {
            case Constants.SETTING_LANGUAGE_ENGLISH:
                locale = Locale.US;
                break;
            case Constants.SETTING_LANGUAGE_CHINESE_SIMPLIFIED:
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
        }
        if(locale == null) ((LanguageApplication)getApplicationContext()).resetLocale();
        else ((LanguageApplication)getApplicationContext()).setLocale(locale);
        finish();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_PAGE,
                mViewPager!=null ? mViewPager.getCurrentItem() : 0);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    void onDataFragmentViewPagerChanged(int id) {
        dataItemId = id;

        if(dataStatus[dataItemId]) mFAB.setImageResource(R.drawable.ic_pause_white_24dp);
        else mFAB.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }

    void onDataFragmentViewPagerChanged() {
        onDataFragmentViewPagerChanged(
                ((DataFragment) getFragment(Constants.DATA_PAGE)).getViewPagerItem());
    }

    void showSnackbar(String msg, int duration) {
        Snackbar.make(this.findViewById(R.id.main_content), msg, duration).show();
    }

    void showSnackbar(String msg) {
        showSnackbar(msg, Snackbar.LENGTH_LONG);
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void setViewPagerItem(int item) {
        switch (item) {
            case Constants.CONNECT_PAGE:
                mToolbar.setSubtitle(R.string.nav_connect);
                mFAB.setImageResource(R.drawable.ic_search_white_24dp);
                mFAB.setVisibility(View.VISIBLE);
                break;
            case Constants.DATA_PAGE:
                mToolbar.setSubtitle(R.string.nav_data);
                onDataFragmentViewPagerChanged();
                mFAB.setVisibility(View.VISIBLE);
                break;
            case Constants.SETTINGS_PAGE:
                mToolbar.setSubtitle(R.string.nav_settings);
                mFAB.setVisibility(View.GONE);
                break;
            case Constants.ABOUT_PAGE:
                mToolbar.setSubtitle(R.string.nav_about);
                mFAB.setVisibility(View.GONE);
                break;
        }
        mViewPager.setCurrentItem(item, false);
    }

    private class TimerThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                mBluetooth.readAll();
            }
        }
    }

}

class LanguageContextWrapper extends ContextWrapper {

    public LanguageContextWrapper(Context base) {
        super(base);
    }

    @SuppressWarnings("deprecation")
    public static ContextWrapper wrap(Context context, Locale locale) {
        Configuration config = context.getResources().getConfiguration();

        if (locale!=null) {
            Locale.setDefault(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config, locale);
            }
            else {
                setSystemLocaleLegacy(config, locale);
            }

            context = context.createConfigurationContext(config);


        }
        return new LanguageContextWrapper(context);
    }

    @SuppressWarnings("deprecation")
    public static void setSystemLocaleLegacy(Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale){
        config.setLocale(locale);
    }
}
