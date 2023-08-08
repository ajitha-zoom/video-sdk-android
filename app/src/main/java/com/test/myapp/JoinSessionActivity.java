package com.test.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.test.myapp.utils.JWTGenerator;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAudioOption;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKSessionContext;
import us.zoom.sdk.ZoomVideoSDKVideoOption;

public class JoinSessionActivity extends AppCompatActivity implements View.OnClickListener {
    protected TextView leftView;
    protected TextView titleTextView;
    protected Button btnJoin;
    protected EditText sessionEditText;
    protected String sessionName;
    protected TextView nameEdit;
    protected String userName;
    protected EditText passwordEdit;
    protected String password;
    private boolean hasInJoinorCreate = false;
    protected final static int REQUEST_AUDIO_TEST_CODE = 1011;
    protected final static int REQUEST_VIDEO_AUDIO_CODE = 1010;

    ZoomVideoSDKSessionContext sessionContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        /* ------------ INIT VIEWS ------------ */

        leftView = findViewById(R.id.tvBack);
        titleTextView = findViewById(R.id.title);
        if (null != leftView) {
            leftView.setOnClickListener(this);
        }
        sessionEditText = findViewById(R.id.session_edit);
        sessionEditText.setSelection(0, sessionEditText.getText().length());
        nameEdit = findViewById(R.id.userName_edit);
        if (null != nameEdit) {
            nameEdit.setText(Build.MODEL + "-" + Build.VERSION.SDK_INT);
        }
        passwordEdit = findViewById(R.id.password_edit);
        btnJoin = findViewById(R.id.btn_join);
        btnJoin.setText(R.string.join);
        if (null != titleTextView && (R.string.join_title) > 0) {
            titleTextView.setText(R.string.join_title);
        }
        sessionContext = new ZoomVideoSDKSessionContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionEditText.setSelection(0, sessionEditText.getText().length());
        sessionEditText.requestFocus();
        hasInJoinorCreate = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == leftView) {
            onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickJoinSession(View view) {
        joinOrCreateSession();
    }

    protected boolean requestPermission(int code) {

        String[] permissions = new String[]{android.Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.POST_NOTIFICATIONS};
        } else if (Build.VERSION.SDK_INT >= 31) {
            permissions = new String[]{android.Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT};
        }
        if (code == REQUEST_AUDIO_TEST_CODE) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
            if (Build.VERSION.SDK_INT >= 31) {
                permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT};
            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, code);
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_AUDIO_CODE) {
            if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    && (Build.VERSION.SDK_INT >= 31 && checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED))) {
                onPermissionGranted();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onPermissionGranted() {
        joinOrCreateSession();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void joinOrCreateSession() {
        if (hasInJoinorCreate)
            return;

        /* ------------ CHECK PERMISSIONS ------------ */
        if (!requestPermission(REQUEST_VIDEO_AUDIO_CODE))
            return;

        if (null == ZoomVideoSDK.getInstance()) {
            Toast.makeText(this, "Please initialize SDK", Toast.LENGTH_LONG).show();
            return;
        }

        /* ------------ SET AUDIO OPTIONS ------------ */
        ZoomVideoSDKAudioOption audioOption = new ZoomVideoSDKAudioOption();
        audioOption.connect = true;
        audioOption.mute = false;
        audioOption.isMyVoiceInMix = true;
        sessionContext.audioOption = audioOption;

        /* ------------ SET VIDEO OPTIONS ------------ */
        ZoomVideoSDKVideoOption videoOption = new ZoomVideoSDKVideoOption();
        videoOption.localVideoOn = true;
        sessionContext.videoOption = videoOption;

        /* ------------ VALIDATE INPUTS ------------ */
        userName = nameEdit.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            userName = Build.MODEL;
        }

        sessionName = sessionEditText.getText().toString().toLowerCase().trim();

        if (TextUtils.isEmpty(sessionName)) {
            Toast.makeText(this, "Session name is empty", Toast.LENGTH_LONG).show();
            return;
        }
        //Required
        sessionContext.sessionName = sessionName;
        sessionContext.userName = userName;

        password = passwordEdit.getText().toString();
        //Optional
        sessionContext.sessionPassword = password;

        /* ------------ GENERATE JWT ------------ */
        JWTGenerator jwtGenerator = new JWTGenerator(this);
        jwtGenerator.execute();
    }

    public void onPostExecution(String token) {

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Token is empty", Toast.LENGTH_LONG).show();
            return;
        }

        sessionContext.token = token;

        /* ------------ JOIN SESSION ------------ */
        ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().joinSession(sessionContext);
        if (null == session) {
            return;
        }
        hasInJoinorCreate = true;

        /* ------------ NAVIGATE TO MEETING SCREEN ------------ */
        Intent intent = new Intent(this, MeetingActivity.class);
        intent.putExtra("name", userName);
        intent.putExtra("password", password);
        intent.putExtra("sessionName", sessionName);
        startActivity(intent);
    }
}