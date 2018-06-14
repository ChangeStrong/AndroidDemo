package com.example.luoluo.animationdemo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.example.luoluo.animationdemo.Utils.CommonUtil;
import com.example.luoluo.animationdemo.Utils.PointEvaluator;
import com.example.luoluo.animationdemo.Views.LLCustomView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LLCustomView mCustomView;

public String TAG = "MainActivity";
    int mscreenWidth;
    int mscreenHeight;
    Point mcenterPoint;
    List<Point>mPoints;
ImageView mMenuImageButton;
ImageView mMenuRightIB;
ImageView mMenuLeftIB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCustomView = findViewById(R.id.iv_keyAnimations);
        mMenuImageButton = findViewById(R.id.ib_menu);
        mMenuRightIB = findViewById(R.id.ib_menu_right);
        mMenuLeftIB = findViewById(R.id.ib_menu_left);
mPoints = new ArrayList<>();
        mscreenWidth = CommonUtil.getScreenWidth(this);
        mscreenHeight =  CommonUtil.getScreenHeight(this);
         Log.d(TAG, "onCreate: screenWidth"+mscreenWidth + " screenHeight:"+mscreenHeight);

        /*
        ImageView imageView = findViewById(R.id.iv_keyAnimations);
        //帧动画
        imageView.setImageResource(R.drawable.frame_key_animat);//设置动态图片集
        AnimationDrawable animationDrawable1 = (AnimationDrawable) imageView.getDrawable();
        animationDrawable1.start();
        //属性动画---通过xml设置属性.
//        imageView.setImageResource(R.drawable.a_0);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.alpha_anim);
//        animation.setDuration(10);
        imageView.startAnimation(animation);*/
    }


    @Override
    protected void onStart() {
        super.onStart();

       Size size = CommonUtil.getWidgetSize(mCustomView);

       mCustomView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //开始动画
               startAnimation();
           }
       });

       mMenuImageButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG, "onClick: click menu item.");
           }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //监听测量方法走完后获取尺寸
        ViewTreeObserver vto2 = mMenuImageButton.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMenuImageButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int[] startLoc = new int[2];
                mMenuImageButton.getLocationInWindow(startLoc);//此方法获取的高加上导航条了
                Log.d(TAG, "onResume: "+ mMenuImageButton.getMeasuredWidth()+" "+mMenuImageButton.getMeasuredHeight());
                Log.d(TAG, "onResume: "+startLoc[0]+" "+startLoc[1]);
                System.out.println("图片各个角Left："+mMenuImageButton.getLeft()
                        +" Right："+mMenuImageButton.getRight()+" Top："
                        +mMenuImageButton.getTop()+" Bottom："
                        +mMenuImageButton.getBottom());
                setPath();
            }
        });
    }


    //****动画
    public void setPath(){
        int startX = mscreenWidth/2;
        int startY = mscreenHeight-mMenuImageButton.getMeasuredHeight()/2;
        Point startPoint = new Point(startX,startY);
        int radius = CommonUtil.dpTopx(this,200);
        Point endPoint = new Point(startX,startY-radius);
       mPoints.add(startPoint);
       mPoints.add(endPoint);
       mcenterPoint = startPoint;

    }
    //动画每次计算后会掉此方法执行
    public void setMenuImageButtonPosition(Point point){
        Log.d(TAG, "setMenuImageButtonPosition: x="+point.x +" y="+point.y);
        int moveX = point.x-mcenterPoint.x;
        int moveY = point.y - mcenterPoint.y;
        mMenuImageButton.setTranslationX(moveX);
        mMenuImageButton.setTranslationY(moveY);

    }

    //开始动画
    public void startAnimation(){
//        ObjectAnimator anim = ObjectAnimator.ofObject(this, "MenuImageButtonPosition", new PointEvaluator()
//                ,mPoints.toArray());
//        anim.setInterpolator(new DecelerateInterpolator());
//        anim.setDuration(500);//1秒=1000
//        anim.start();

        //开启集合动画
        this.startAnimationSet();
    }

    public  void startAnimationSet(){

        AnimatorSet animationSet = new AnimatorSet();
        //中心按钮
        ObjectAnimator animScale =ObjectAnimator.ofFloat(mMenuImageButton, "ScaleX", 0.0f, 0.5f, 1.0f);
        ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(mMenuImageButton,
                "rotation", 0, 90, 180, 270, 0);
        int radius = CommonUtil.dpTopx(this,200);
        ObjectAnimator animatorTransitionY = ObjectAnimator.ofFloat(mMenuImageButton,
                "TranslationY", -radius);

        //右按钮
        int movex1 = (int) (radius*Math.cos(CommonUtil.angleToRadians(60)));
        int movey1 = (int) (-radius*Math.sin(CommonUtil.angleToRadians(60)));

        ObjectAnimator animScale1 =ObjectAnimator.ofFloat(mMenuRightIB, "ScaleX", 0.0f, 0.5f, 1.0f);
        ObjectAnimator animatorRotation1 = ObjectAnimator.ofFloat(mMenuRightIB,
                "rotation", 0, 90, 180, 270, 0);
        ObjectAnimator animatorTransitionY1 = ObjectAnimator.ofFloat(mMenuRightIB,
                "TranslationY",movey1);
        ObjectAnimator animatorTransitionX1 = ObjectAnimator.ofFloat(mMenuRightIB,
                "TranslationX",movex1);

        //左按钮
        int movex2 = (int) (radius*Math.cos(CommonUtil.angleToRadians(120)));
        int movey2 = (int) (-radius*Math.sin(CommonUtil.angleToRadians(120)));
        ObjectAnimator animScale2 =ObjectAnimator.ofFloat(mMenuLeftIB, "ScaleX", 0.0f, 0.5f, 1.0f);
        ObjectAnimator animatorRotation2 = ObjectAnimator.ofFloat(mMenuLeftIB,
                "rotation", 0, 90, 180, 270, 0);
        ObjectAnimator animatorTransitionY2 = ObjectAnimator.ofFloat(mMenuLeftIB,
                "TranslationY",movey2);
        ObjectAnimator animatorTransitionX2 = ObjectAnimator.ofFloat(mMenuLeftIB,
                "TranslationX",movex2);


        animationSet.playTogether(animScale,animatorRotation,animatorTransitionY
                ,animScale1,animatorRotation1,animatorTransitionY1,animatorTransitionX1
                ,animScale2,animatorRotation2,animatorTransitionY2,animatorTransitionX2);
        animationSet.setDuration(1000);
        animationSet.start();
    }



}
