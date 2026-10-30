/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.mediatek.incallservice;

import android.media.AudioManager;

import android.telephony.TelephonyManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.app.Service;
import android.os.IBinder;

import android.util.Log;

public class VolumeChangeService extends Service {
    public static final String LOG_TAG = "MediatekInCallService";

    private AudioManager mAudioManager;
    private TelephonyManager mTelephonyManager;
    private VolumeChangeReceiver mVolumeChangeReceiver;
    private CallStateListener mCallStateListener;
    private AudioManager.OnCommunicationDeviceChangedListener mDeviceChangedListener;
    private AudioManager.OnModeChangedListener mModeChangedListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = getSystemService(AudioManager.class);
        mTelephonyManager = getSystemService(TelephonyManager.class);
        mVolumeChangeReceiver = new VolumeChangeReceiver(mAudioManager);
        mCallStateListener = new CallStateListener(mAudioManager);
        mDeviceChangedListener = device -> {
            if (mAudioManager.getMode() == AudioManager.MODE_IN_CALL) {
                GainUtils.setGainLevel(mAudioManager);
            }
        };
        mModeChangedListener = mode -> {
            if (mode == AudioManager.MODE_IN_CALL) {
                GainUtils.setGainLevel(mAudioManager);
            }
        };

        Log.i(LOG_TAG, "Service is starting...");

        this.registerReceiver(mVolumeChangeReceiver,
                               new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION));

        mTelephonyManager.registerTelephonyCallback(getMainExecutor(), mCallStateListener);
        mAudioManager.addOnCommunicationDeviceChangedListener(
                getMainExecutor(), mDeviceChangedListener);
        mAudioManager.addOnModeChangedListener(getMainExecutor(), mModeChangedListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mAudioManager.removeOnModeChangedListener(mModeChangedListener);
        mAudioManager.removeOnCommunicationDeviceChangedListener(mDeviceChangedListener);
        mTelephonyManager.unregisterTelephonyCallback(mCallStateListener);
        unregisterReceiver(mVolumeChangeReceiver);
        super.onDestroy();
    }
}
