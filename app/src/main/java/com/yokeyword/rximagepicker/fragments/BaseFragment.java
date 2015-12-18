package com.yokeyword.rximagepicker.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;
import com.yokeyword.simple.SimpleApplication;

/**
 * Created by YoKeyword on 15/12/18.
 */
public class BaseFragment extends Fragment {
    protected Activity _activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this._activity = activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SimpleApplication.getInstance().getRefWatcher(_activity);
        refWatcher.watch(this);
    }
}
