package com.raju.native_developers.referral;

import android.content.Context;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.raju.domain.models.ReferralModel;
import com.support.utills.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallReferrerPlay implements InstallReferrerStateListener {

    private static String TAG = "MixpanelAPI.InstallReferrerPlay";

    private static final int MAX_INSTALL_REFERRER_RETRIES = 5;
    private static final int TIME_MS_BETWEEN_RETRIES = 2500;

    protected static final Pattern UTM_SOURCE_PATTERN = Pattern.compile("(^|&)utm_source=([^&#=]*)([#&]|$)");
    private final Pattern UTM_MEDIUM_PATTERN = Pattern.compile("(^|&)utm_medium=([^&#=]*)([#&]|$)");
    private final Pattern UTM_CAMPAIGN_PATTERN = Pattern.compile("(^|&)utm_campaign=([^&#=]*)([#&]|$)");
    private final Pattern UTM_CONTENT_PATTERN = Pattern.compile("(^|&)utm_content=([^&#=]*)([#&]|$)");
    private final Pattern UTM_TERM_PATTERN = Pattern.compile("(^|&)utm_term=([^&#=]*)([#&]|$)");

    private static boolean sHasStartedConnection = false;

    private Context mContext;
    private ReferrerCallback mCallBack;
    private InstallReferrerClient mReferrerClient;
    private int mRetryCount;
    private Timer mTimer;

    public InstallReferrerPlay(Context appContext, ReferrerCallback callback) {
        this.mContext = appContext;
        this.mCallBack = callback;
        this.mRetryCount = 0;
        this.mTimer = new Timer();
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        boolean shouldRetry = false;
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                try {
                    ReferrerDetails details = mReferrerClient.getInstallReferrer();
                    String referrer = details.getInstallReferrer();
                    saveReferrerDetails(referrer);

                    Log.e(TAG, "details.getGooglePlayInstantParam() " + details.getGooglePlayInstantParam());
                    Log.e(TAG, "details.getInstallBeginTimestampSeconds() " + details.getInstallBeginTimestampSeconds());
                    Log.e(TAG, "details.getInstallVersion() " + details.getInstallVersion());
                    Log.e(TAG, "details.getReferrerClickTimestampSeconds() " + details.getReferrerClickTimestampSeconds());
                    Log.e(TAG, "details.getReferrerClickTimestampServerSeconds() " + details.getReferrerClickTimestampServerSeconds());
                } catch (Exception e) {
                    Log.e(TAG, "There was an error fetching your referrer details.", e);
                    shouldRetry = true;
                }
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                shouldRetry = true;
                Log.d(TAG, "Service is currently unavailable.");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                shouldRetry = true;
                Log.d(TAG, "Service was disconnected unexpectedly.");
                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                Log.d(TAG, "API not available on the current Play Store app.");
                break;
            case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR:
                Log.d(TAG, "Unexpected error.");
                break;
            default:
                break;
        }

        if (shouldRetry) {
            retryConnection();
        } else {
            disconnect();
        }
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        Log.d(TAG, "Install Referrer Service Disconnected.");
        retryConnection();
    }

    public void connect() {
        try {
            mReferrerClient = InstallReferrerClient.newBuilder(mContext).build();
            mReferrerClient.startConnection(this);
            sHasStartedConnection = true;
        } catch (SecurityException e) {
            // see https://issuetracker.google.com/issues/72926755
            Log.e(TAG, "Install referrer client could not start connection", e);
        }
    }

    private void retryConnection() {
        if (mRetryCount > MAX_INSTALL_REFERRER_RETRIES) {
            Log.d(TAG, "Already retried " + MAX_INSTALL_REFERRER_RETRIES + " times. Disconnecting...");
            disconnect();
            return;
        }

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                connect();
            }
        }, TIME_MS_BETWEEN_RETRIES);

        mRetryCount++;
    }

    public void disconnect() {
        if (mReferrerClient != null && mReferrerClient.isReady()) {
            try {
                mReferrerClient.endConnection();
            } catch (Exception e) {
                Log.e(TAG, "Error closing referrer connection", e);
            }
        }
    }

    public static boolean hasStartedConnection() {
        return sHasStartedConnection;
    }

    void saveReferrerDetails(String referrer) {
        if (referrer == null) return;
        ReferralModel referralModel = new ReferralModel();

        referralModel.referrer = referrer;

        final Matcher sourceMatcher = UTM_SOURCE_PATTERN.matcher(referrer);
        final String source = find(sourceMatcher);
        if (null != source) {
            referralModel.utm_source = source;
        }

        final Matcher mediumMatcher = UTM_MEDIUM_PATTERN.matcher(referrer);
        final String medium = find(mediumMatcher);
        if (null != medium) {
            referralModel.utm_medium = medium;
        }

        final Matcher campaignMatcher = UTM_CAMPAIGN_PATTERN.matcher(referrer);
        final String campaign = find(campaignMatcher);
        if (null != campaign) {
            referralModel.utm_campaign = campaign;
        }

        final Matcher contentMatcher = UTM_CONTENT_PATTERN.matcher(referrer);
        final String content = find(contentMatcher);
        if (null != content) {
            referralModel.utm_content = content;
        }

        final Matcher termMatcher = UTM_TERM_PATTERN.matcher(referrer);
        final String term = find(termMatcher);
        if (null != term) {
            referralModel.utm_term = term;
        }

        Log.e(TAG, "utm_campaign" + referralModel.utm_campaign);
        Log.e(TAG, "utm_content" + referralModel.utm_content);
        Log.e(TAG, "utm_term" + referralModel.utm_term);
        Log.e(TAG, "utm_source" + referralModel.utm_source);
        Log.e(TAG, "referrer" + referralModel.referrer);
        Log.e(TAG, "utm_medium" + referralModel.utm_medium);
        if (mCallBack != null) {
            mCallBack.onReferrerReadSuccess(referralModel);
        }
    }


    private String find(Matcher matcher) {
        if (matcher.find()) {
            final String encoded = matcher.group(2);
            if (null != encoded) {
                try {
                    return URLDecoder.decode(encoded, "UTF-8");
                } catch (final UnsupportedEncodingException e) {
                    Log.e(TAG, "Could not decode a parameter into UTF-8");
                }
            }
        }
        return null;
    }

    public interface ReferrerCallback {
        void onReferrerReadSuccess(ReferralModel referralModel);
    }
}