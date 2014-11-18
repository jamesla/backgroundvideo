package io.iclue.backgroundvideo;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.View;

import java.io.IOException;

interface Preview {

    PreviewType getPreviewType();

    void setOpacity(float opacity);

    void startRecordingWhenAvailable(boolean startOnCreate);

    void attach(Camera camera) throws IOException;

    void attach(MediaRecorder recorder);

    View getView();
}

