package com.things.android.vuki.knockknock.camera;

import android.content.Context;
import android.media.ImageReader;
import android.os.Handler;

/**
 * Created by mvukosav
 */
public interface CameraActions {

    void initializeCamera( Context context, Handler backgroundHandler, ImageReader.OnImageAvailableListener onImageAvailableListener );

    void takePicture();

    void shutDown();
}
