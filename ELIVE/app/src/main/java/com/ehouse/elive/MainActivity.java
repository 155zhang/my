package com.ehouse.elive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.eil.eilpublisher.interfaces.LiveEventInterface;
import com.eil.eilpublisher.interfaces.LiveInterface;
import com.eil.eilpublisher.liveConstants.LiveConstants;
import com.eil.eilpublisher.media.LivePushConfig;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button mBtnStartLive;

    private Button mBtnCame;

    private Button mBtnSwitchCam;

    static int mDefinitionMode = 0;//默认480分辨率

    String mRtmpUrl;

    static int mEncodeMode = 1;//默认硬编码

    int mPushState = 0; //默认非推流状态

    private LivePushConfig mLivePushConfig;

    Handler mHandler = null;

    Runnable mRunnable;

    public final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    private TextView tv_loading;

    private TextView tv_stop;

    private static final int PUSH_ERRO_CONN = 30;

    private static final int FINISH_STOP_PUSH = 40;

    public static boolean mGoToStting = false;

    public TextView tv_warning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        setContentView(R.layout.activity_main);
        if (mGoToStting) {
            Resources res = getResources();
            Drawable drawable = res.getDrawable(R.drawable.bkcolor);
            this.getWindow().setBackgroundDrawable(drawable);
            Log.i(TAG, "backgroud--" + drawable);
        }
        if (Build.VERSION.SDK_INT < 19) {
            showMessage("版本过低,最低支持4.4");
            return;
        }
        mRtmpUrl = PreUtils.getString(MainActivity.this, "url", "");
        intActivity();
        initView();
        initPush();
        setListener();
    }

    private void checkPermission() {
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("未获取到权限");
            return;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("未获取到权限");
            return;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("未获取到权限");
            return;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("未获取到权限");
            return;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_VIDEO_OUTPUT)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("未获取到权限");
            return;
        } else {
            initPush();
            setListener();
        }
    }

    private void initPush() {
        mLivePushConfig = new LivePushConfig();
        updatePushConfig();
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        LiveInterface.getInstance().init(mSurfaceView, mLivePushConfig);
        if (mGoToStting) {
            Resources res = getResources();
            Drawable drawable = res.getDrawable(R.color.transparent);
            this.getWindow().setBackgroundDrawable(drawable);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.i(TAG, "isCLick--" + String.valueOf(mBtnCame.isClickable()));
                Log.i(TAG, "mShouldClick" + mShouldClick);
                switch (msg.what) {
                    case 300:
                        mBtnCame.setClickable(true);
                        tv_warning.setVisibility(View.INVISIBLE);
                        mPushClickCount = 0;
                        mShouldClick = true;
                        mBtnCame.setClickable(true);
                        break;
                    case LiveConstants.PUSH_EVT_CONNECT_SUCC:
                        mBtnStartLive.setClickable(false);
                        showMessage("连接成功");
                        tv_loading.setVisibility(View.INVISIBLE);
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                        mPushState = 0;
                        mLastPushTime = System.currentTimeMillis();
                        break;
                    case LiveConstants.PUSH_EVT_PUSH_BEGIN:
                        showMessage("开始推流");
                            mBtnStartLive.setClickable(true);
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_start);
                        mPushState = 1;
                        break;
                    case LiveConstants.PUSH_ERR_NET_DISCONNECT:
                        showMessage("网络连接断开");
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                        mPushState = 0;
                        LiveInterface.getInstance().stop();
                        mBtnStartLive.setClickable(true);
                        break;

                    case FINISH_STOP_PUSH:
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                        showMessage("推流结束");
                        tv_stop.setVisibility(View.INVISIBLE);
                            mBtnStartLive.setClickable(true);
                        mPushState = 0;
                        Log.i(TAG, "stop finish");
                        break;

                    case PUSH_ERRO_CONN:
                        showMessage("连接失败");
                        tv_loading.setVisibility(View.INVISIBLE);
                            mBtnStartLive.setClickable(true);
                        break;

                    case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL:
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                            mBtnStartLive.setClickable(true);
                        showMessage("连接失败");
                        mPushState = 0;
                        break;
                    case LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL:
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                            mBtnStartLive.setClickable(true);
                        showMessage("音频失败");
                        mPushState = 0;
                        break;
                    case LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL:
                        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
                            mBtnStartLive.setClickable(true);

                        showMessage("视频失败");
                        mPushState = 0;
                        break;
                    default:
                }
            }
        };
        mRunnable = new Runnable() {
            public void run() {
                Log.i(TAG, "LiveEventInterface disconnect and reconnect init");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                int ret = LiveInterface.getInstance().start();
                if (ret < 0) {
                    Log.i(TAG, "reconnect start failed");
                    showMessage("");
                } else {

                }
            }
        };

    }

    private void showMessage(String s) {
        ToastUtils.showToast(MainActivity.this, s);
    }

    private void setListener() {
        mBtnCame.setOnClickListener(this);
        mBtnStartLive.setOnClickListener(this);
        mBtnSwitchCam.setOnClickListener(this);

    }

    private void initView() {
        mBtnStartLive = (Button) findViewById(R.id.btn_push);
        mBtnCame = (Button) findViewById(R.id.btn_menu);
        mBtnSwitchCam = (Button) findViewById(R.id.btn_swith);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        tv_loading.setVisibility(View.INVISIBLE);
        tv_stop = (TextView) findViewById(R.id.tv_stop);
        tv_stop.setVisibility(View.INVISIBLE);
        tv_warning = (TextView) findViewById(R.id.tv_warning);
        tv_warning.setVisibility(View.INVISIBLE);

    }

    long mLastCameraTime = 0;
    long mLastPushTime = 0;
    int mPushClickCount = 0;
    static boolean mShouldClick = true;

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_menu:
                if (mPushState == 0) {
                    startActivty();
                } else if (mPushState == 1) {
                    showMessage("设置参数前，结束当前推流");
                }
                mPushClickCount = 0;
                break;
            case R.id.btn_swith:
                //摄像头切换预留时间
                if (System.currentTimeMillis() - mLastCameraTime < 1000) {
                    return;
                }
                LiveInterface.getInstance().switchCamera();
                showMessage("切换摄像头方向");
                mLastCameraTime = System.currentTimeMillis();
                mPushClickCount = 0;
                break;
            case R.id.btn_push:
                Log.i(TAG, "current count---" + mPushClickCount);
                if (System.currentTimeMillis() - mLastPushTime < 800) {
                    mPushClickCount++;//防止频繁操作
                    Log.i(TAG, "when quick click" + mPushClickCount);
                    if (mPushClickCount > 8) {
                        //避免连续点击多次
                        mBtnCame.setClickable(false);
                        showMessage("操作频繁,稍后重试");
                        mShouldClick = false;
                        tv_warning.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message msg2 = Message.obtain();
                                msg2.what = 300;
                                mHandler.sendMessageDelayed(msg2, 4000);
                            }
                        }).start();
                        mBtnCame.setClickable(true);
                        return;
                    }
                    return;
                }
                if (checkUrl()) {
                    showMessage("推流地址不能为空");
                    return;
                }
                if (mPushState == 0) {  //未推流状态
                    if (!ConnectionUtils.isConn(this)) {
                        showMessage("请检查网络");
                        return;
                    }
                    showMessage("打开推流连接");
                    tv_loading.setVisibility(View.VISIBLE);
                    mBtnStartLive.setClickable(false);
                    Log.i(TAG, "go to  start push");

                    //开启子线程推流连接
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (LiveInterface.getInstance() == null) {
                                return;
                            }
                            if (LiveInterface.getInstance() == null) {
                                return;
                            }
                            int ret = LiveInterface.getInstance().start();
                            if (ret < 0) {
                                Log.i(TAG, "connect failed");
                                Message msg = Message.obtain();
                                msg.what = PUSH_ERRO_CONN;
                                mHandler.sendMessage(msg);
                            }
                        }
                    }).start();

                } else if (mPushState == 1) {  //已推流状态
                    mBtnStartLive.setClickable(false);
                    showMessage("关闭推流连接");
                    tv_stop.setVisibility(View.VISIBLE);
                    Log.i(TAG, "go  to  stop push");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LiveInterface.getInstance().stop();
                            Message msg = Message.obtain();
                            msg.what = FINISH_STOP_PUSH;
                            mHandler.sendMessage(msg);
                        }
                    }).start();
                }

                break;
        }
    }

    private boolean checkUrl() {
        if (!StringUtils.isEmpty(mRtmpUrl)) {
            return false;
        } else {
            return true;
        }
    }

    private void startActivty() {
        Intent myIntent;
        myIntent = new Intent(MainActivity.this, SettingActivty.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        mGoToStting = true;
        finish();
    }

    private LiveEventInterface mCaptureStateListener = new LiveEventInterface() {
        @Override
        public void onStateChanged(int eventId) {
            Message msg = new Message();

            switch (eventId) {
                case LiveConstants.PUSH_ERR_NET_DISCONNECT://断线
                    Log.i(TAG, "LiveEventInterface net break");
                    msg.what = LiveConstants.PUSH_ERR_NET_DISCONNECT;
                    mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL://
                    Log.i(TAG, "LiveEventInterface disconnect and reconnect");
                    msg.what = LiveConstants.PUSH_ERR_NET_CONNECT_FAIL;
                    mHandler.sendMessage(msg);
                    LiveInterface.getInstance().stop();
                    //重连
                    mHandler.post(mRunnable);

                    break;
                case LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL://音频采集编码线程启动失败
                    Log.i(TAG, "LiveEventInterface audio encoder failed");
                    msg.what = LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL;
                    mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL://视频采集编码线程启动失败
                    Log.i(TAG, "LiveEventInterface video encoder failed");
                    msg.what = LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL;
                    mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_EVT_CONNECT_SUCC: //连接成功
                    Log.i(TAG, "LiveEventInterface connect ok");
                    msg.what = LiveConstants.PUSH_EVT_CONNECT_SUCC;
                    mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_EVT_PUSH_BEGIN: //开始推流
                    Log.i(TAG, "LiveEventInterface begin push");
                    msg.what = LiveConstants.PUSH_EVT_PUSH_BEGIN;
                    mHandler.sendMessage(msg);
                    break;
            }
        }
    };

    private void updatePushConfig() {
        Log.i(TAG, "mDefinitionMode--" + mDefinitionMode);
        Log.i(TAG, "mEncodeMode--" + mEncodeMode);
        mLivePushConfig.setRtmpUrl(mRtmpUrl);
        mLivePushConfig.setEventInterface(mCaptureStateListener);
        mLivePushConfig.setAppContext(this);
        mLivePushConfig.setAudioChannels(AudioFormat.CHANNEL_IN_MONO);
        mLivePushConfig.setAudioSampleRate(44100);
        if (0 == mEncodeMode) {
            mLivePushConfig.setHWVideoEncode(false);
        } else {
            mLivePushConfig.setHWVideoEncode(true);
        }

        switch (mDefinitionMode) {
            case 0:
                mLivePushConfig.setVideoSize(640, 480);
                mLivePushConfig.setVideoFPS(20);
                mLivePushConfig.setVideoBitrate(800);
                break;
            case 1:
                mLivePushConfig.setVideoSize(1280, 720);
                mLivePushConfig.setVideoFPS(15);
                mLivePushConfig.setVideoBitrate(1200);
                break;
        }
    }

    public static void setDefinitionMode(int i) {
        // TODO Auto-generated method stub
        mDefinitionMode = i;

    }

    public static void setEncodeMode(int i) {
        // TODO Auto-generated method stub
        mEncodeMode = i;
    }

    private void intActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        LiveInterface.getInstance().resume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        LiveInterface.getInstance().uninit();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        LiveInterface.getInstance().pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //推流状态复位
        mBtnStartLive.setBackgroundResource(R.drawable.ic_video_stop);
        mPushState = 0;
        tv_loading.setVisibility(View.INVISIBLE);
        LiveInterface.getInstance().uninit();
    }
}
