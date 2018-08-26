package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.objectdetection.exception;

public class UnmalformedInputImageException extends RuntimeException {
    public UnmalformedInputImageException(){
        super("Input image must be 300 * 300 size.");
    }
}
