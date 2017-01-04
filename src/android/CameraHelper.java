package io.iclue.backgroundvideo;

import android.app.Activity;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.Surface;

import java.util.List;

@SuppressWarnings("deprecation")
class CameraHelper {
    static final int NO_CAMERA = -101;

    static Camera.Size getPreviewSize(Camera.Parameters cp) {
        Camera.Size size = cp.getPreferredPreviewSizeForVideo();

        if (size == null)
            size = cp.getSupportedPreviewSizes().get(0);

        return size;
    }

    static Camera.Size getLowestResolution(Camera.Parameters cp) {
        List<Camera.Size> sl = cp.getSupportedVideoSizes();

        if (sl == null)
            sl = cp.getSupportedPictureSizes();

        Camera.Size small = sl.get(0);

        for (Camera.Size s : sl) {
            if ((s.height * s.width) < (small.height * small.width))
                small = s;
        }

        return small;
    }

    static int getCameraId(int position) {
        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position)
                return i;
        }

        return NO_CAMERA;
    }

    static int calculateOrientation(Activity activity, int cameraId) {
        if (cameraId == NO_CAMERA)
            return 0;

        DisplayMetrics dm = new DisplayMetrics();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int cameraRotationOffset = info.orientation;

        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int currentScreenRotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (currentScreenRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int orientation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            orientation = (cameraRotationOffset + degrees) % 360;
            orientation = (360 - orientation) % 360;
        } else {
            orientation = (cameraRotationOffset - degrees + 360) % 360;
        }

        return orientation;
    }
}
