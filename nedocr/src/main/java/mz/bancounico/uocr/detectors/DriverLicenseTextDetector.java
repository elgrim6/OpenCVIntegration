package mz.bancounico.uocr.detectors;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mz.bancounico.uocr.models.DriverLicense;
import mz.bancounico.uocr.models.IdentityCard;
import mz.bancounico.uocr.models.PositionLine;
import mz.bancounico.uocr.utils.Line;

/**
 * Created by dds_unico on 11/9/18.
 */

public class DriverLicenseTextDetector extends TextDetector {

    private DriverLicense driverLicense;

    private final String BASE_INTEGRETY_REGEX = "NOME|NAME|BRITH|EMISS|LOCAL|SEX|GENDER|B.I.N|REPUBLICA|DATA|ADDRESS|DATE|:|/|CARTA|DRIVING|LICENCE|ESTADO|BIRTH|CODIGO|I.D|RESTRI|VEHICLE|VALIDADE|CATEGO";
    private final String NAME_INTEGRETY_REGEX = "CARTA|MASCU|FEMEN|GARTA|CONDU|LICENCE|DRIVING|DRIVNG|SADC|SAOC|SAIC|MOC|PDP|\\d";
    private final String GENDER_INTEGRETY_REGEX = "^M|^F";
    private final String DATE_INTEGRETY_REGEX = "\\s*(3[01]|[12][0-9]|0[1-9])\\/(1[012]|0[1-9])\\/((?:19|20)\\d{2})\\s*";

    // Reading properties
    public final static String DRIVER_LICENCE_CARD_ID = "DRIVER_LICENCE_ID";
    public final static String NAME_PROPERTY = "NAME";
    public final static String BIRTH_DATE_PROPERTY = "BIRTH_DATE";
    public final static String ISSUANCE_EXPIRY_DATE_PROPERTY = "ISSUANCE_EXPIRY_DATE_PROPERTY";



    public DriverLicenseTextDetector(Context context)  {
        super(context);
        this.driverLicense = new DriverLicense();
        setParams();
    }



    @Override
    public Object getData() {

        if (getNotFriendlyDetectedProperties().get(NAME_PROPERTY) != null) {
            driverLicense.setName(getName());
        }
        if (getNotFriendlyDetectedProperties().get(DRIVER_LICENCE_CARD_ID) != null) {
            driverLicense.setNumber(getDriverLicenceCardId());
        }
        if (getNotFriendlyDetectedProperties().get(BIRTH_DATE_PROPERTY) != null) {
            driverLicense.setBirthDate(getBirthDate());
        }
        if (getNotDetectedProperties().get(ISSUANCE_EXPIRY_DATE_PROPERTY) != null) {
           setIssuanceExpiryDate();
        }

        clearLines();
        return driverLicense;
    }

    public void setIssuanceExpiryDate(){
        int position = find("VALIDADE");

        if (position == -1) {
            return;
        }

        String issuance = "";
        String expiry = "";
        Pattern pattern = Pattern.compile(DATE_INTEGRETY_REGEX);
        Matcher matcher = pattern.matcher(lines.get(position).getValue());
        if (matcher.find()) {
            issuance = matcher.group(1) + "/" + matcher.group(2) + "/" + matcher.group(3);
        }
        else
            return ;

        if(matcher.find()){
            expiry = matcher.group(1) + "/" + matcher.group(2) + "/" + matcher.group(3);
        }
        else
            return;

        getNotDetectedProperties().remove(ISSUANCE_EXPIRY_DATE_PROPERTY);

        driverLicense.setIssuanceDate(issuance);
        driverLicense.setExpiryDate(expiry);

    }

    public String getBirthDate() {
        int position = find("NASCIMENTO");

        if (position == -1) {
            return "";
        }
        String date = "";
        Pattern pattern = Pattern.compile(DATE_INTEGRETY_REGEX);
        Matcher matcher = pattern.matcher(lines.get(position).getValue());
        if (matcher.find())
            date = matcher.group(1)+"/"+matcher.group(2)+"/"+matcher.group(3);
        else
            return "";

        getNotFriendlyDetectedProperties().remove(BIRTH_DATE_PROPERTY);
        return date;
    }

    public String getName() {

        int position = findNonUpperCase("\\b(?!"+NAME_INTEGRETY_REGEX+")[A-Z]{4}");
        String name = "";

        if (position == -1) {
            return "";

        }

        try {
            name = lines.get(position).getValue().toUpperCase();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        name = name.replaceAll("[(?!\\d|\\p{Punct})a-z]", "");
        name = name.replaceAll("NOME|NAME","");
        name = name.replaceAll("^\\s*","");

        if (find(name, NAME_INTEGRETY_REGEX)|| name.length() < 11)
            return "";

        getNotFriendlyDetectedProperties().remove(NAME_PROPERTY);
        return name;
    }

    public String getDriverLicenceCardId() {

        int position = find("(\\d{8})(\\/)(\\d{1})");

        if (position == -1) {
            return "";
        }

        getNotFriendlyDetectedProperties().remove(DRIVER_LICENCE_CARD_ID);
        return lines.get(position).getValue().replaceAll("^[^0-9]+", "").replaceAll("\\s", "");
    }


    @Override
    public void setParams() {
        getNotFriendlyDetectedProperties().put(DRIVER_LICENCE_CARD_ID, "Numero da carta de conduçāo");
        getNotFriendlyDetectedProperties().put(NAME_PROPERTY, "Nome");
        getNotFriendlyDetectedProperties().put(BIRTH_DATE_PROPERTY, "Data de Nascimento");
        getNotDetectedProperties().put(ISSUANCE_EXPIRY_DATE_PROPERTY, "Data de Validade");
        /*getNotDetectedProperties().put(ISSUANCE_EXPIRY_DATE_PROPERTY, "Data de emissāo e validade");
        getNotDetectedProperties().put(BIRTH_DATE_PROPERTY, "Data de Nascimento");
        getNotDetectedProperties().put(GENDER, "Genero");*/
    }
}
