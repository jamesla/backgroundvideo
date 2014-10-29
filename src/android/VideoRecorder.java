package io.iclue.backgroundvideo;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoRecorder {
    private static final String TAG = "BACKGROUND_VIDEO";

    private Camera myCamera;
    private MediaRecorder myMediaRecorder;

    protected boolean isInitialized = false;
    protected boolean isRecording = false;

    private int myCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT; 
    private int camera_facing = 0;

    public void SetCameraFacing(String facing){
        camera_facing = ( facing.equalsIgnoreCase("FRONT") ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK );
    }

    public boolean Initialize (TextureView previewSurface, String path) throws Exception {        
        if (isRecording) {
            Log.d(TAG, "ALREADY RECORDING");
            return true;
        }

        if (isInitialized) {
            Log.d(TAG, "ALREADY INITIALIZED");
            return true;
        }

        try {
            myCameraId = CameraHelper.getCameraId(camera_facing);
            myCamera = CameraHelper.getDefaultCamera(camera_facing);

            if (myCamera == null) {
                throw new Exception("Could not open camera, camera is null");
            }
        } catch (Exception e) {
            ReleaseCamera();
            throw new Exception(TAG + " : CANNOT OPEN CAMERA : " + e.getMessage(), e);
        }

        Camera.Parameters cp = myCamera.getParameters();
        Camera.Size optimalSize = CameraHelper.getLowestResolution(cp);
        CamcorderProfile profile = CamcorderProfile.get(myCameraId, CamcorderProfile.QUALITY_LOW);

        //This ensures that the resolution with be acceptable to the camera.
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        Camera.Size previewSize = CameraHelper.getPreviewSize(cp);
        cp.setPreviewSize(previewSize.width, previewSize.height);
      
        //Not sure on this one, seems to give odd results on some phones
        //cp.setRecordingHint(true); 
        
        cp.setRotation(90);
        cp.set("cam_mode", 1);

        myCamera.setDisplayOrientation(90);
        myCamera.setParameters(cp);

        //Setup preview
        try {
            myCamera.setPreviewTexture(previewSurface.getSurfaceTexture());
        } catch (IOException e) {
            ReleaseCamera();
            throw new Exception(TAG + " : SURFACE UNAVAILABLE : " + e.getMessage(), e);
        }

        myMediaRecorder = new MediaRecorder();
        myMediaRecorder.setOrientationHint(90);

        myCamera.unlock();
        myMediaRecorder.setCamera(myCamera);

        myMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        myMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        myMediaRecorder.setProfile(profile);
        myMediaRecorder.setOutputFile(path);

        try {
            myMediaRecorder.prepare();
        } catch (Exception e) {
            ReleaseCamera();
            throw new Exception(TAG + " : ERROR PREPARING MEDIA RECORDER : " + e.getMessage(), e);
        }

        isInitialized = true;
        return true;
    }

    public boolean Start () {
        if (myMediaRecorder != null && isInitialized) {
            myMediaRecorder.start();
            isRecording = true;
            return true;
        }
        return false;
    }

    public void Stop () {
        if (myMediaRecorder != null && isRecording){
            myMediaRecorder.stop();
            isRecording = false;
        }
        releaseMediaRecorder();
        ReleaseCamera();
    }

    private void releaseMediaRecorder () {
        if (myMediaRecorder != null) {
            myMediaRecorder.reset();
            myMediaRecorder.release();
            myMediaRecorder = null;
            isRecording = isInitialized = false;
        }

        if (myCamera != null) {
            myCamera.lock();
        }
    }
    
    private void ReleaseCamera () {
        if (myCamera != null) {
            myCamera.release();
            myCamera = null;
        }
    }
}
