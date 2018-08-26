package ka.masato.library.device.camera.exception;

public class NoCameraFoundException extends Exception {

    public NoCameraFoundException(){
        super("No camera found on your device.");
    }
}
