package com.test.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.test.myapp.utils.ErrorMsgUtil;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKExtendParams;
import us.zoom.sdk.ZoomVideoSDKInitParams;
import us.zoom.sdk.ZoomVideoSDKRawDataMemoryMode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSDK();
    }

    public void gotoJoin(View view) {
        Intent intent = new Intent(this, JoinSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    protected void initSDK() {
        ZoomVideoSDKInitParams params = new ZoomVideoSDKInitParams();
        params.domain = "zoom.us";
        params.enableLog = true;
        params.videoRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.audioRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.shareRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;

        params.extendParam = new ZoomVideoSDKExtendParams();
        params.extendParam.speakerTestFilePath = "/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/test.mp3";

        int ret = ZoomVideoSDK.getInstance().initialize(this.getApplicationContext(), params);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(ret), Toast.LENGTH_LONG).show();
        } else {
            Log.d(MainActivity.class.getSimpleName(), "Success");
        }
    }

}