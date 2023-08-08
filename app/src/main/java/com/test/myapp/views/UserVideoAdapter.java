package com.test.myapp.views;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.myapp.R;
import com.test.myapp.utils.UserHelper;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoView;

public class UserVideoAdapter extends RecyclerView.Adapter<UserVideoAdapter.BaseHolder> {

    private static final String TAG = "UserVideoAdapter";

    public interface ItemTapListener {
        void onSingleTap(ZoomVideoSDKUser user);
    }

    private ItemTapListener tapListener;

    private List<ZoomVideoSDKUser> userList = new ArrayList<>();

    private Context context;


    private ZoomVideoSDKUser selectedVideoUser;

    private List<ZoomVideoSDKUser> activeAudioList;


    public UserVideoAdapter(Context context, ItemTapListener listener) {
        this.context = context;
        tapListener = listener;
    }

    public ZoomVideoSDKUser getSelectedVideoUser() {
        return selectedVideoUser;
    }

    public void updateSelectedVideoUser(ZoomVideoSDKUser user) {
        if (null == user) {
            return;
        }
        int index = userList.indexOf(user);
        if (index >= 0) {
            selectedVideoUser = user;
            notifyItemRangeChanged(0, userList.size(), "active");
        }
    }

    public int getIndexByUser(ZoomVideoSDKUser user) {
        return userList.indexOf(user);
    }

    public void clear(boolean resetSelect) {
        userList.clear();
        if (resetSelect) {
            selectedVideoUser = null;
        }
        notifyDataSetChanged();
    }

    public void onUserVideoStatusChanged(List<ZoomVideoSDKUser> changeList) {

        for (ZoomVideoSDKUser user : changeList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                notifyItemChanged(index, "avar");
            }
        }
    }

    public void addAll() {
        userList.clear();
        List<ZoomVideoSDKUser> all = UserHelper.getAllUsers();
        userList.addAll(all);
        notifyDataSetChanged();
    }

    public void onUserJoin(List<ZoomVideoSDKUser> joinList) {
        for (ZoomVideoSDKUser user : joinList) {
            if (!userList.contains(user)) {
                userList.add(user);
                notifyItemInserted(userList.size());
            }
        }
        checkUserList();
    }

    private void checkUserList() {
        List<ZoomVideoSDKUser> all = UserHelper.getAllUsers();
        if (all.size() != userList.size()) {
            userList.clear();
            for (ZoomVideoSDKUser userInfo : all) {
                userList.add(userInfo);
            }
            notifyDataSetChanged();
        }
    }

    public void onUserLeave(List<ZoomVideoSDKUser> leaveList) {

        boolean refreshActive = false;
        if (null != selectedVideoUser && leaveList.contains(selectedVideoUser)) {
            selectedVideoUser = ZoomVideoSDK.getInstance().getSession().getMySelf();
            refreshActive = true;
        }
        for (ZoomVideoSDKUser user : leaveList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                userList.remove(index);
                notifyItemRemoved(index);
            }
        }
        if (refreshActive) {
            notifyItemRangeChanged(0, userList.size(), "active");
        }
        checkUserList();
    }


    public void onUserActiveAudioChanged(List<ZoomVideoSDKUser> list, RecyclerView userVideoList) {
        activeAudioList = list;
        int childCount = userVideoList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = userVideoList.getChildAt(i);
            int position = userVideoList.getChildAdapterPosition(view);
            if (position >= 0 && position < userList.size()) {
                ZoomVideoSDKUser userId = userList.get(position);
                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(position);
                if (null != holder) {
                    if (null != activeAudioList && activeAudioList.contains(userId)) {
                        holder.audioStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.audioStatus.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_video, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {

        onBindViewHolder(holder, position, null);
    }


    @Override
    public void onViewRecycled(@NonNull BaseHolder holder) {
        super.onViewRecycled(holder);
        VideoHolder viewHolder = (VideoHolder) holder;
        viewHolder.user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);

    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position, @NonNull List<Object> payloads) {

        ZoomVideoSDKUser user = userList.get(position);
        VideoHolder viewHolder = (VideoHolder) holder;

        if (null == payloads || payloads.isEmpty() || payloads.contains("video")) {
            subscribeVideo(user, viewHolder);
        }
        viewHolder.user = user;

        if (null != user) {
            if (!user.getVideoStatus().isOn()) {
                viewHolder.video_off_contain.setVisibility(View.VISIBLE);
                viewHolder.video_off_tips.setImageResource(R.drawable.zm_conf_no_avatar);
            } else {
                viewHolder.video_off_contain.setVisibility(View.INVISIBLE);
            }
            viewHolder.userNameText.setText(user.getUserName());
        }

        if (selectedVideoUser == user) {
            viewHolder.itemView.setBackgroundResource(R.drawable.video_active_item_bg);
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.video_item_bg);
        }

        if (null != activeAudioList && activeAudioList.contains(user)) {
            viewHolder.audioStatus.setVisibility(View.VISIBLE);
        } else {
            viewHolder.audioStatus.setVisibility(View.GONE);
        }

    }

    private void subscribeVideo(ZoomVideoSDKUser user, VideoHolder viewHolder) {
        user.getVideoCanvas().setResolution(viewHolder.videoRenderer, ZoomVideoSDKVideoResolution.VideoResolution_90P);
        user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);
        int ret = user.getVideoCanvas().subscribe(viewHolder.videoRenderer, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_PanAndScan);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            Log.e(TAG, "subscribeVideo fail:" + ret + " user:" + user.getUserID() + ":" + user.getUserName());
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        protected View view;

        BaseHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    class VideoHolder extends BaseHolder {

        ZoomVideoSDKVideoView videoRenderer;

        ImageView emojiView;

        ImageView audioStatus;

        View itemView;

        TextView userNameText;

        ImageView video_off_tips;

        View video_off_contain;

        ZoomVideoSDKUser user;

        VideoHolder(View view) {
            super(view);
            itemView = view;
            video_off_tips = view.findViewById(R.id.video_off_tips);
            audioStatus = view.findViewById(R.id.item_audio_status);
            userNameText = view.findViewById(R.id.item_user_name);
            video_off_contain = view.findViewById(R.id.video_off_contain);

            videoRenderer = view.findViewById(R.id.videoRenderer);


            videoRenderer.setVisibility(View.VISIBLE);
            videoRenderer.setZOrderMediaOverlay(true);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != tapListener) {
                        if (selectedVideoUser == user) {
                            return;
                        }
                        tapListener.onSingleTap(user);
                        selectedVideoUser = user;
                        notifyItemRangeChanged(0, getItemCount(), "active");
                    }
                }
            });
        }
    }
}
