package pl.psnc.dl.wf4ever.darceo.client;

public class DArceoException extends Exception {

    /** id. */
    private static final long serialVersionUID = -1452658706529053879L;


    public DArceoException(String message, Exception cause) {
        super(message, cause);
    }


    public DArceoException(String message) {
        super(message);
    }

}
