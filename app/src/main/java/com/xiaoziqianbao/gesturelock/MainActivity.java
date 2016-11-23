package com.xiaoziqianbao.gesturelock;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.xiaoziqianbao.gesturelock.view.GestureLockView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GestureLockView gestureLockView = (GestureLockView) findViewById(R.id.gesturelock);
        gestureLockView.setOnGestureStateListener(new GestureLockView.GestureStateListener() {
            @Override
            public void setCountLess4() {
                Toast.makeText(MainActivity.this, "密码至少为4位", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fisrtSettingSuccess() {
                Toast.makeText(MainActivity.this, "第一次输入密码成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void secondSettingSuccess(String secondPWD) {
                Toast.makeText(MainActivity.this, "第二次输入密码成功"+secondPWD, Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("pwd",secondPWD).commit();

            }

            @Override
            public void secondSettingFailed() {
                Toast.makeText(MainActivity.this, "第二次输入密码与第一次不相同", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void inputGestureWrong() {
                Toast.makeText(MainActivity.this, "输入密码错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void inputGestureCorrect() {
                Toast.makeText(MainActivity.this, "输入密码正确", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
