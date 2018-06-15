package com.example.luoluo.cartoon.UI.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.luoluo.cartoon.MainActivity;
import com.example.luoluo.cartoon.R;

public class PhoneLoginActivity extends Activity {
    private Button loginbutton ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
        loginbutton = findViewById(R.id.loginButton);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
