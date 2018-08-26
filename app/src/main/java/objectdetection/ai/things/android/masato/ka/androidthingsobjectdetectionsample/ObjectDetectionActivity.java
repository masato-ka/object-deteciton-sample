package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import ka.masato.library.ai.ssddetection.MultiObjectDetector;
import ka.masato.library.ai.ssddetection.exception.FailedInitializeDetectorException;
import ka.masato.library.ai.ssddetection.exception.UnInitializeDetectorException;
import ka.masato.library.ai.ssddetection.model.Recognition;
import ka.masato.library.device.camera.CameraController;
import ka.masato.library.device.camera.ImagePreprocessor;
import ka.masato.library.device.camera.exception.FailedCaptureImageException;
import ka.masato.library.device.camera.exception.NoCameraFoundException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * TensorFlow Lite with coco-mobilenet-ssd model SAMPLE.
 *
 */
public class ObjectDetectionActivity extends Activity {

    private static final String LOG_TAG = "ObjectDetectionActivity";
    private static final String CAMERA_THREAD_NAME = "cameraBackground";
    private static final int CAMERA_WIDTH = 640;
    private static final int CAMERA_HEIGHT = 480;
    private static final int TF_IMAGE_WIDTH = 300;
    private static final int TF_IMAGE_HEIGHT = 300;
    private static final String MODEL_FILE_PATH = "detect.tflite";
    private static final String LABEL_FILE_PATH = "labelmap.txt";

    private static final String SHUTTER_BUTTON = "BCM21";

    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraController mCameraController;

    private ImagePreprocessor mImagePreprocessor;

    private MultiObjectDetector mMultiObjectDetector;

    private CanvasView mCanvasView;

    private Gpio mButton;

    private Handler mLoopHandler;
    private boolean isCapture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);


        initializeCameraDevice();

        mImagePreprocessor = new ImagePreprocessor(CAMERA_WIDTH, CAMERA_HEIGHT, TF_IMAGE_WIDTH, TF_IMAGE_HEIGHT);

        initializeObjectDetector();

        mCanvasView = findViewById(R.id.CanvasView);

        ButtonInitialize();

        mLoopHandler = new Handler();
    }

    private void ButtonInitialize(){
        PeripheralManager mManager = PeripheralManager.getInstance();
        try {
            mButton = mManager.openGpio(SHUTTER_BUTTON);
            mButton.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButton.registerGpioCallback(shutterCallBack);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed opne GPIO:" + SHUTTER_BUTTON , e);
        }


    }

    private void initializeObjectDetector() {
        mMultiObjectDetector = MultiObjectDetector.getInstance();
        try {
            mMultiObjectDetector.initialize(this, MODEL_FILE_PATH, LABEL_FILE_PATH);
        } catch (FailedInitializeDetectorException e) {
            Log.e(LOG_TAG, "FailedInitializeDetectorException", e);
            //TODO Must be Abort.
        }
    }

    private void initializeCameraDevice() {
        mCameraThread = new HandlerThread(CAMERA_THREAD_NAME);
        mCameraThread.start();
        //mCameraHandler = new Handler(mCameraThread.getLooper());
        mCameraHandler = new Handler();
        mCameraController = CameraController.getInstance();
        try {
            mCameraController.initializeCameraDevice(this, CAMERA_WIDTH, CAMERA_HEIGHT,
                    mCameraHandler, mOnImageAvailableListener);
        } catch (NoCameraFoundException e) {
            Log.e(LOG_TAG, "Can not access to camera device.", e);
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "Can not access tp camera device.", e);
        }
    }

    /*Callback method of GPIO*/
    private GpioCallback shutterCallBack = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            if (!isCapture) {
                isCapture = true;
                mLoopHandler.post(timerThread);
            } else {
                isCapture = false;
                mLoopHandler.removeCallbacks(timerThread);
            }
            return true;
        }
    };

    /*Callback method of CAMERA result.*/
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            try {
                final Bitmap cropedImage = mImagePreprocessor.preprocessImage(imageReader.acquireLatestImage());
                ArrayList<Recognition> result;
                result = mMultiObjectDetector.runDetection(cropedImage, 10);
                mCanvasView.setmBitmap(cropedImage);
                mCanvasView.setRecognitions(result);
                mCanvasView.invalidate();
                Log.d(LOG_TAG, "result" + result.toString());
            } catch (UnInitializeDetectorException e) {
                Log.e(LOG_TAG, "Failed object detection.", e);
            }
        }
    };

    private final Runnable timerThread = new Runnable() {
        @Override
        public void run() {
            try {
                mCameraController.takePicture();
            } catch (FailedCaptureImageException e) {
                Log.e(LOG_TAG, "Failed capture camera image.", e);
            }
            mLoopHandler.postDelayed(this, 1000);
        }
    };

}
