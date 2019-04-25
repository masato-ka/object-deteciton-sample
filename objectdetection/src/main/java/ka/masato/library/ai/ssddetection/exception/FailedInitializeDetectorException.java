/**
 * @param
 */
package ka.masato.library.ai.ssddetection.exception;

public class FailedInitializeDetectorException extends Exception {

    public FailedInitializeDetectorException(){
        super("Failed load model or label file.");
    }

}
