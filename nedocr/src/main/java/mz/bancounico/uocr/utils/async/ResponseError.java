package mz.bancounico.uocr.utils.async;

/**
 * Created by Barros on 5/9/2018.
 */

public class ResponseError {


    private String ErrorCode;
    private String Message;
    private boolean VisibleToHuman;

    public ResponseError(){
    }
    public ResponseError(String errorCode, String message, boolean visibleToHuman) {
        ErrorCode = errorCode;
        Message = message;
        VisibleToHuman = visibleToHuman;
    }

    public String getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(String errorCode) {
        this.ErrorCode = errorCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }

    public boolean isVisibleToHuman() {
        return VisibleToHuman;
    }

    public void setVisibleToHuman(boolean visibleToHuman) {
        this.VisibleToHuman = visibleToHuman;
    }
}
