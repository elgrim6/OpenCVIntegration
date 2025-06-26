package mz.bancounico.uocr.processors;

import mz.bancounico.uocr.detectors.Detector;

/**
 * Created by dds_unico on 4/24/18.
 */

public abstract class Processor {

    public abstract Detector getDetector();
}
