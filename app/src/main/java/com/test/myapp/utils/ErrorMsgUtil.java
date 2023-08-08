package com.test.myapp.utils;

import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Auth_Disable_SDK;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Auth_DoesNot_Support_SDK;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Auth_Empty_Key_or_Secret;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Auth_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Auth_Wrong_Key_or_Secret;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Internal_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Invalid_Parameter;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Load_Module_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Memory_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_SessionModule_Not_Found;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_SessionService_Invaild;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Already_In_Progress;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Audio_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Audio_No_Microphone;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Client_Incompatible;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Disconnect;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Dont_Support_SessionType;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Invalid_Param;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Join_Failed;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Live_Stream_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Need_Password;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_No_Rights;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Not_Started;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Password_Wrong;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Reconncting;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Remote_DB_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Video_Device_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Session_Video_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_UnLoad_Module_Error;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Uninitialize;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Unknown;
import static us.zoom.sdk.ZoomVideoSDKErrors.Errors_Wrong_Usage;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_AUDIO_MODULE_ERROR;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_AUDIO_MODULE_NOT_READY;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_MALLOC_FAILED;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_NOT_IN_Session;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_NO_AUDIO_DATA;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_NO_LICENSE;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_NO_SHARE_DATA;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_NO_VIDEO_DATA;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_SHARE_MODULE_ERROR;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_SHARE_MODULE_NOT_READY;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_VIDEO_DEVICE_ERROR;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_VIDEO_MODULE_ERROR;
import static us.zoom.sdk.ZoomVideoSDKErrors.RawDataError_VIDEO_MODULE_NOT_READY;

public class ErrorMsgUtil {

    public static String getMsgByErrorCode(int errorCode) {
        switch (errorCode) {
            case Errors_Wrong_Usage:
                return "Incorrect use";
            case Errors_Internal_Error:
                return "Internal error";
            case Errors_Uninitialize:
                return "Uninitialized";
            case Errors_Memory_Error:
                return "Memory error";
            case Errors_Load_Module_Error:
                return "Load module failed";
            case Errors_UnLoad_Module_Error:
                return "Unload module failed";
            case Errors_Invalid_Parameter:
                return "Parameter error";
            case Errors_Unknown:
                return "Unknown error";
            case Errors_Auth_Error:
                return "Authentication error";
            case Errors_Auth_Empty_Key_or_Secret:
                return "Empty key or secret";
            case Errors_Auth_Wrong_Key_or_Secret:
                return "Incorrect key or secret";
            case Errors_Auth_DoesNot_Support_SDK:
                return "Authenticated key or secret does not support SDK";
            case Errors_Auth_Disable_SDK:
                return "Disabled SDK with authenticated key or secret";
            case Errors_SessionModule_Not_Found:
                return "Module not found";
            case Errors_SessionService_Invaild:
                return "The service is invalid";
            case Errors_Session_Join_Failed:
                return "Join session failed";
            case Errors_Session_No_Rights:
                return "You don’t have permission to join this session";
            case Errors_Session_Already_In_Progress:
                return "Joining session…";
            case Errors_Session_Dont_Support_SessionType:
                return "Unsupported session type";
            case Errors_Session_Reconncting:
                return "Reconnecting session…";
            case Errors_Session_Disconnect:
                return "Disconnecting session…";
            case Errors_Session_Not_Started:
                return "This session has not started";
            case Errors_Session_Need_Password:
                return "This session requires password";
            case Errors_Session_Password_Wrong:
                return "Incorrect password";
            case Errors_Session_Remote_DB_Error:
                return "Error received from remote database";
            case Errors_Session_Invalid_Param:
                return "Parameter error when joining the session";
            case Errors_Session_Client_Incompatible:
                return "Client force upgrade";
            case Errors_Session_Audio_Error:
                return "Session audio module error";
            case Errors_Session_Audio_No_Microphone:
                return "Session audio no microphone";
            case Errors_Session_Video_Error:
                return "Session video module error";
            case Errors_Session_Video_Device_Error:
                return "Session video device module error";
            case Errors_Session_Live_Stream_Error:
                return "Live stream error";
            case RawDataError_MALLOC_FAILED:
                return "Raw data memory allocation error";
            case RawDataError_NOT_IN_Session:
                return "Not in session when subscribing to raw data";
            case RawDataError_NO_LICENSE:
                return "License without raw data";
            case RawDataError_VIDEO_MODULE_NOT_READY:
                return "Video module is not ready";
            case RawDataError_VIDEO_MODULE_ERROR:
                return "Video module error";
            case RawDataError_VIDEO_DEVICE_ERROR:
                return "Video device error";
            case RawDataError_NO_VIDEO_DATA:
                return "No video data";
            case RawDataError_SHARE_MODULE_NOT_READY:
                return "Share module is not ready";
            case RawDataError_SHARE_MODULE_ERROR:
                return "Share module error";
            case RawDataError_NO_SHARE_DATA:
                return "No sharing data";
            case RawDataError_AUDIO_MODULE_NOT_READY:
                return "Audio module is not ready";
            case RawDataError_AUDIO_MODULE_ERROR:
                return "Audio module error";
            case RawDataError_NO_AUDIO_DATA:
                return "No audio data";
        }
        return String.valueOf(errorCode);
    }
}
