package mz.bancounico.uocr.detectors;

import android.content.Context;

import mz.bancounico.uocr.models.Passport;

/**
 * Created by dds_unico on 9/13/18.
 */

public class PassportTextDetector extends TextDetector {

    private Passport passport;

    private final String BASE_INTEGRETY_REGEX = "REPÚBLIC|REPUBLI|TIPO|CÓDIG|CODIGO|PASSAP|PASSP|APELIDO|NOME|NACIONA|DATADENASC|SEXO|DATA|VALIDO|LOCAL|ASSINATURA";
    private final String DATE_INTEGRETY_REGEX = "\\s*(3[01]|[12][0-9]|0[1-9])\\-(1[012]|0[1-9])\\-((?:19|20)\\d{2})\\s*$";
    private final String NAME_INTEGRETY_REGEX = BASE_INTEGRETY_REGEX + "|\\d";


    // Reading properties
    public final static String PASSPORT_ID = "PASSPORT_ID";
    public final static String IDENTITY_CARD_ID = "DIRE_CARD_ID";
    public final static String NAME_PROPERTY = "NAME";
    public final static String DATES_PROPERTIES = "DATES_PROPERTIES";
    public final static String GENRE = "GENRE";

    private ReadMode readMode;

    public static enum ReadMode {
        Front,
        BACK
    }



    public PassportTextDetector(Context context, ReadMode readMode) {
        super(context);
        this.readMode = readMode;
        this.passport = new Passport();
        setParams();

    }


    @Override
    public Object getData() {

        if (readMode == PassportTextDetector.ReadMode.Front) {
            if (getNotDetectedProperties().get(DATES_PROPERTIES) != null) {
                setDates();
            }
        }
        else {
            if (getNotDetectedProperties().get(PASSPORT_ID) != null) {
                passport.setNumber(getPassportNumber());
            }

            if (getNotDetectedProperties().get(NAME_PROPERTY) != null) {
                passport.setName(getName());
            }

        }
        clearLines();
        return passport;
    }

    @Override
    public void setParams() {
        //getNotDetectedProperties().put(GENRE, "Genero");
        setParams(readMode);

    }

    public void setParams(ReadMode readMode) {

        this.readMode = readMode;

        if (readMode == PassportTextDetector.ReadMode.Front) {
            getNotDetectedProperties().put(DATES_PROPERTIES, "Data de Nascimento\nData de validade\n ");
        }
        else{

            getNotDetectedProperties().put(NAME_PROPERTY, "Nome");
            getNotDetectedProperties().put(PASSPORT_ID, "Numero do Passaporte");
        }
    }

    public String getPassportNumber() {

        String passportNumber;

        int position = find("^(\\d{2})(\\w{2})(\\d{5})$");

        if (position == -1)
            return "";

        passportNumber = lines.get(position).getValue();
        getNotDetectedProperties().remove(PASSPORT_ID);

        return passportNumber;
    }


    public void setDates() {

        int datePosition1 = find(DATE_INTEGRETY_REGEX);
        int datePosition2 =  find(DATE_INTEGRETY_REGEX,datePosition1+1);
        int datePosition3 = find(DATE_INTEGRETY_REGEX,datePosition2+1);

        if(datePosition1 == -1 || datePosition2 == -1 || datePosition3 == -1)
            return;

        passport.setBirthDate(lines.get(datePosition1).getValue().replace("-","/"));
        passport.setIssuanceDate(lines.get(datePosition2).getValue().replace("-","/"));
        passport.setExpiryDate(lines.get(datePosition3).getValue().replace("-","/"));



        getNotDetectedProperties().remove(DATES_PROPERTIES);


    }

    public String getName() {

        String name;

        int position = find("[A-Z]{4}");

        if (position == -1)
            return "";

        name = lines.get(position).getValue();

        if(find(name,NAME_INTEGRETY_REGEX)|| name.length() < 9)
            return "";

        getNotDetectedProperties().remove(NAME_PROPERTY);

        return name;
    }


}
