package ka.masato.library.device.camera.exception;

public class UnInitializeCameraException extends RuntimeException {

    public UnInitializeCameraException(){
        super("Before take picture you  should be do initialize() method.");
    }
}
