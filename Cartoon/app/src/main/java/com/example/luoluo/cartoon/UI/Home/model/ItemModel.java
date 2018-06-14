package com.example.luoluo.cartoon.UI.Home.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemModel extends Object implements Parcelable {
    //***正在召开的会议字段
    public String title;
    public int imageId;
    public String[] titles;
    //*end

    public ItemModel(Parcel in) {

    }
    public static final Parcelable.Creator<ItemModel> CREATOR = new Parcelable.Creator<ItemModel>() {
        @Override
        public ItemModel createFromParcel(Parcel in) {
            return new ItemModel(in);
        }

        @Override
        public ItemModel[] newArray(int size) {
            return new ItemModel[size];
        }
    };

    public ItemModel() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

//        dest.writeString(this.classChannel);

    }
}
