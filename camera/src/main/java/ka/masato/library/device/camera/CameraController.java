package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.camera.exception.FailedCaptureImageException;
import objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.camera.exception.NoCameraFoundException;
import objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.camera.exception.UnInitializeCameraException;

import java.util.Collections;

public class CameraController {

    private static final String LOG_TAG = "CameraController";
    private static final int MAX_IMAGES = 1;

    private boolean initialized = false;

    private ImageReader mImageReader;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;


    private CameraController(){}

    private static class InstanceHolder{
        public static CameraController mInstance = new CameraController();
    }

    public static CameraController getInstance(){
        return InstanceHolder.mInstance;
    }

    public void initializeCameraDevice(Context context, int imageWidth, int imageHeight,
                                       Handler backgroundHandler, ImageReader.OnImageAvailableListener listener)
            throws NoCameraFoundException, CameraAccessException {
        if (initialized) {
            throw new IllegalStateException("CameraController Already initialized or is initializing.");
        }

        initialized = true;

        CameraManager mManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds = null;
        try {
            cameraIds = mManager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "Can not get camera device list.", e);
        }
        if (cameraIds==null || cameraIds.length <= 0) {
            initialized = false;
            throw new NoCameraFoundException();
        }

        Log.d(LOG_TAG, "Using camera Ids:" + cameraIds[0]);

        mImageReader = ImageReader.newInstance(imageWidth,imageHeight, ImageFormat.JPEG, MAX_IMAGES);
        mImageReader.setOnImageAvailableListener(listener, backgroundHandler);

        try{
            mManager.openCamera(cameraIds[0], mStateCallback, backgroundHandler);
        } catch(CameraAccessException e){
            initialized = false;
            throw e;
        }

    }

    public void takePicture() throws FailedCaptureImageException {
        if (mCameraDevice == null) {
            Log.w(LOG_TAG, "Failed take a image from camera.");
            return;
        }
        if (!initialized) {
            throw new UnInitializeCameraException();
        }
        try {
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionCallback,
                    null
            );
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "Failed capture image.", e);
            throw new FailedCaptureImageException();
        }

    }


    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed( CameraCaptureSession session,  CaptureRequest request,
                                         CaptureResult partialResult) {

        }

        @Override
        public void onCaptureCompleted( CameraCaptureSession session, CaptureRequest request,
                                        TotalCaptureResult result) {
            session.close();
            mCaptureSession = null;
            Log.d(LOG_TAG, "Capture session closed");
        }

    };


    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            if (mCameraDevice == null) {
                return;
            }
            mCaptureSession = cameraCaptureSession;
            triggerImageCapture();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.w(LOG_TAG, "Failed to configure camera");
        }
    };

    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "Failed create capture request.");
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d(LOG_TAG, "Open Camera");
            mCameraDevice = cameraDevice;
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice=null;

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }


    };


}
