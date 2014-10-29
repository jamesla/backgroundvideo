package io.iclue.backgroundvideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import org.apache.cordova.CallbackContext;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoPreview extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "BACKGROUND_VIDEO";

    public VideoRecorder myVideoRecorder;
    public String path;
    public CallbackContext myCallbackContext;

    public VideoPreview(Context context, String filePath, VideoRecorder recorder, CallbackContext callback) {
        super(context);
        
        this.path = filePath;
        this.myVideoRecorder = recorder;
        this.myCallbackContext = callback;

        this.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.setSurfaceTextureListener(this);
        this.setClickable(false);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        boolean isInitialized = false;
        boolean started = myVideoRecorder.isRecording;
        int attempts = 0;

        while (attempts < 3 && !started) {
            try {
                isInitialized = myVideoRecorder.Initialize(this, path);

                //Setup Media Recorder and Start Recording
                if (isInitialized) {
                    started = myVideoRecorder.Start();
                    
                    if (started) {
                        break;
                    } else {
                        throw new Exception(TAG + " : DID NOT START");
                    }
                } else {
                    throw new Exception(TAG + " : MEDIA RECORDER NOT INITIALIZED");
                }
            } catch (Exception e) {
                Log.e(TAG, "EXCEPTION", e);
                try {
                    myVideoRecorder.Stop();
                } catch (Exception ex) {
                    Log.e(TAG,"Stop Failed", ex);
                }

                if (attempts >= 2) {
                    myCallbackContext.error("RECORDING FAILED : " + e.getMessage());
                    Log.e(TAG,"Failed to start recording", e);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } finally {
                if (!started) {
                    Log.d(TAG, "Failed to start video, retrying " + attempts);
                    attempts++;
                }
            }
        }

        if (started && isInitialized) {
            myCallbackContext.success();
        } else {
            myCallbackContext.error(TAG + ": Could not start media recorder is not initialized");
        }
    }
    
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
}
