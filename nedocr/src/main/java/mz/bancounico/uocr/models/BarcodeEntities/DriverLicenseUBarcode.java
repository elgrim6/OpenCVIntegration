package mz.bancounico.uocr.models.BarcodeEntities;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;

public class DriverLicenseUBarcode extends DocumentUBarcode {

    public DriverLicenseUBarcode(byte[] rawData){
       this.setRawData(rawData);
       parseRawData();
    }

    private void parseRawData(){
        String parsedRawData=new String(getRawData(), StandardCharsets.ISO_8859_1);

        if(!TextUtils.isEmpty(parsedRawData)){
            String[] stringList=parsedRawData.split(",");
            String[] issuanceAndExpiryDates=stringList[7].replaceAll("\\s","").split("-");
            String documentNumberPart_2=stringList[6].split("/")[1];

            setName(stringList[2].replaceAll("\"",""));
            setDocumentNumber(stringList[5]+"/"+documentNumberPart_2);
            setGender(stringList[12]);
            setBirthDate(stringList[4]);
            setIssuanceDate(issuanceAndExpiryDates[0]);
            setExpiryDate(issuanceAndExpiryDates[1]);
        }
    }
}
