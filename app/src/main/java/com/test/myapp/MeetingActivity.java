package com.test.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.myapp.notification.NotificationMgr;
import com.test.myapp.notification.NotificationService;
import com.test.myapp.screenshare.ShareToolbar;
import com.test.myapp.utils.ErrorMsgUtil;
import com.test.myapp.utils.UserHelper;
import com.test.myapp.utils.ZMAdapterOsBugHelper;
import com.test.myapp.views.UserVideoAdapter;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKAudioStatus;
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus;
import us.zoom.sdk.ZoomVideoSDKCameraDevice;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType;
import us.zoom.sdk.ZoomVideoSDKChatPrivilegeType;
import us.zoom.sdk.ZoomVideoSDKDelegate;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper;
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus;
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper;
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus;
import us.zoom.sdk.ZoomVideoSDKNetworkStatus;
import us.zoom.sdk.ZoomVideoSDKPasswordHandler;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason;
import us.zoom.sdk.ZoomVideoSDKVideoView;

public class MeetingActivity extends AppCompatActivity implements ZoomVideoSDKDelegate, ShareToolbar.Listener, UserVideoAdapter.ItemTapListener {

    private static final String TAG = MeetingActivity.class.getSimpleName();
    ZoomVideoSDKVideoView zoomCanvas;


    public final static int REQUEST_SHARE_SCREEN_PERMISSION = 1001;

    public final static int REQUEST_SYSTEM_ALERT_WINDOW = 1002;

    private FrameLayout videoContain;


    private Display display;

    private DisplayMetrics displayMetrics;

    private RecyclerView userVideoList;

    private LinearLayout videoListContain;

    private UserVideoAdapter adapter;


    private ShareToolbar shareToolbar;

    private ImageView iconShare;

    private ImageView iconVideo;

    private ImageView iconAudio;

    private ImageView iconMore;

    private TextView practiceText;

    private TextView sessionNameText;

    private View actionBar;

    private ScrollView actionBarScroll;

    private View btnViewShare;

    private String myDisplayName = "";
    private String meetingPwd = "";
    private String sessionName;

    private ImageView videoOffView;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean isActivityPaused = false;

    private ZoomVideoSDKUser mActiveUser;

    private ZoomVideoSDKUser currentShareUser;

    private ZoomVideoSDKSession session;

    private Intent mScreenInfoData;

    /* ------------ LIFECYCLE METHODS ------------ */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_meeting);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        display = ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        session = ZoomVideoSDK.getInstance().getSession();
        ZoomVideoSDK.getInstance().addListener(this);

        parseIntent();
        initView();
        initMeeting();
        updateSessionInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        unSubscribe();
        adapter.clear(false);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isActivityPaused) {
            isActivityPaused = false;
            resumeSubscribe();
        }
        updateActionBarLayoutParams();
        refreshRotation();

        if (ZoomVideoSDK.getInstance().isInSession()) {
            int size = UserHelper.getAllUsers().size();
            if (size > 0 && adapter.getItemCount() == 0) {
                adapter.addAll();
                updateVideoListLayout();
                refreshUserListAdapter();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
        ZoomVideoSDK.getInstance().removeListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        stopMeetingService();
    }

    /* ------------ ANDROID METHODS ------------ */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SHARE_SCREEN_PERMISSION:
                if (resultCode != RESULT_OK) {
                    break;
                }
                startShareScreen(data);
                break;
            case REQUEST_SYSTEM_ALERT_WINDOW:
                if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ZMAdapterOsBugHelper.getInstance().stopListenOverlayPermissionChange(this);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if ((!Settings.canDrawOverlays(this)) && (!ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged() || !ZMAdapterOsBugHelper.getInstance().ismCanDraw())) {
                        return;
                    }
                }
                onStartShareScreen(mScreenInfoData);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateActionBarLayoutParams();
        updateSmallVideoLayoutParams();
        refreshRotation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent();
    }

    private void parseIntent() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            myDisplayName = bundle.getString("name");
            meetingPwd = bundle.getString("password");
            sessionName = bundle.getString("sessionName");
        }
    }

    /* ------------ INIT METHODS ------------ */
    private void initView() {
        sessionNameText = findViewById(R.id.sessionName);
        userVideoList = findViewById(R.id.userVideoList);
        videoContain = findViewById(R.id.big_video_contain);
        videoListContain = findViewById(R.id.video_list_contain);
        adapter = new UserVideoAdapter(this, this);
        userVideoList.setItemViewCacheSize(0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        layoutManager.setItemPrefetchEnabled(false);
        userVideoList.setLayoutManager(layoutManager);
        userVideoList.setAdapter(adapter);
        actionBar = findViewById(R.id.action_bar);

        iconVideo = findViewById(R.id.icon_video);
        iconAudio = findViewById(R.id.icon_audio);
        practiceText = findViewById(R.id.text_meeting_user_size);
        iconShare = findViewById(R.id.icon_share);
        actionBarScroll = findViewById(R.id.action_bar_scroll);

        videoOffView = findViewById(R.id.video_off_tips);

        btnViewShare = findViewById(R.id.btn_view_share);

        final int margin = (int) (5 * displayMetrics.scaledDensity);
        userVideoList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(margin, 0, margin, 0);
            }
        });

        userVideoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    int index = linearLayoutManager.findFirstVisibleItemPosition();
                    int left = view.getLeft();
                    if (left < 0) {
                        if (-left > view.getWidth() / 2) {
                            index = index + 1;
                            if (index == adapter.getItemCount() - 1) {
                                recyclerView.scrollBy(view.getWidth(), 0);
                            } else {
                                recyclerView.scrollBy(view.getWidth() + left + 2 * margin, 0);
                            }
                        } else {
                            recyclerView.scrollBy(left - margin, 0);
                        }
                        if (index == 0) {
                            recyclerView.scrollTo(0, 0);
                        }
                    }
                    view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    scrollVideoViewForMargin(view);
                }
            }
        });
    }

    private void initMeeting() {
        ZoomVideoSDK.getInstance().addListener(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        zoomCanvas = new ZoomVideoSDKVideoView(this, false);
        videoContain.addView(zoomCanvas, 0, params);

        ZoomVideoSDKUser mySelf = ZoomVideoSDK.getInstance().getSession().getMySelf();
        subscribeVideoByUser(mySelf);
    }

    /* ------------ MEETING SERVICE CREATION ------------ */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected:");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected:");
        }
    };

    private void startMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        if (null != serviceConnection) {
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void stopMeetingService() {
        Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);
        try {
            if (null != serviceConnection) {
                unbindService(serviceConnection);
            }
            serviceConnection = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /* ------------ DELEGATE METHODS ------------ */
    @Override
    public void onSessionJoin() {
        Log.d(TAG, "onSessionJoin ");
        updateSessionInfo();
        actionBar.setVisibility(View.VISIBLE);
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
        }

        adapter.onUserJoin(UserHelper.getAllUsers());
        refreshUserListAdapter();

        startMeetingService();
    }

    @Override
    public void onSessionLeave() {
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
        finish();
    }

    @Override
    public void onError(int errorcode) {
        Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(errorcode) + ". Error code: " + errorcode, Toast.LENGTH_LONG).show();
        if (errorcode == ZoomVideoSDKErrors.Errors_Session_Disconnect) {
            unSubscribe();
            adapter.clear(true);
            updateSessionInfo();
            currentShareUser = null;
            mActiveUser = null;
            btnViewShare.setVisibility(View.GONE);
        } else if (errorcode == ZoomVideoSDKErrors.Errors_Session_Reconncting) {
            //start preview
//            subscribeVideoByUser(session.getMySelf());
        } else {
            ZoomVideoSDK.getInstance().leaveSession(false);
            finish();
        }
    }

    @Override
    public void onUserJoin(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {
        Log.d(TAG, "onUserJoin " + userList.size());
        updateVideoListLayout();
        if (!isActivityPaused) {
            adapter.onUserJoin(userList);
        }
        refreshUserListAdapter();
        updateSessionInfo();
    }

    @Override
    public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {
        updateVideoListLayout();
        Log.d(TAG, "onUserLeave " + userList.size());
        adapter.onUserLeave(userList);

        if (adapter.getItemCount() == 0) {
            videoListContain.setVisibility(View.INVISIBLE);
        }
        if (null == mActiveUser || userList.contains(mActiveUser)) {
            subscribeVideoByUser(session.getMySelf());
            selectAndScrollToUser(session.getMySelf());
        }
        updateSessionInfo();
    }

    @Override
    public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {
        Log.d(TAG, "onUserVideoStatusChanged ");
        if (null == iconVideo) {
            return;
        }

        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null != zoomSDKUserInfo) {
            iconVideo.setImageResource(zoomSDKUserInfo.getVideoStatus().isOn() ? R.drawable.icon_video_off : R.drawable.icon_video_on);

        }
        adapter.onUserVideoStatusChanged(userList);
    }

    @Override
    public void onUserAudioStatusChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> userList) {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (zoomSDKUserInfo != null && userList.contains(zoomSDKUserInfo)) {
            if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomVideoSDKAudioStatus.ZoomVideoSDKAudioType.ZoomVideoSDKAudioType_None) {
                iconAudio.setImageResource(R.drawable.icon_join_audio);
            } else {
                if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                    iconAudio.setImageResource(R.drawable.icon_unmute);
                } else {
                    iconAudio.setImageResource(R.drawable.icon_mute);
                }
            }
        }
    }

    @Override
    public void onLiveStreamStatusChanged(ZoomVideoSDKLiveStreamHelper liveStreamHelper, ZoomVideoSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatMessage messageItem) {

    }

    @Override
    public void onChatDeleteMessageNotify(ZoomVideoSDKChatHelper chatHelper, String msgID, ZoomVideoSDKChatMessageDeleteType deleteBy) {

    }

    @Override
    public void onChatPrivilegeChanged(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatPrivilegeType currentPrivilege) {

    }

    @Override
    public void onUserHostChanged(ZoomVideoSDKUserHelper userHelper, ZoomVideoSDKUser userInfo) {

    }

    @Override
    public void onUserManagerChanged(ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserNameChanged(ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserActiveAudioChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> list) {
        adapter.onUserActiveAudioChanged(list, userVideoList);
    }

    @Override
    public void onSessionNeedPassword(ZoomVideoSDKPasswordHandler handler) {

    }

    @Override
    public void onSessionPasswordWrong(ZoomVideoSDKPasswordHandler handler) {

    }

    @Override
    public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {

    }

    @Override
    public void onShareAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onCommandReceived(ZoomVideoSDKUser sender, String strCmd) {

    }

    @Override
    public void onCommandChannelConnectResult(boolean isSuccess) {

    }

    @Override
    public void onCloudRecordingStatus(ZoomVideoSDKRecordingStatus status, ZoomVideoSDKRecordingConsentHandler handler) {

    }

    @Override
    public void onHostAskUnmute() {

    }

    @Override
    public void onInviteByPhoneStatus(ZoomVideoSDKPhoneStatus status, ZoomVideoSDKPhoneFailedReason reason) {

    }

    @Override
    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKRawDataPipe videoPipe) {

    }

    @Override
    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKVideoCanvas canvas) {

    }

    @Override
    public void onLiveTranscriptionStatus(ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus status) {

    }

    @Override
    public void onLiveTranscriptionMsgReceived(String ltMsg, ZoomVideoSDKUser pUser, ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionOperationType type) {

    }

    @Override
    public void onOriginalLanguageMsgReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

    }

    @Override
    public void onLiveTranscriptionMsgInfoReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

    }

    @Override
    public void onLiveTranscriptionMsgError(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage spokenLanguage, ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage transcriptLanguage) {

    }

    @Override
    public void onProxySettingNotification(ZoomVideoSDKProxySettingHandler handler) {

    }

    @Override
    public void onSSLCertVerifiedFailNotification(ZoomVideoSDKSSLCertificateInfo info) {

    }

    @Override
    public void onCameraControlRequestResult(ZoomVideoSDKUser user, boolean isApproved) {

    }

    @Override
    public void onUserVideoNetworkStatusChanged(ZoomVideoSDKNetworkStatus status, ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserRecordingConsent(ZoomVideoSDKUser user) {

    }

    @Override
    public void onCallCRCDeviceStatusChanged(ZoomVideoSDKCRCCallStatus status) {

    }

    @Override
    public void onVideoCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

    }

    @Override
    public void onShareCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

    }

    @Override
    public void onAnnotationHelperCleanUp(ZoomVideoSDKAnnotationHelper helper) {

    }

    @Override
    public void onAnnotationPrivilegeChange(boolean enable, ZoomVideoSDKUser shareOwner) {

    }

    @Override
    public void onClickStopShare() {
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
            Intent intent = new Intent(getApplicationContext(), IntegrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setAction(IntegrationActivity.ACTION_RETURN_TO_CONF);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
    }

    /* ------------ CLICK ACTIONS ------------ */
    public void onClickEnd(View view) {
        unSubscribe();
        adapter.clear(true);
        actionBar.setVisibility(View.GONE);
        int ret = ZoomVideoSDK.getInstance().leaveSession(true);
        Log.d(TAG, "leaveSession ret = " + ret);

    }

    public void onClickVideo(View view) {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo) return;
        if (zoomSDKUserInfo.getVideoStatus().isOn()) {
            ZoomVideoSDK.getInstance().getVideoHelper().stopVideo();
        } else {
            List<ZoomVideoSDKCameraDevice> list = ZoomVideoSDK.getInstance().getVideoHelper().getCameraList();
            ZoomVideoSDK.getInstance().getVideoHelper().startVideo();
            for (ZoomVideoSDKCameraDevice item : list) {
                Log.d("ajitha", item.getDeviceName());
                Log.d("ajitha", String.valueOf(item.isSelectedDevice()));
                Log.d("ajitha", item.getDeviceId());

                if(item.getDeviceName().contains("Back")) {
                    ZoomVideoSDK.getInstance().getVideoHelper().switchCamera(item);
                }
            }
        }
    }

    public void onClickShare(View view) {
        ZoomVideoSDKShareHelper sdkShareHelper = ZoomVideoSDK.getInstance().getShareHelper();

        boolean isShareLocked = sdkShareHelper.isShareLocked();
        if (isShareLocked && !session.getMySelf().isHost()) {
            Toast.makeText(this, "Share is locked by host", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null != currentShareUser && currentShareUser != session.getMySelf()) {
            Toast.makeText(this, "Other is shareing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentShareUser == session.getMySelf()) {
            sdkShareHelper.stopShare();
            return;
        }
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
            if (null != shareToolbar) {
                shareToolbar.destroy();
            }
        } else {
            askScreenSharePermission();
        }
    }

    public void onClickAudio(View view) {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo) return;
        if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomVideoSDKAudioStatus.ZoomVideoSDKAudioType.ZoomVideoSDKAudioType_None) {
            ZoomVideoSDK.getInstance().getAudioHelper().startAudio();
        } else {
            if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                ZoomVideoSDK.getInstance().getAudioHelper().unMuteAudio(zoomSDKUserInfo);
            } else {
                ZoomVideoSDK.getInstance().getAudioHelper().muteAudio(zoomSDKUserInfo);
            }
        }
    }

    @Override
    public void onSingleTap(ZoomVideoSDKUser user) {
        subscribeVideoByUser(user);
    }

    /* ------------ SUBSCRIPTION / UN-SUBSCRIPTION METHODS ------------ */
    private void subscribeShareByUser(ZoomVideoSDKUser user) {
        if (null != mActiveUser) {
            mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
            mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
        }
        if (ZoomVideoSDK.getInstance().getShareHelper().isOtherSharing()) {
            user.getShareCanvas().subscribe(zoomCanvas, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
        }
        mActiveUser = user;
        btnViewShare.setVisibility(View.GONE);
    }

    private void subscribeVideoByUser(ZoomVideoSDKUser user) {
        ZoomVideoSDKVideoAspect aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_LetterBox;
        if (ZoomVideoSDK.getInstance().isInSession()) {
            aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original;
        }
        if (null != currentShareUser) {
            currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
        }
        user.getVideoCanvas().unSubscribe(zoomCanvas);
        int ret = user.getVideoCanvas().subscribe(zoomCanvas, aspect);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            Toast.makeText(this, "subscribe error:" + ret, Toast.LENGTH_LONG).show();
        }

        mActiveUser = user;

        if (null != currentShareUser) {
            btnViewShare.setVisibility(View.VISIBLE);
        } else {
            btnViewShare.setVisibility(View.GONE);
        }

    }

    protected void resumeSubscribe() {
        if (null != currentShareUser) {
            subscribeShareByUser(currentShareUser);
        } else if (null != mActiveUser) {
            subscribeVideoByUser(mActiveUser);
        }

        if (ZoomVideoSDK.getInstance().isInSession()) {
            List<ZoomVideoSDKUser> userInfoList = UserHelper.getAllUsers();
            if (null != userInfoList && userInfoList.size() > 0) {
                List<ZoomVideoSDKUser> list = new ArrayList<>(userInfoList.size());
                for (ZoomVideoSDKUser userInfo : userInfoList) {
                    list.add(userInfo);
                }
                adapter.onUserJoin(list);
                selectAndScrollToUser(mActiveUser);
            }
        }
    }

    private void unSubscribe() {
        if (null != currentShareUser) {
            currentShareUser.getVideoCanvas().unSubscribe(zoomCanvas);
            currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
        }
        if (null != mActiveUser) {
            mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
            mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
        }
    }

    /* ------------ UI RELATED METHODS ------------ */
    private void selectAndScrollToUser(ZoomVideoSDKUser user) {
        if (null == user) {
            return;
        }
        adapter.updateSelectedVideoUser(user);
        int index = adapter.getIndexByUser(user);
        if (index >= 0) {
            LinearLayoutManager manager = (LinearLayoutManager) userVideoList.getLayoutManager();
            int first = manager.findFirstVisibleItemPosition();
            int last = manager.findLastVisibleItemPosition();
            if (index > last || index < first) {
                userVideoList.scrollToPosition(index);
                adapter.notifyDataSetChanged();
            }
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
        View view = linearLayoutManager.getChildAt(0);
        if (null != view) {
            scrollVideoViewForMargin(view);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    scrollVideoViewForMargin(view);
                }
            }, 50);
        }
    }

    private void scrollVideoViewForMargin(View view) {
        if (null == view) {
            return;
        }
        int left = view.getLeft();
        int margin = 5;
        if (left > margin || left <= 0) {
            userVideoList.scrollBy(left - margin, 0);
        }
    }

    private void refreshUserListAdapter() {
        if (adapter.getItemCount() > 0) {
            videoListContain.setVisibility(View.VISIBLE);
            if (adapter.getSelectedVideoUser() == null) {
                ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
                if (null != zoomSDKUserInfo) {
                    selectAndScrollToUser(zoomSDKUserInfo);
                }
            }
        }
    }

    private void updateVideoListLayout() {
        int size = UserHelper.getAllUsers().size();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userVideoList.getLayoutParams();
        int preWidth = params.width;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (size - 1 >= 3) {
            int maxWidth = (int) (325 * displayMetrics.scaledDensity);
            width = maxWidth;
        }
        if (width != preWidth) {
            params.width = width;
            userVideoList.setLayoutParams(params);
        }
    }

    private void updateSessionInfo() {
        ZoomVideoSDKSession sessionInfo = ZoomVideoSDK.getInstance().getSession();
        if (ZoomVideoSDK.getInstance().isInSession()) {
            int size = UserHelper.getAllUsers().size();
            if (size <= 0) {
                size = 1;
            }
            practiceText.setText("Participants:" + size);
            if (sessionInfo != null) meetingPwd = sessionInfo.getSessionPassword();
        } else {
            actionBar.setVisibility(View.GONE);
            practiceText.setText("Connecting ...");
        }
        if (sessionInfo != null) sessionNameText.setText(sessionInfo.getSessionName());
    }

    private void refreshRotation() {
        int displayRotation = display.getRotation();
        boolean ret = ZoomVideoSDK.getInstance().getVideoHelper().rotateMyVideo(displayRotation);
        Log.d(TAG, "rotateVideo:" + displayRotation + " ret:" + ret);
    }

    private void updateActionBarLayoutParams() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) actionBar.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.topMargin = (int) (35 * displayMetrics.scaledDensity);
            actionBarScroll.scrollTo(0, 0);
        } else {
            params.topMargin = 0;
        }
        actionBar.setLayoutParams(params);

    }

    private void updateSmallVideoLayoutParams() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoListContain.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            videoListContain.setGravity(Gravity.CENTER);
        }
    }

    /* ------------ SHARE SCREEN RELATED METHODS ------------ */

    @SuppressLint("NewApi")
    private void askScreenSharePermission() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            return;
        }
        MediaProjectionManager mgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mgr != null) {
            Intent intent = mgr.createScreenCaptureIntent();
            try {
                startActivityForResult(intent, REQUEST_SHARE_SCREEN_PERMISSION);
            } catch (Exception e) {
                Log.e(TAG, "askScreenSharePermission failed");
            }
        }
    }

    @SuppressLint("NewApi")
    private void startShareScreen(Intent data) {
        if (data == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
            if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged())
                ZMAdapterOsBugHelper.getInstance().startListenOverlayPermissionChange(this);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            mScreenInfoData = data;
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            onStartShareScreen(data);
        }
    }

    private void onStartShareScreen(Intent data) {
        if (null == shareToolbar) {
            shareToolbar = new ShareToolbar(this, this);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            //MediaProjection  need service with foregroundServiceType mediaProjection in android Q
            boolean hasForegroundNotification = NotificationMgr.hasNotification(NotificationMgr.PT_NOTICICATION_ID);
            if (!hasForegroundNotification) {
                Intent intent = new Intent(this, NotificationService.class);
                startForegroundService(intent);
            }
        }
        int ret = ZoomVideoSDK.getInstance().getShareHelper().startShareScreen(data);
        if (ret == ZoomVideoSDKErrors.Errors_Success) {
            shareToolbar.showToolbar();
            showDesktop();
        }
    }

    private void showDesktop() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(home);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            currentShareUser = userInfo;
            if (userInfo != session.getMySelf()) {
                subscribeShareByUser(userInfo);
                selectAndScrollToUser(userInfo);
            } else {
                iconShare.setImageResource(R.drawable.icon_stop_share);
                if (!ZoomVideoSDK.getInstance().getShareHelper().isScreenSharingOut()) {
                    unSubscribe();
                    adapter.clear(false);
                }
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            if (userInfo == session.getMySelf()) {
                /* only self share stop should update the ui */
                iconShare.setImageResource(R.drawable.icon_share);
                if (null != shareToolbar) {
                    shareToolbar.destroy();
                }
            }

            if (currentShareUser == userInfo) {
                currentShareUser = null;
            }

            subscribeVideoByUser(userInfo);
            if (adapter.getItemCount() == 0) {
                adapter.addAll();
            }
            selectAndScrollToUser(userInfo);
        }
    }

    public void onClickSwitchShare(View view) {
        if (null != currentShareUser) {
            subscribeShareByUser(currentShareUser);
            selectAndScrollToUser(mActiveUser);
        }
    }


}