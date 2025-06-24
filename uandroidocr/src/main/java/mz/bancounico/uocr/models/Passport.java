package mz.bancounico.uocr.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dds_unico on 8/8/18.
 */

public class Passport extends Document implements Serializable {


    private String identityCardNumber;


    public String getIdentityCardNumber() {
        return identityCardNumber;
    }

    public void setIdentityCardNumber(String identityCardNumber) {
        this.identityCardNumber = identityCardNumber;
    }


}
