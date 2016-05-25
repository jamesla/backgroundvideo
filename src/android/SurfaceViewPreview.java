package io.iclue.backgroundvideo;


import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

public class SurfaceViewPreview implements Preview, SurfaceHolder.Callback {
    private static final String TAG = "BACKGROUND_VID_SURFACE";
    private final VideoOverlay overlay;
    private final SurfaceView surfaceView;
    private final SurfaceHolder surfaceHolder;
    private boolean startRecordingOnCreate = true;

    @SuppressWarnings("deprecation")
    public SurfaceViewPreview(VideoOverlay overlay) {
        Log.d(TAG, "Creating Surface Preview");
        this.overlay = overlay;
        surfaceView = new SurfaceView(overlay.getContext());
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Surface Created");
        overlay.previewAvailable();

        if(startRecordingOnCreate) {
            try {
                overlay.startRecording();
            } catch (Exception e) {
                Log.e(TAG, "Error start recording", e);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    @Override
    public void startRecordingWhenAvailable(boolean startOnCreate) { startRecordingOnCreate = startOnCreate; }

    @Override
    public void setOpacity(float opacity) { Log.i(TAG, "Cannot Set Opacity for SurfaceView"); }

    @Override
    public void attach(Camera camera) throws IOException {
        camera.setPreviewDisplay(surfaceHolder);
    }

    @Override
    public void attach(MediaRecorder recorder) {
        recorder.setPreviewDisplay(surfaceHolder.getSurface());
    }

    @Override
    public View getView() {
        return surfaceView;
    }

    @Override
    public PreviewType getPreviewType(){
        return PreviewType.SURFACE;
    }
}
