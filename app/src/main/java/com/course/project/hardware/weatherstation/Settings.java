package com.course.project.hardware.weatherstation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

class Settings {

    private Activity mActivity;
    private static ArrayList<Setting> settingList;

    Settings(Activity activity) {
        this.mActivity = activity;
        settingList = this.parse(
                mActivity.getResources().getXml(R.xml.settings)
        );

    }

    static Setting getSettingById(int settingId) {
        for (Settings.Setting setting : settingList) {
            if(setting.getId() == settingId) {
                return setting;
            }
        }
        return null;
    }

    static void updateSettingList(ArrayList<Setting> settingList,
                                                SharedPreferences preferences) {
        int id, defaultValue, value;
        for(Setting setting : settingList) {
            id = setting.getId();
            defaultValue = setting.getValue();
            value = preferences.getInt(String.valueOf(id), defaultValue);
            setting.setValue(value);
        }
    }

    ArrayList<Setting> getSettingList() {
        return this.settingList;
    }

    private ArrayList<Setting> parse(XmlPullParser parser) {
        ArrayList<Setting> settingList = null;
        Setting setting;
        Setting.Option option;
        boolean endTag;
        int eventType;

        try {
            eventType = parser.getEventType();
            setting = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        settingList = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("setting")) {
                            setting = new Setting();
                        }
                        else if (parser.getName().equals("id")) {
                            eventType = parser.next();
                            setting.setId(Integer.parseInt(parser.getText()));
                        }
                        else if (parser.getName().equals("name")) {
                            eventType = parser.next();
                            setting.setName(parser.getText());
                        }
                        else if (parser.getName().equals("default_value")) {
                            eventType = parser.next();
                            setting.setValue(Integer.parseInt(parser.getText()));
                        }
                        else if(parser.getName().equals("options")) {
                            endTag = false;
                            option = null;
                            while(!endTag) {
                                eventType = parser.next();
                                switch (eventType) {
                                    case XmlPullParser.START_TAG:
                                        if (parser.getName().equals("option")) {
                                            option = setting.newOption();
                                        }
                                        else if (parser.getName().equals("id")) {
                                            eventType = parser.next();
                                            option.setOptionId(Integer.parseInt(parser.getText()));
                                        }
                                        else if (parser.getName().equals("name")) {
                                            eventType = parser.next();
                                            option.setOptionName(parser.getText());
                                        }
                                        break;
                                    case XmlPullParser.END_TAG:
                                        if (parser.getName().equals("option")) {
                                            setting.getOptions().add(option);
                                            option = null;
                                        }
                                        else if(parser.getName().equals("options")) {
                                            endTag = true;
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("setting")) {
                            settingList.add(setting);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.d("XML", e.toString());
        } catch(IOException e) {
            Log.d("XML", e.toString());
        } catch(Exception e) {
            Log.d("XML", e.toString());
        }

        Log.d("XMLPARSE", settingList.toString());
        return settingList;
    }

    class Setting {

        private int id;
        private String name;
        private int value;
        private ArrayList<Setting.Option> options = new ArrayList<>();

        int getId() {
            return id;
        }

        String getName() {
            return name;
        }

        int getValue() {
            return value;
        }

        ArrayList<Setting.Option> getOptions() {
            return options;
        }

        void setId(int id){
            this.id = id;
        }

        void setName(String name) {
            this.name = name;
        }

        void setValue(int value) {
            this.value = value;
        }

        void setOptions(ArrayList<Setting.Option> option) {
            this.options = option;
        }

        Setting.Option newOption() {
            return new Setting.Option();
        }

        String getOptionNameByValue(int value) {
            for (Option opt: options) {
                if(opt.getOptionId()==value) return opt.getOptionName();
            }
            return "Not Found";
        }

        class Option {

            private int optionId;
            private String optionName;

            int getOptionId() {
                return optionId;
            }

            String getOptionName() {
                return optionName;
            }

            void setOptionId(int optionId) {
                this.optionId = optionId;
            }

            void setOptionName(String optionName) {
                this.optionName = optionName;
            }
        }

    }

}
