package mz.bancounico.uocr.utils.async;

/**
 * Created by dds_unico on 2/22/18.
 */

public class TaskError {
    private String errorMesage;
    private Exception exception;
    private ResponseError mResponseError;
    private int code;

    public static int RESOURCE_NOT_FOUND=404;


    public TaskError() {
    }

    public TaskError(ResponseError responseError) {
        this.mResponseError = responseError;
    }

    public TaskError(String errorMesage) {
        this.errorMesage = errorMesage;
    }

    public TaskError(Exception exception) {
        this.exception = exception;
    }

    public ResponseError getmResponseError() {
        return mResponseError;
    }

    public void setmResponseError(ResponseError mResponseError) {
        this.mResponseError = mResponseError;
    }

    public String getErrorMesage() {
        return errorMesage;
    }

    public void setErrorMesage(String errorMesage) {
        this.errorMesage = errorMesage;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}