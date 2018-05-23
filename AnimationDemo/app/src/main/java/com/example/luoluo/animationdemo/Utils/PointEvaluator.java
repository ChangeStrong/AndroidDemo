package com.example.luoluo.animationdemo.Utils;

import android.animation.TypeEvaluator;
import android.graphics.Point;

public class PointEvaluator implements TypeEvaluator <Point> {
    @Override
    public Point evaluate(float fraction, Point startValue, Point endValue) {
        int x,y;
        x = (int) (startValue.x +fraction*(endValue.x-startValue.x));
        y = (int) (startValue.y+fraction*(endValue.y-startValue.y));
        return new Point(x,y);
    }
}
