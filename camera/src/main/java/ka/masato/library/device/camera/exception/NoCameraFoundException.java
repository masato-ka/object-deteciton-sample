package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.camera.exception;

public class NoCameraFoundException extends Exception {

    public NoCameraFoundException(){
        super("No camera found on your device.");
    }
}
