package com.course.project.hardware.weatherstation;

import android.app.Application;

import java.util.Locale;

public class LanguageApplication extends Application{

    private static LanguageApplication instance;
    private static Locale mLocale;
    private static Locale defaultLocale;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        mLocale = null;
        defaultLocale = Locale.getDefault();
    }

    public Locale getLocale() {
        return mLocale;
    }

    public void setLocale(Locale locale) {
        mLocale = locale;
    }

    public void resetLocale() {
        setLocale(defaultLocale);
    }

}
