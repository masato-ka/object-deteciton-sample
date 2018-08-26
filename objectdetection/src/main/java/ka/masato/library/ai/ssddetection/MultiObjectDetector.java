package ka.masato.library.ai.ssddetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import ka.masato.library.ai.ssddetection.exception.FailedInitializeDetectorException;
import ka.masato.library.ai.ssddetection.exception.UnInitializeDetectorException;
import ka.masato.library.ai.ssddetection.exception.UnmalformedInputImageException;
import ka.masato.library.ai.ssddetection.model.Recognition;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiObjectDetector {

    private static final String LOG_TAG = "MultiObjectDetector";

    private static final int TF_INPUT_IMAGE_WIDTH = 300;
    private static final int TF_INPUT_IMAGE_HEIGHT = 300;
    private static final int LABEL_OFFSET = 1;
    private static final int DIM_PIXELE_SIZE = 3;
    private static final int DIM_BATCH_SIZE = 1;

    private Interpreter mTensorFlowLite;
    private List<String> label;

    private float[][][] outputLocations;
    private float[][] outputClasses;
    private float[][] outputScores;
    private float[] numDetections;

    private MultiObjectDetector(){
    }

    private static class HasInstance {
        private static MultiObjectDetector mInstance = new MultiObjectDetector();
    }

    public static MultiObjectDetector getInstance(){
        return HasInstance.mInstance;
    }

    public void initialize(Context context, String modelFilePath, String labelFilePath)
            throws FailedInitializeDetectorException {

        try {
            loadModel(context, modelFilePath);
            loadLabel(context, labelFilePath);
        } catch (IOException e) {
            throw new FailedInitializeDetectorException();
        }

    }

    private void loadModel(Context context, String modelFilePath) throws IOException {
            mTensorFlowLite = new Interpreter(TensorFlowHelper.loadModelFile(context, modelFilePath));
    }

    private void loadLabel(Context context, String labelFilePath) throws IOException {
        label = TensorFlowHelper.loadLabelFile(context, labelFilePath);
    }

    public ArrayList<Recognition> runDetection(Bitmap bitmap, int numDetection)
            throws UnInitializeDetectorException {

        if(mTensorFlowLite ==null) {
            throw new UnInitializeDetectorException();
        }

        if (bitmap.getWidth() != TF_INPUT_IMAGE_WIDTH
                && bitmap.getHeight() != TF_INPUT_IMAGE_HEIGHT) {
            throw new UnmalformedInputImageException();
        }

        ByteBuffer imgData = convertBitmapToByteBuffer(bitmap);
        Map<Integer,Object> outputData = prepareOutputModle(numDetection);
        Object[] inputArray = {imgData};
        mTensorFlowLite.runForMultipleInputsOutputs(inputArray, outputData);
        ArrayList<Recognition> result = formatResult(outputData, numDetection);
        return result;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {

        int[] intValues = new int[TF_INPUT_IMAGE_WIDTH * TF_INPUT_IMAGE_HEIGHT];
        ByteBuffer imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * TF_INPUT_IMAGE_WIDTH
                * TF_INPUT_IMAGE_HEIGHT * DIM_PIXELE_SIZE);
        imgData.order(ByteOrder.LITTLE_ENDIAN);
        TensorFlowHelper.convertBitmapToByteBuffer(bitmap, intValues, imgData);
        return imgData;
    }

    private Map<Integer, Object> prepareOutputModle(int numDetection){

        Map<Integer,Object> outputData = new HashMap<>();
        outputLocations = new float[1][numDetection][4];
        outputClasses = new float[1][numDetection];
        outputScores = new float[1][numDetection];
        numDetections = new float[1];
        outputData.put(0, outputLocations);
        outputData.put(1, outputClasses);
        outputData.put(2, outputScores);
        outputData.put(3, numDetections);

        return outputData;
    }

    private ArrayList<Recognition> formatResult(Map<Integer, Object> outputData, int numDetection){


        final ArrayList<Recognition> recognitions = new ArrayList<>(numDetection);
        for(int i=0; i < outputData.size(); i++){
            final RectF location = new RectF(
                    outputLocations[0][i][1] * TF_INPUT_IMAGE_WIDTH,
                    outputLocations[0][i][0] * TF_INPUT_IMAGE_HEIGHT,
                    outputLocations[0][i][3] * TF_INPUT_IMAGE_WIDTH,
                    outputLocations[0][i][2] * TF_INPUT_IMAGE_HEIGHT
            );
            Recognition recognition =
                    new Recognition(
                            "" + i,
                            label.get((int) outputClasses[0][i] + LABEL_OFFSET),
                            outputScores[0][i],
                            location);
            recognitions.add(recognition);
        }

        return recognitions;
    }



}
