package com.things.android.vuki.knockknock.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;

import static android.content.Context.CAMERA_SERVICE;

/**
 * Created by mvukosav
 */

public class Camera implements CameraActions {

    private static final String TAG = Camera.class.getSimpleName();

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;
    private static final int MAX_IMAGES = 1;

    //The CameraDevice class is a representation of a single camera connected to an Android device
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    //The ImageReader class allows direct application access to image data rendered into a {@link android.view.Surface}
    private ImageReader mImageReader;

    private Camera() {
    }

    private static class InstanceHolder {
        private static Camera camera = new Camera();
    }

    public static Camera getInstance() {
        return InstanceHolder.camera;
    }

    /**
     * Initialize the camera device
     */
    @Override
    public void initializeCamera( Context context,
                                  Handler backgroundHandler,
                                  ImageReader.OnImageAvailableListener imageAvailableListener ) {
        // Discover the camera instance
        CameraManager manager = (CameraManager) context.getSystemService( CAMERA_SERVICE );
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch ( CameraAccessException e ) {
            Log.d( TAG, "Cam access exception getting IDs", e );
        }
        if ( camIds.length < 1 ) {
            Log.d( TAG, "No cameras found" );
            return;
        }
        String id = camIds[0];
        Log.d( TAG, "Using camera id " + id );

        // Initialize the image processor
        mImageReader = ImageReader.newInstance( IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMAGES );
        mImageReader.setOnImageAvailableListener( imageAvailableListener, backgroundHandler );

        // Open the camera resource
        try {
            manager.openCamera( id, mStateCallback, backgroundHandler );
        } catch ( CameraAccessException cae ) {
            Log.d( TAG, "Camera access exception", cae );
        }
    }

    /**
     * Callback handling device state changes
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened( @NonNull CameraDevice cameraDevice ) {
            Log.d( TAG, "Opened camera." );
            Camera.this.cameraDevice = cameraDevice;
        }

        @Override
        public void onDisconnected( @NonNull CameraDevice cameraDevice ) {
            Log.d( TAG, "Camera disconnected, closing." );
            cameraDevice.close();
        }

        @Override
        public void onError( @NonNull CameraDevice cameraDevice, int i ) {
            Log.d( TAG, "Camera device error, closing." );
            cameraDevice.close();
        }

        @Override
        public void onClosed( @NonNull CameraDevice cameraDevice ) {
            Log.d( TAG, "Closed camera, releasing" );
            Camera.this.cameraDevice = null;
        }
    };

    /**
     * Begin a still image capture
     */
    @Override
    public void takePicture() {
        if ( cameraDevice == null ) {
            Log.w( TAG, "Cannot capture image. Camera not initialized." );
            return;
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            cameraDevice.createCaptureSession(
                    Collections.singletonList( mImageReader.getSurface() ),
                    mSessionCallback,
                    null );
        } catch ( CameraAccessException cae ) {
            Log.d( TAG, "access exception while preparing pic", cae );
        }
    }

    /**
     * Callback handling session state changes
     */
    private CameraCaptureSession.StateCallback mSessionCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured( @NonNull CameraCaptureSession cameraCaptureSession ) {
                    // The camera is already closed
                    if ( cameraDevice == null ) {
                        return;
                    }
                    // When the session is ready, we start capture.
                    captureSession = cameraCaptureSession;
                    triggerImageCapture();
                }

                @Override
                public void onConfigureFailed( @NonNull CameraCaptureSession cameraCaptureSession ) {
                    Log.w( TAG, "Failed to configure camera" );
                }
            };

    /**
     * Execute a new capture request within the active session
     */
    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest( CameraDevice.TEMPLATE_STILL_CAPTURE );
            captureBuilder.addTarget( mImageReader.getSurface() );
            captureBuilder.set( CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON );
            Log.d( TAG, "Session initialized." );
            captureSession.capture( captureBuilder.build(), mCaptureCallback, null );
        } catch ( CameraAccessException cae ) {
            Log.d( TAG, "camera capture exception" );
        }
    }

    /**
     * Callback handling capture session events
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed( @NonNull CameraCaptureSession session,
                                                 @NonNull CaptureRequest request,
                                                 @NonNull CaptureResult partialResult ) {
                    Log.d( TAG, "Partial result" );
                }

                @Override
                public void onCaptureCompleted( @NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull TotalCaptureResult result ) {
                    session.close();
                    captureSession = null;
                    Log.d( TAG, "CaptureSession closed" );
                }
            };

    @Override
    public void shutDown() {
        if ( cameraDevice != null ) {
            cameraDevice.close();
        }
    }

}
