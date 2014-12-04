package io.iclue.backgroundvideo;


import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

public class VideoOverlay extends ViewGroup {
    private static final String TAG = "BACKGROUND_VID_OVERLAY";
    private final Preview preview;
    private MediaRecorder recorder = null;
    private Camera camera = null;
    private int cameraId;
    private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String filePath = "";

    private boolean inPreview = false;
    private boolean viewIsAttached = false;


    public VideoOverlay(Context context, String filePath) {
        super(context);
        this.filePath = filePath;
        preview = getPreview();
        addView(preview.getView());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int numChildren = getChildCount();
        if (changed && numChildren > 0) {
            int itemWidth = (r - l) / numChildren;
            for (int i = 0; i < numChildren; i++) {
                View v = getChildAt(i);
                v.layout(itemWidth * i, 0, (i + 1) * itemWidth, b - t);
            }
        }
    }

    public boolean isRecording() {
        return recorder != null;
    }

    public void startRecording() {
        if (isRecording()) {
            Log.d(TAG, "Already Recording!");
            return;
        }

        initCamera();

        if (camera == null)
            throw new NullPointerException("Cannot start recording, we don't have a camera!");

        Camera.Parameters cameraParameters = camera.getParameters();
        cameraParameters.setRotation(90);
        camera.setParameters(cameraParameters);

        camera.stopPreview(); //Not sure about this one.
        camera.unlock();

        try {
            recorder = new MediaRecorder();
            recorder.setCamera(camera);

            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            setProfile(cameraParameters);
            recorder.setOutputFile(filePath);
            recorder.setOrientationHint(90);

            preview.attach(recorder);
            recorder.prepare();
            recorder.start();
        }
        catch (IOException e) {
            recorder.reset();
            recorder.release();
            recorder=null;
            camera.lock();
            Log.e(TAG, "Could not start recording! MediaRecorder Error", e);
        }
    }

    public void stopRecording() throws IOException {
        Log.d(TAG, "stopRecording called");

        if(recorder != null) {
            MediaRecorder tempRecorder = recorder;
            recorder = null;
            tempRecorder.stop();
            tempRecorder.reset();
            tempRecorder.release();
            camera.lock();
        }
    }

    private void initCamera(){
        if (camera == null) {
            // Find the total number of cameras available
            int mNumberOfCameras = Camera.getNumberOfCameras();

            // Find the ID of the back-facing ("default") camera
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < mNumberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    cameraId = i;
                    camera = Camera.open(i);
                    return;
                }
            }
            cameraId = -1;
            camera = null;
        }
    }

    private Preview getPreview() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return new TextureViewPreview(this);
        else
            return new SurfaceViewPreview(this);
    }

    void previewAvailable(){
        viewIsAttached = true;
        initCamera();
        if(camera != null) {
            try {
                preview.attach(camera);
            } catch (IOException e) {
                Log.e(TAG, "Unable to attach preview to camera!", e);
            }
        }
    }

    public void initPreview(int height, int width) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();

            Camera.Size previewSize = CameraHelper.getPreviewSize(parameters);
            parameters.setPreviewSize(previewSize.width, previewSize.height);

            parameters.setRotation(90);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parameters.setRecordingHint(true);
            }

            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            inPreview = true;
        }
    }

    private void setProfile(Camera.Parameters params){
        CamcorderProfile profile;

        if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
        } else {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
        }

        //Sometimes the profile is incorrect - (Front Camera)
        if (!CameraHelper.sizeSupported(params, profile)) {
            Camera.Size size = CameraHelper.getLowestResolution(params);
            profile.videoFrameWidth = size.width;
            profile.videoFrameHeight = size.height;
        }

//        NOTE: Check if we need to set other params?
//        Camera.Size previewSize = CameraHelper.getPreviewSize(cp);
//        cp.setPreviewSize(previewSize.width, previewSize.height);
//        //Not sure on this one, seems to give odd results on some phones
//        //cp.setRecordingHint(true);
//        cp.setRotation(90);
//        params.set("cam_mode", 1);
//        myCamera.setDisplayOrientation(90);

        recorder.setProfile(profile);
    }


    public void startPreview(boolean startRecording){
        if(!inPreview) {
            if(preview != null && !viewIsAttached ) {
                preview.startRecordingWhenAvailable(startRecording);
                addView(preview.getView());
            } else {
                previewAvailable();
                initPreview(getHeight(), getWidth());
                if (startRecording)
                    startRecording();
                inPreview = true;
            }
        }
    }

    public void stopPreview() {
        Log.d(TAG, "stopPreview called");
        if (inPreview && camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
        }

        if(camera != null) {
            camera.lock();
            camera.release();
            camera = null;
        }

        inPreview = false;
    }

    public void setCameraFacing(String cameraFace) {
        cameraFacing = ( cameraFace.equalsIgnoreCase("FRONT") ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK );
    }

    public void onResume() {
        addView(preview.getView());
        viewIsAttached = true;
    }

    public void onPause() {
        try {
            Log.d(TAG, "onPause called");
            stopRecording();
            stopPreview();
            preview.startRecordingWhenAvailable(false);
            Log.d(TAG, "removing View");
            if(preview != null)
                removeView(preview.getView());
            viewIsAttached = false;
        } catch (IOException e) {
            Log.e(TAG, "Error in OnPause - Could not stop camera", e);
        }
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        onPause();
    }

    public PreviewType getViewType(){
        if (preview != null)
            return preview.getPreviewType();
        return PreviewType.NONE;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
