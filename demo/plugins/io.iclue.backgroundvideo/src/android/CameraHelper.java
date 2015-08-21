package io.iclue.backgroundvideo;

import android.hardware.Camera;
import android.media.CamcorderProfile;

import java.util.List;

class CameraHelper {
    public static Camera.Size getPreviewSize(Camera.Parameters cp) {
        Camera.Size size = cp.getPreferredPreviewSizeForVideo();
        
        if (size == null)
            size = cp.getSupportedPreviewSizes().get(0);
        
        return size;
    }
    
    public static Camera.Size getLowestResolution (Camera.Parameters cp) {
        List<Camera.Size> sl = cp.getSupportedVideoSizes();
        
        if (sl == null)
            sl = cp.getSupportedPictureSizes();

        Camera.Size small = sl.get(0);

        for(Camera.Size s : sl) {
            if ((s.height * s.width) < (small.height * small.width))
                small = s;
        }

        return small;
    }

    public static boolean sizeSupported (Camera.Parameters cp, CamcorderProfile profile) {
        List<Camera.Size> sl = cp.getSupportedVideoSizes();

        if (sl == null)
            sl = cp.getSupportedPictureSizes();

        for(Camera.Size s : sl) {
            if (profile.videoFrameWidth == s.width && profile.videoFrameHeight == s.height)
                return true;
        }

        return false;
    }

    public static Camera getDefaultCamera(int position) {
        // Find the total number of cameras available
        int  mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);
            }
        }

        return null;
    }

    public static int getCameraId(int position) {
        // Find the total number of cameras available
        int  mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position)
                return i;
        }

        return 0;
    }
}
