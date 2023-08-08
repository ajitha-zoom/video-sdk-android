package com.test.myapp.utils;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKUser;

public class UserHelper {
    public static List<ZoomVideoSDKUser> getAllUsers() {
        List<ZoomVideoSDKUser> userList = new ArrayList<>();
        ZoomVideoSDKSession sdkSession = ZoomVideoSDK.getInstance().getSession();
        if (sdkSession == null) {
            return userList;
        }
        if (sdkSession.getMySelf() != null) {
            userList.add(sdkSession.getMySelf());
        }
        userList.addAll(sdkSession.getRemoteUsers());
        return userList;
    }
}
