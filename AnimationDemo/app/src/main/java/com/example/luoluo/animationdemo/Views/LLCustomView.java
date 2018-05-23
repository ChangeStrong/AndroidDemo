package com.example.luoluo.animationdemo.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.luoluo.animationdemo.R;

public class LLCustomView extends View {
    private String TAG = "LLCustomView";
    private Paint paint;
    public LLCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //获取自定义属性的值
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.LLCustomView);
        //第二个参数为，如果没有设置这个属性，则设置的默认的值
        int defalutMinSize = attributes.getDimensionPixelSize(R.styleable.LLCustomView_default_min_size,200);
        Log.d(TAG, "LLCustomView: Setting default min size is "+defalutMinSize);
        attributes.recycle();//回收对象

        this.initPaint();
    }

    //初始化画笔
    private  void initPaint(){
        paint = new Paint();
        //抗锯齿
        paint.setAntiAlias(true);
        //防抖动
        paint.setDither(true);
        //设置画笔未实心(是否填充)
        paint.setStyle(Paint.Style.STROKE);
        //设置颜色
        paint.setColor(Color.GREEN);
        //设置画笔宽度
        paint.setStrokeWidth(5);
    }
    //获取路径
    private Path getPath(){
        Path path = new Path();

        path.moveTo( 0,0);
        path.lineTo(120,120);
        //绘制圆滑曲线
//        path.quadTo(660, 260, 860, 460); //订单
        //二次方贝塞尔曲线
//        path.cubicTo(160,660,960,1060,260,1260);
        return path;
    }

    private  Path getCirclePath(){
        Path path = new Path();
        int radius = this.width()/2;
        Point centerPoint = new Point(this.width()/2,this.height()/2);
        path.addCircle(centerPoint.x,centerPoint.y,radius, Path.Direction.CW);
        return path;
    }

//根据拿到的xml的描述 得到此view实际的尺寸  走了此方法后在其它地方也可以拿到尺寸
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取当前设置宽的模式
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//当前设置的模式
        //实际的宽
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//根据模式计算出来的高
        int width = getActulaySizeBysetSizeAndMode(200, widthMeasureSpec);//此处的宽高为像素px 大小525px 200dp
        int height = getActulaySizeBysetSizeAndMode(200, heightMeasureSpec);
        //设置当前view的宽高
        setMeasuredDimension(width, height);
        Log.d(TAG, "onMeasure: "+this.width()+this.height());

    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画布上开始绘制
        canvas.drawPath(this.getCirclePath(),paint);
    }



    public int x(){
        return getLeft();
    }
    public  int y(){
        return getTop();
    }

    public int width(){
        //获取测量之后的宽
        return getMeasuredWidth();
    }
    public  int height(){
        return getMeasuredHeight();
    }



    //获取当前模式下的尺寸 如果为任意尺寸模式则 返回默认的宽高
    private  int getActulaySizeBysetSizeAndMode(int defaultSize,int measureSpec){
        int mySize = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            // UNSPECIFIED(任意尺寸、对应有adapter的view)
            case MeasureSpec.UNSPECIFIED: {
                //如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            //AT_MOST(父view能给你的最大尺寸、对应wrap_content)
            case MeasureSpec.AT_MOST: {
                //如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            //EXACTLY(当前尺寸是确定的、对应match_parent、固定100dp)
            case MeasureSpec.EXACTLY: {
                //如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

}
