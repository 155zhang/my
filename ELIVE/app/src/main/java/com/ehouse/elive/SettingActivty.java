package com.ehouse.elive;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import info.hoang8f.android.segmented.SegmentedGroup;

public class SettingActivty extends Activity implements View.OnClickListener {
    public final static String TAG = "MainActivity";
    private Button mBtnSetOK;
    EditText mUrlText;
    public RadioButton mRadio480,mRadio720;
    public RadioButton mRadioEncHW, mRadioEncSW;
    private String mRtmpUrl;
    private SegmentedGroup groupencoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        setContentView(R.layout.activity_setting);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        intviews();

    }

    private void intviews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBtnSetOK = (Button) findViewById(R.id.btn_ok);
        mBtnSetOK.setOnClickListener(this);

        mUrlText =(EditText)findViewById(R.id.editText1);
        mRtmpUrl = PreUtils.getString(SettingActivty.this,"url",null);
        mUrlText.setText(mRtmpUrl);
        MainActivity.setEncodeMode(1);
        groupencoder = (SegmentedGroup)this.findViewById(R.id.radioGroup);
        mRadioEncHW =  (RadioButton) findViewById(R.id.radioHW);
        mRadioEncSW =  (RadioButton) findViewById(R.id.radioSW);
        groupencoder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if(arg1 == mRadioEncSW.getId())
                {
                    MainActivity.setEncodeMode(0);
                }
                if(arg1 == mRadioEncHW.getId())
                {
                    MainActivity.setEncodeMode(1);
                }
            }
        });
        mRadio480 = (RadioButton) findViewById(R.id.radio480);
        mRadio720 = (RadioButton) findViewById(R.id.radio720);
        MainActivity.setDefinitionMode(0);
        RadioGroup groupdefinition = (RadioGroup)this.findViewById(R.id.radioGroup2);
        groupdefinition.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if(arg1 == mRadio480.getId())
                {
                    MainActivity.setDefinitionMode(0);
                }
                if(arg1 == mRadio720.getId())
                {
                    MainActivity.setDefinitionMode(1);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        String strUrl = mUrlText.getText().toString().trim();
        if(StringUtils.isEmpty( strUrl))
        {
            ToastUtils.showToast(SettingActivty.this,"推流地址不能为空!");
            return;
        }
        mRtmpUrl = strUrl;
        PreUtils.putString(SettingActivty.this,"url",mRtmpUrl);
        Intent intent = new Intent();
        MainActivity.mGoToStting = true;
        intent.setClass(SettingActivty.this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
        finish();
    }
    @Override
    protected void onStop() {
        super.onStop();
        PreUtils.putString(SettingActivty.this,"url",mRtmpUrl);
    }
}
