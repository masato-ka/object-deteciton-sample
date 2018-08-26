package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.objectdetection;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.objectdetection.exception.UninitializedBufferException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TensorFlowHelper {

    private static final String LOG_TAG = "TensorFlowHelper";

    public static MappedByteBuffer loadModelFile(Context context, String filePath) throws IOException {

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getAssets().openFd(filePath);
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed load model file.", e);
            throw e;
        }
    }

    public static List<String> loadLabelFile(Context context, String labelFilePath) throws IOException {
        AssetManager assetManager = context.getAssets();
        ArrayList<String> result = new ArrayList<>();
        try(InputStream is = assetManager.open(labelFilePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while((line = bufferedReader.readLine()) != null){
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed load label file.", e);
            throw e;
        }
    }

    public static void convertBitmapToByteBuffer(Bitmap bitmap, int[] intValues, ByteBuffer imgData) {
        if (imgData ==null) {
            Log.e(LOG_TAG, "imgData as result buffer must be initialized before.");
            throw new UninitializedBufferException();
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        for (int i = 0; i < bitmap.getWidth(); i++){
            for (int j = 0; j < bitmap.getHeight(); j++ ){
                final int val = intValues[pixel++];
                imgData.put((byte)((val >> 16) & 0xFF));
                imgData.put((byte)((val >> 8) & 0xFF));
                imgData.put((byte)(val &0xFF));
            }
        }
    }
}
