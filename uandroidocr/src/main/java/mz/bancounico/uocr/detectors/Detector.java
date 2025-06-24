package mz.bancounico.uocr.detectors;

import java.util.HashMap;

/**
 * Created by dds_unico on 4/24/18.
 */

public abstract class Detector {

    public abstract Object getData();

    public abstract boolean hasDetect();

    public abstract HashMap<String, String> getNotDetectedProperties();

    public abstract HashMap<String, String> getNotFriendlyDetectedProperties();
}
