package io.iclue.backgroundvideo;

import android.util.Log;
import android.view.ViewGroup;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;


public class BackgroundVideo extends CordovaPlugin {
    private static final String TAG = "BACKGROUND_VIDEO";
    private static final String ACTION_START_RECORDING = "start";
    private static final String ACTION_STOP_RECORDING = "stop";
    private static final String FILE_EXTENSION = ".mp4";
    private String FILE_PATH = "";
    private String FILE_NAME = "";

    //private final static float opacity = 0.3f;
    private VideoOverlay videoOverlay;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        FILE_PATH = cordova.getActivity().getFilesDir().toString() + "/";
    }


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        try {
        	Log.d(TAG, "ACTION: " + action);

            if(ACTION_START_RECORDING.equals(action)) {
                FILE_NAME = args.getString(0);
                String CAMERA_FACE = args.getString(1);

                if(videoOverlay == null) {
                    videoOverlay = new VideoOverlay(cordova.getActivity(), getFilePath());
                    videoOverlay.setCameraFacing(CAMERA_FACE);
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
						public void run() {
                            webView.setKeepScreenOn(true);

                            if(videoOverlay.getViewType() == PreviewType.TEXTURE_VIEW) {
                                cordova.getActivity().addContentView(videoOverlay, new ViewGroup.LayoutParams(webView.getWidth(), webView.getHeight()));
                            } else {
                                // Set to 1 because we cannot have a transparent surface view, therefore view is not shown / tiny.
                                cordova.getActivity().addContentView(videoOverlay, new ViewGroup.LayoutParams(1, 1));
                            }
                        }
                    });
                } else {
                    videoOverlay.setCameraFacing(CAMERA_FACE);
                    videoOverlay.setFilePath(getFilePath());

                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() { videoOverlay.startPreview(true); }
                    });
                }
                return true;
            }

            if(ACTION_STOP_RECORDING.equals(action)) {
                if(videoOverlay != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() { videoOverlay.onPause(); }
                    });
                }
                return true;
            }

            callbackContext.error("INVALID ACTION");
            return false;
        } catch(Exception e) {
            Log.d(TAG, "ERROR: " + e.getMessage(), e);
            callbackContext.error(TAG + " : " + e.getMessage());
            return false;
        }
    }

    private String getFilePath(){
        return  FILE_PATH + getNextFileName() + FILE_EXTENSION;
    }

    private String getNextFileName(){
        int i=1;
        String tmpFileName = FILE_NAME;
        while(new File(FILE_PATH + tmpFileName + FILE_EXTENSION).exists()) {
            tmpFileName = FILE_NAME + '_' + i;
            i++;
        }
        return tmpFileName;
    }

    //Plugin Method Overrides
    @Override
    public void onPause(boolean multitasking) {
        if(videoOverlay != null)
            videoOverlay.onPause();
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if(videoOverlay != null)
            videoOverlay.onResume();
    }

    @Override
    public void onDestroy() {
        if(videoOverlay != null)
            videoOverlay.onDestroy();
        super.onDestroy();
    }

}