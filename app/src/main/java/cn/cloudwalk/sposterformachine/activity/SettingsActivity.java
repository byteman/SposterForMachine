package cn.cloudwalk.sposterformachine.activity;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;

import cn.cloudwalk.sposterformachine.PhoneApp;
import cn.cloudwalk.sposterformachine.R;
import cn.cloudwalk.util.PreferencesUtils;


public class SettingsActivity extends ActionBarActivity implements View.OnClickListener{
    EditText set_serveraddr;
    EditText set_threshold;
    Spinner set_class;
    Button set_save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initData();
        DecimalFormat df = new DecimalFormat("#.#");
        set_threshold.setText(String.valueOf(df.format(PhoneApp.scoreThreshold)));
        set_serveraddr.setText(PhoneApp.ATTENDANCEADDR);
        if(PhoneApp.livingLevel==1){
            set_class.setSelection(0);
        }else if(PhoneApp.livingLevel==2){
            set_class.setSelection(1);
        }else if(PhoneApp.livingLevel==3){
            set_class.setSelection(2);
        }
        set_save.setOnClickListener(this);
    }

    private void initData() {
        set_serveraddr = (EditText) findViewById(R.id.set_serveraddr);
        set_threshold  = (EditText) findViewById(R.id.set_threshold);
        set_class      = (Spinner) findViewById(R.id.set_class);
        set_save       = (Button) findViewById(R.id.set_save);
    }

    @Override
    public void onClick(View v) {
        String str = set_serveraddr.getText().toString();
        PreferencesUtils.putString(this,"preference_attendanceaddr",str);
        PhoneApp.ATTENDANCEADDR = str;
        String threshold = set_threshold.getText().toString();
        PreferencesUtils.putFloat(this,"preference_score", Float.valueOf(threshold));
        PhoneApp.scoreThreshold = Float.valueOf(threshold);
        int setClass = set_class.getSelectedItemPosition();
        PreferencesUtils.putInt(this,"preference_livinglevel",setClass);
        PhoneApp.livingLevel = setClass;
        Toast.makeText(this,"保存成功",Toast.LENGTH_LONG).show();
        finish();
    }
}
