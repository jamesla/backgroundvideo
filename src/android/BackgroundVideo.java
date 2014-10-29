package io.iclue.backgroundvideo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Locale;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class BackgroundVideo extends CordovaPlugin {
    public static final String TAG = "BACKGROUND_VIDEO";
    public static final String ACTION_START_RECORDING = "start";
    public static final String ACTION_STOP_RECORDING = "stop";

    String FILE_PATH = "";
    String FILE_NAME = "";
    
    static final String FILE_EXTENTION = ".mp4";
    static final String FILE_SUFFIX = "-combined";

    private final static float opacity = 0.3f;

    protected RelativeLayout myRelativeLayout;
    protected VideoPreview myVideoPreview;
    protected VideoRecorder myVideoRecorder;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        FILE_PATH = cordova.getActivity().getFilesDir().toString() + "/";
    }


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_START_RECORDING.equals(action)) {
                FILE_NAME = args.getString(0);
                String CAMERA_FACE = args.getString(1);
                    
                if (myVideoRecorder != null && myVideoRecorder.isRecording == true) {
                    Log.d(TAG, "Already Recording");
                    return true;
                }

                if (myVideoRecorder == null) {
                    myVideoRecorder = new VideoRecorder();
                }
                myVideoRecorder.SetCameraFacing(CAMERA_FACE);

                if (myRelativeLayout == null) {
                    final Activity act = cordova.getActivity();
                    final WebView wv = webView;
                    
                    myVideoPreview = new VideoPreview(act, getFilePath(), this.myVideoRecorder, callbackContext);
                    
                    myRelativeLayout = new RelativeLayout(act);
                    myRelativeLayout.addView(myVideoPreview);
                    myRelativeLayout.setAlpha(opacity);

                    cordova.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            wv.setKeepScreenOn(true);
                            act.addContentView(myRelativeLayout, new ViewGroup.LayoutParams(wv.getWidth(), wv.getHeight()));
                        }
                    });
                } else {
                    SetSurfaceVisibility(View.VISIBLE);
                    myVideoPreview.myCallbackContext = callbackContext;
                    myVideoPreview.onSurfaceTextureAvailable(myVideoPreview.getSurfaceTexture(), myVideoPreview.getWidth(), myVideoPreview.getHeight());
                }

                PluginResult pr = new PluginResult(Status.NO_RESULT);
                pr.setKeepCallback(true);
                callbackContext.sendPluginResult(pr);
                return true;
            }

            if (ACTION_STOP_RECORDING.equals(action)) {
                this.Stop();
                return true;
            }

            callbackContext.error("INVALID ACTION");
            return false;
        } catch(Exception e) {
            Log.d(TAG, "ERROR: " + e.getMessage());
            callbackContext.error(TAG + " : " + e.getMessage());
            return false;
        }
    }


    private void SetSurfaceVisibility(final int visibility){
        if (myVideoPreview == null)
            return;

        if (myVideoPreview.getVisibility() != visibility)
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    myVideoPreview.setVisibility(visibility);
                }
            });
    }


    private String getFilePath(){
        return  FILE_PATH + getNextFileName() + FILE_EXTENTION;
    }

    private String getNextFileName(){
        int i=1;
        String tmpFileName = FILE_NAME;
        while(new File(FILE_PATH + tmpFileName + FILE_EXTENTION).exists()) {
            tmpFileName = FILE_NAME + '_' + i;
            i++;
        }
        return tmpFileName;
    }


    private void Stop(){
        if (myVideoRecorder != null){
            myVideoRecorder.Stop();
        }

        SetSurfaceVisibility(View.GONE);
    }


    //Plugin Method Overrides
    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause - stopping video");
        this.Stop();
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    @Override
    public void onDestroy() {
        this.Stop();
        super.onDestroy();
    }

}