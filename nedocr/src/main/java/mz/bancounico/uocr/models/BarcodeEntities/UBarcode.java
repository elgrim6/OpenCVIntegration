package mz.bancounico.uocr.models.BarcodeEntities;

public class UBarcode {

    private byte[] rawData;
    private String displayData;

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public String getDisplayData() {
        return displayData;
    }

    public void setDisplayData(String displayData) {
        this.displayData = displayData;
    }
}
