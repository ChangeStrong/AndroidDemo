package com.example.luoluo.cartoon;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.luoluo.cartoon.UI.BookselfFragment;
import com.example.luoluo.cartoon.UI.HomeFragment;
import com.example.luoluo.cartoon.UI.MeFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //加载fragment
    private HomeFragment homeFragment;//主页
    private BookselfFragment bookselfFragment;//书架
    private MeFragment meFragment;//我的

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        if (savedInstanceState != null){

            this.createTabBarUI();
        }

        setTabSelection(0);
    }

    /**
     * 底部的三个个按钮
     */
    private LinearLayout mTabHomeLinear;
    private LinearLayout mTabBookselfLinear;
    private LinearLayout mTabMeLinear;
    private void initViews() {
        mTabHomeLinear =  (LinearLayout) findViewById(R.id.tab_home_linear);
        mTabBookselfLinear =(LinearLayout) findViewById(R.id.tab_bookself_linear);
        mTabMeLinear = (LinearLayout) findViewById(R.id.tab_me_linear);

        mTabHomeLinear.setOnClickListener(this);
        mTabBookselfLinear.setOnClickListener(this);
        mTabMeLinear.setOnClickListener(this);

    }
    public void createTabBarUI(){

        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
        bookselfFragment =  (BookselfFragment) getSupportFragmentManager().findFragmentByTag("bookself");
        meFragment = (MeFragment ) getSupportFragmentManager().findFragmentByTag("me");

        if (homeFragment == null){
            Log.d(TAG, "createTabBarUI: 222222");
        }

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_home_linear:
                setTabSelection(0);
                break;
            case R.id.tab_bookself_linear:
                setTabSelection(1);
                break;
            case R.id.tab_me_linear:
                setTabSelection(2);
                break;
            default:
                break;
        }
    }

    //碎片转换器
    private FragmentTransaction mTransaction;
    private int lastSelectedIndex = -1;
    private boolean isCurrentCallFrag;
    public void setTabSelection(int index) {
        isCurrentCallFrag = false;//重置
        // 清除所有图标状态显示
        resetBtn();
        // 开启一个Fragment事务
        mTransaction = getSupportFragmentManager().beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(mTransaction);

        if (index != 2) {
            lastSelectedIndex = index;
        }
        switch (index) {
            case 0:
                //主页
                ((ImageButton) mTabHomeLinear.findViewById(R.id.tab_home_button)).setImageResource(R.drawable.tabbar_home_press);
                ((TextView) mTabHomeLinear.findViewById(R.id.tab_home_textView)).setTextColor(getResources().getColor(R.color.tabbar_textColor_press));

                if (homeFragment == null) {
                    //初始化另一个Fragment
                    homeFragment = new HomeFragment();
                    //将此fragment添加到显示的位置 并添加一个标志
                    mTransaction.add(R.id.main_home_frameLayout, homeFragment, "home");
                } else {
                    mTransaction.show(homeFragment);
                }
                break;
            case 1:
                //联系人
                ((ImageButton) mTabBookselfLinear.findViewById(R.id.tab_bookself_button)).setImageResource(R.drawable.tabbar_bookself_press);
                ((TextView) mTabBookselfLinear.findViewById(R.id.tab_bookself_textView)).setTextColor(getResources().getColor(R.color.tabbar_textColor_press));
//                if (contactMenu != null) {
//                    ((TextView) mTabBtnContacts.findViewById(R.id.tv_tab_bottom_contacts)).setText(contactMenu.getMenuname());
//                }
                if (bookselfFragment == null) {
                    bookselfFragment = new BookselfFragment();
//                    listener = (IMainClickListener) mContactsFragment1;
                    mTransaction.add(R.id.main_home_frameLayout, bookselfFragment, "bookself");
                } else {
                    mTransaction.show(bookselfFragment);
                }
                break;

            case 2:
                //我的
                ((ImageButton) mTabMeLinear.findViewById(R.id.tab_me_button)).setImageResource(R.drawable.tabbar_me_press);
                ((TextView) mTabMeLinear.findViewById(R.id.tab_me_textView)).setTextColor(getResources().getColor(R.color.tabbar_textColor_press));
//                if (meMenu != null) {
//                    ((TextView) mTabBtnMe.findViewById(R.id.tv_tab_bottom_me)).setText(meMenu.getMenuname());
//                }
                if (meFragment == null) {
                    meFragment = new MeFragment();
                    mTransaction.add(R.id.main_home_frameLayout, meFragment, "me");
                } else {
                    mTransaction.show(meFragment);
                }
                break;

        }
        mTransaction.commitAllowingStateLoss();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void resetBtn() {
        ((ImageButton) mTabHomeLinear.findViewById(R.id.tab_home_button)).setImageResource(R.drawable.tabbar_home);
        ((ImageButton) mTabBookselfLinear.findViewById(R.id.tab_bookself_button)).setImageResource(R.drawable.tabbar_bookshelf);
        ((ImageButton) mTabMeLinear.findViewById(R.id.tab_me_button)).setImageResource(R.drawable.tabbar_me);

        ((TextView) mTabHomeLinear.findViewById(R.id.tab_home_textView)).setTextColor(getResources().getColor(R.color.color_default_textColor));
        ((TextView) mTabBookselfLinear.findViewById(R.id.tab_bookself_textView)).setTextColor(getResources().getColor(R.color.color_default_textColor));
        ((TextView) mTabMeLinear.findViewById(R.id.tab_me_textView)).setTextColor(getResources().getColor(R.color.color_default_textColor));
    }

    private void hideFragments(FragmentTransaction transaction) {

        if (bookselfFragment != null) {
            transaction.hide(bookselfFragment);
        }
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (meFragment != null) {
            transaction.hide(meFragment);
        }
    }
}
