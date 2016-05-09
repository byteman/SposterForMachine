package cn.cloudwalk.sposterformachine.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tencent.bugly.crashreport.CrashReport;

import cn.cloudwalk.sposterformachine.PhoneApp;
import cn.cloudwalk.sposterformachine.R;
import cn.cloudwalk.util.PreferencesUtils;

/**
 * 主板机主界面
 */
public class MainActivity extends AppCompatActivity {
    Button facereg_main;
    Button recface_main;
    ImageButton settings_main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.loadLibrary("CloudWalkFaceSdkForC");
        //腾讯bugly
        CrashReport.initCrashReport(getApplicationContext(), "900028053", false);
        initData();//初始化数据
    }

    /**
     * 初始化数据
     */
    private void initData() {
        facereg_main = (Button) findViewById(R.id.facereg_main);
        recface_main = (Button) findViewById(R.id.recface_main);
        settings_main = (ImageButton) findViewById(R.id.settings_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            PhoneApp.serverStr = PreferencesUtils.getString(this,
                    "preference_save_location", "120.25.253.47:7000");
            PhoneApp.scoreThreshold = Float.parseFloat(PreferencesUtils
                    .getString(this, "preference_score", "0.7"));
            PhoneApp.ocrServer = PreferencesUtils.getString(this,
                    "preference_ocr", "http://120.76.76.125:30006");
            PhoneApp.liveCount = Integer.parseInt(PreferencesUtils.getString(
                    this, "preference_livecount", "3"));
            PhoneApp.livingLevel = Integer.valueOf(PreferencesUtils.getInt(
                    this,"preference_livinglevel",2));
            PhoneApp.ATTENDANCEADDR = PreferencesUtils.getString(
                    this,"preference_attendanceaddr","192.168.10.62:8080");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 点击设置
     */
    public void click_sets(View view){
        if(view.getId()==R.id.settings_main){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        }else if (view.getId()==R.id.facereg_main){
            startActivity(new Intent(MainActivity.this,SendNumberActivity.class));
        }else if(view.getId()==R.id.recface_main){
            startActivity(new Intent(MainActivity.this,null));
        }
    }
}
