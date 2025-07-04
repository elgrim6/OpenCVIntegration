/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mz.bancounico.uocr.processors;

import android.util.SparseArray;


import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import mz.bancounico.uocr.detectors.TextDetector;
import mz.bancounico.uocr.graphics.OcrGraphic;
import mz.bancounico.uocr.logic.GraphicOverlay;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class TextProcessor extends Processor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private TextDetector textDetector;
    private SparseArray<TextBlock> items;

    public TextProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, TextDetector textDetector) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.textDetector = textDetector;
        items = new SparseArray<>();

    }

    public TextProcessor(TextDetector textDetector) {
        this.textDetector = textDetector;
        items = new SparseArray<>();

    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
//        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        textDetector.setLines(items);

        /*
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);

        }
        */
    }


    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        //mGraphicOverlay.clear();
    }


    @Override
    public mz.bancounico.uocr.detectors.Detector getDetector() {
        return textDetector;
    }
}
