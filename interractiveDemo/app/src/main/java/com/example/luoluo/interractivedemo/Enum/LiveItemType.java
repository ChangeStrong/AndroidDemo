package com.example.luoluo.interractivedemo.Enum;

public enum LiveItemType {
    LiveItemTypeTitle(0),//相当于是个这个枚举的实列
    LiveItemTypeFirst(1),
    LiveItemTypeTitleSecond(2);

    private int value;

    private LiveItemType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    }
