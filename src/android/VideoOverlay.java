package io.iclue.backgroundvideo;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class VideoOverlay extends ViewGroup implements TextureView.SurfaceTextureListener {
    private static final String TAG = "BACKGROUND_VID_OVERLAY";
    private RecordingState mRecordingState = RecordingState.INITIALIZING;

    private int mCameraId = CameraHelper.NO_CAMERA;
    private Camera mCamera = null;
    private final TextureView mPreview;
    private boolean mPreviewAttached = false;
    private MediaRecorder mRecorder = null;
    private boolean mStartWhenInitialized = false;

    private String mFilePath;
    private boolean mRecordAudio = true;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int mOrientation;

    public VideoOverlay(Context context) {
        super(context);

        this.setClickable(false);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Create surface to display the camera preview
        mPreview = new TextureView(context);
        mPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mPreview.setClickable(false);
        mPreview.setSurfaceTextureListener(this);
        attachView();
    }

    public void setCameraFacing(String cameraFace) {
        mCameraFacing = (cameraFace.equalsIgnoreCase("FRONT") ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public void setRecordAudio(boolean recordAudio) {
        mRecordAudio = recordAudio;
    }

    public void Start(String filePath) throws Exception {
        if (this.mRecordingState == RecordingState.STARTED) {
            Log.w(TAG, "Already Recording");
            return;
        }

        if (!TextUtils.isEmpty(filePath)) {
            this.mFilePath = filePath;
        }

        attachView();

        if (this.mRecordingState == RecordingState.INITIALIZING) {
            this.mStartWhenInitialized = true;
            return;
        }

        if (TextUtils.isEmpty(mFilePath)) {
            throw new IllegalArgumentException("Filename for recording must be set");
        }

        initializeCamera();

        if (mCamera == null) {
            this.detachView();
            throw new NullPointerException("Cannot start recording, we don't have a camera!");
        }

        // Set camera parameters
        Camera.Parameters cameraParameters = mCamera.getParameters();
        mCamera.stopPreview(); //Apparently helps with freezing issue on some Samsung devices.
        mCamera.unlock();

        try {
            mRecorder = new MediaRecorder();
            mRecorder.setCamera(mCamera);

            CamcorderProfile profile;
            if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_LOW)) {
                profile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                profile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);
            }

            Camera.Size lowestRes = CameraHelper.getLowestResolution(cameraParameters);
            profile.videoFrameWidth = lowestRes.width;
            profile.videoFrameHeight = lowestRes.height;

            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            if (mRecordAudio) {
                // With audio
                mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorder.setVideoFrameRate(profile.videoFrameRate);
                mRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                mRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                mRecorder.setAudioEncodingBitRate(profile.audioBitRate);
                mRecorder.setAudioChannels(profile.audioChannels);
                mRecorder.setAudioSamplingRate(profile.audioSampleRate);
                mRecorder.setVideoEncoder(profile.videoCodec);
                mRecorder.setAudioEncoder(profile.audioCodec);
            } else {
                // Without audio
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorder.setVideoFrameRate(profile.videoFrameRate);
                mRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                mRecorder.setVideoEncodingBitRate(profile.videoBitRate);
                mRecorder.setVideoEncoder(profile.videoCodec);
            }

            mRecorder.setOutputFile(filePath);
            mRecorder.setOrientationHint(mOrientation);
            mRecorder.prepare();
            Log.d(TAG, "Starting recording");
            mRecorder.start();
        } catch (Exception e) {
            this.releaseCamera();
            Log.e(TAG, "Could not start recording! MediaRecorder Error", e);
            throw e;
        }
    }

    public String Stop() throws IOException {
        Log.d(TAG, "stopRecording called");

        if (mRecorder != null) {
            MediaRecorder tempRecorder = mRecorder;
            mRecorder = null;
            try {
                tempRecorder.stop();
            } catch (Exception e) {
                //This can occur when the camera failed to start and then stop is called
                Log.e(TAG, "Could not stop recording.", e);
            }
        }

        this.releaseCamera();
        this.detachView();

        return this.mFilePath;
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

    private void initializeCamera() {
        if (mCamera == null) {
            try {
                mCameraId = CameraHelper.getCameraId(mCameraFacing);
                if (mCameraId != CameraHelper.NO_CAMERA) {
                    mCamera = Camera.open(mCameraId);

                    // Set camera parameters
                    mOrientation = CameraHelper.calculateOrientation((Activity) this.getContext(), mCameraId);
                    Camera.Parameters cameraParameters = mCamera.getParameters();
                    Camera.Size previewSize = CameraHelper.getPreviewSize(cameraParameters);
                    cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
                    cameraParameters.setRotation(mOrientation);
                    cameraParameters.setRecordingHint(true);

                    mCamera.setParameters(cameraParameters);
                    mCamera.setDisplayOrientation(mOrientation);
                }
            } catch (RuntimeException ex) {
                this.releaseCamera();
                Log.e(TAG, "Unable to open camera. Another application probably has a lock", ex);
            }
        }
    }

    private void releaseCamera() {
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
            mCameraId = CameraHelper.NO_CAMERA;
        }
        this.mRecordingState = RecordingState.STOPPED;
    }

    private void attachView() {
        if (!mPreviewAttached && mPreview != null) {
            this.addView(mPreview);
            this.mPreviewAttached = true;
        }
    }

    private void detachView() {
        if (mPreviewAttached && mPreview != null) {
            this.removeView(mPreview);
            this.mPreviewAttached = false;
            this.mRecordingState = RecordingState.INITIALIZING;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "Creating Texture Created");

        this.mRecordingState = RecordingState.STOPPED;

        initializeCamera();

        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                Log.e(TAG, "Unable to attach preview to camera!", e);
            }
            mCamera.startPreview();
        }

        if (mStartWhenInitialized) {
            try {
                Start(this.mFilePath);
            } catch (Exception ex) {
                Log.e(TAG, "Error start camera", ex);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    private enum RecordingState {INITIALIZING, STARTED, STOPPED}
}
