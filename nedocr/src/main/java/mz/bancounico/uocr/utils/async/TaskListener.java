package mz.bancounico.uocr.utils.async;

/**
 * Created by dds_unico on 1/15/18.
 */

public interface TaskListener<TSucess,TError> {

    void success(TSucess t);
    void error(TError error);
}
