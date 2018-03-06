package com.course.project.hardware.weatherstation;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

abstract class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    MainActivity mActivity;
    Fragment mFragment;
    RecyclerView mRecyclerView;

    RecyclerViewAdapter(MainActivity activity, Fragment fragment) {
        this.mActivity = activity;
        this.mFragment = fragment;
    }

    RecyclerViewAdapter(MainActivity activity) {
        this.mActivity = activity;
    }

    void setRecyclerView(RecyclerView view) {
        this.mRecyclerView = view;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        final View mView;

        ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    class OnClickListener implements View.OnClickListener{

        int position;

        OnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Log.d("RECYCLERVIEW", "Item No."+this.position+ " Clicked");
        }

    }

}
