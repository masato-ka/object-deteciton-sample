package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.objectdetection.exception;

public class FailedInitializeDetectorException extends Exception {

    public FailedInitializeDetectorException(){
        super("Failed load model or label file.");
    }

}
