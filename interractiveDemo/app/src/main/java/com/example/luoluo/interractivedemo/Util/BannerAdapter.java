package com.example.luoluo.interractivedemo.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class BannerAdapter extends FragmentPagerAdapter {

    private List<?> mFragment;

    public BannerAdapter(FragmentManager fm, List<?> mFragment) {
        super(fm);
        this.mFragment = mFragment;
    }


    @Override
    public Fragment getItem(int position) {

        return (Fragment) mFragment.get(position);
    }

    @Override
    public int getCount() {
        return mFragment.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
