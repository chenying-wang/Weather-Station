package com.course.project.hardware.weatherstation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

public class SettingsDialogFragment extends DialogFragment {

    Fragment mFragment;
    Settings.Setting mSetting;
    String[] itemNames;

    public SettingsDialogFragment setSetting(
            Fragment fragment,
            Settings.Setting setting) {
        this.mFragment = fragment;
        this.mSetting = setting;
        itemNames = new String[mSetting.getOptions().size()];
        int i=0;
        for (Settings.Setting.Option opt : mSetting.getOptions()) {
            itemNames[i] = opt.getOptionName();
            i++;
        }

        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mSetting.getName())
                .setItems(itemNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mSetting.getValue()==mSetting.getOptions().get(which).getOptionId()) {
                            return;
                        }

                        ((SettingsFragment)mFragment).getAdapter()
                                .processChangedSetting(mSetting.getId(),
                                        mSetting.getOptions().get(which).getOptionId());
                    }
                });
        return builder.create();
    }
}
