package com.example.luoluo.cartoon.UI.Login.model;

import android.view.View;

/**
 * Created by Seal on 2017/4/5.
 */

public class GuideData {
    private int bgRes;
    private boolean showEnter;
    private View mView;

    public View getmView() {
        return mView;
    }

    public void setmView(View mView) {
        this.mView = mView;
    }

    public GuideData(int bgRes, boolean showEnter) {
        this.bgRes = bgRes;
        this.showEnter = showEnter;
    }

    public int getBgRes() {
        return bgRes;
    }

    public void setBgRes(int bgRes) {
        this.bgRes = bgRes;
    }

    public boolean isShowEnter() {
        return showEnter;
    }

    public void setShowEnter(boolean showEnter) {
        this.showEnter = showEnter;
    }
}
