package mz.bancounico.uocr.detectors;

import android.content.Context;
//import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mz.bancounico.uocr.models.Dire;

/**
 * Created by dds_unico on 11/26/18.
 */

public class DireTextDetector extends TextDetector {


    private Dire dire;
    private String name = "";
    private String surname = "";

    private final String BASE_INTEGRETY_REGEX = "NOME|NAME|BRITH|ESTADO|LOCAL|SEXO|SEX|ASSINATURA|CIVIL|REPUBLICA|DATA|ADDRESS|BIRTHDATE|:|/|REPÚBLICA|NACIONALIDADE|TIPO|PERMANENTE";
    private final String NAME_INTEGRETY_REGEX = BASE_INTEGRETY_REGEX + "|\\d";
    private final String GENDER_INTEGRETY_REGEX = "^M|^F";
    private final String DATE_INTEGRETY_REGEX = "\\s*(3[01]|[12][0-9]|0[1-9])\\/(1[012]|0[1-9])\\/((?:19|20)\\d{2})\\s*";

    // Reading properties
    public final static String DIRE_CARD_ID = "DIRE_CARD_ID";
    public final static String NAME_PROPERTY = "NAME";
    public final static String SURNAME_PROPERTY = "SURNAME_PROPERTY";
    public final static String BIRTH_DATE_PROPERTY = "BIRTH_DATE";
    public final static String GENRE = "GENRE";
    public final static String ADDRESS_PROPERTY = "ADDRESS";
    public final static String EXPIRY_DATE_PROPERTY = "EXPIRY_DATE";
    public final static String ISSUANCE_DATE = "ISSUANCE_DATE";


    @Override
    public void setParams() {
        setParams(readMode);
    }


    public static enum ReadMode {
        Front,
        BACK
    }

    private DireTextDetector.ReadMode readMode;


    public DireTextDetector(Context context, DireTextDetector.ReadMode readMode) {
        super(context);
        dire = new Dire();
        dire.setNumber("");
        dire.setName("");
        this.readMode = readMode;
        setParams();
    }


    @Override
    public Dire getData() {
        if (readMode == DireTextDetector.ReadMode.Front) {

            if (getNotFriendlyDetectedProperties().get(NAME_PROPERTY) != null) {
                name = getName();
            }

            if (getNotFriendlyDetectedProperties().get(SURNAME_PROPERTY) != null) {
                surname = getSurname();
            }

            if (getNotDetectedProperties().get(BIRTH_DATE_PROPERTY) != null) {
                dire.setBirthDate(getBirthDate());
            }

            if (getNotFriendlyDetectedProperties().get(DIRE_CARD_ID) != null) {
                dire.setNumber(getDireNumber());
            }

            if (!name.isEmpty() && !surname.isEmpty() && dire.getName().isEmpty()) {
                dire.setName(name + " " + surname);
            }

        } else {

            if (getNotDetectedProperties().get(EXPIRY_DATE_PROPERTY) != null) {
                setDates();
            }


        }

        clearLines();
        return dire;

    }


    public String getName() {

        int position = find("^NOME\\/|\\/NAME|\\/NA|ME\\/|\\/N");
        String nome ;

        if (position == -1) {
            return "";
        }

        try {
            nome = lines.get(position + 1).getValue().toUpperCase();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        if (find(nome, NAME_INTEGRETY_REGEX)) {
            return "";
        }

        getNotFriendlyDetectedProperties().remove(NAME_PROPERTY);
        return nome;
    }


    public String getSurname() {

        int position = find("^APELIDO\\/|\\/SURNAME|^APELLDO|\\/SUR|\\/S|DO\\/");
        String nome;

        if (position == -1) {
            return "";
        }

        try {
            nome = lines.get(position + 1).getValue().toUpperCase();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        if (find(nome, NAME_INTEGRETY_REGEX)) {
            return "";
        }

        getNotFriendlyDetectedProperties().remove(SURNAME_PROPERTY);
        return nome;
    }

    public String getEndereco() {

        String address = "";

        int position = find("^ENDERE|\\/ADDRESS|ADDRESS");
        int errorMarginPosition = 0;

        try {
            if (lines.get(position + 1).getValue().contains("Sex"))
                errorMarginPosition++;

            if (position == -1) {
                return "";
            }

            address = lines.get(position + 1 + errorMarginPosition).getValue() + " " + lines.get(position + 2 + errorMarginPosition).getValue();

        } catch (IndexOutOfBoundsException e) {
            return address;
        }

        address = address.replaceAll("(\\d{1}|)([,]|[.])(\\d{2})(\\w{1})", "");
        address = address.replaceAll("((?i)NO|(?i)NO )(\\d)", "Nº $2");

        if (find(address, BASE_INTEGRETY_REGEX)) {
            return "";
        }

        getNotDetectedProperties().remove(ADDRESS_PROPERTY);
        return address;
    }

    public String getBirthDate() {
        int position = find(DATE_INTEGRETY_REGEX);


        if (position == -1) {
            return "";
        }

        String date;
        Pattern pattern = Pattern.compile(DATE_INTEGRETY_REGEX);
        Matcher matcher = pattern.matcher(lines.get(position).getValue());
        if (matcher.find())
            date = matcher.group(1) + "/" + matcher.group(2) + "/" + matcher.group(3);
        else
            return "";


        getNotDetectedProperties().remove(BIRTH_DATE_PROPERTY);
        return date;
    }


    public String getDireNumber() {

        int position = find("^DIRE|^D.I.R|^D.R|^DIL");

        if (position == -1) {
            return "";
        }
        String id;
        String content = lines.get(position).getValue().replaceAll("[OóòÓÒo]", "0").replaceAll("\\s", "").toUpperCase();

        Pattern pattern = Pattern.compile("(\\d{2}[A-Z]{2}\\d{8}[A-Z])");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            id = matcher.group(1);
        } else
            return "";
        getNotFriendlyDetectedProperties().remove(DIRE_CARD_ID);
        return id;
    }


    public void setDates() {


        int datePosition1 = find(DATE_INTEGRETY_REGEX);
        int datePosition2 = find(DATE_INTEGRETY_REGEX, datePosition1 + 1);


        if (datePosition1 == -1 || datePosition2 == -1)
            return;

        String date1 = lines.get(datePosition1).getValue().replace("-", "/");
        String date2 = lines.get(datePosition2).getValue().replace("-", "/");

        try {


            if (Integer.parseInt(date1.substring(6)) > Integer.parseInt(date2.substring(6))) {
                dire.setIssuanceDate(date2);
                dire.setExpiryDate(date1);
            } else {
                dire.setIssuanceDate(date1);
                dire.setExpiryDate(date2);
            }

            getNotDetectedProperties().remove(ISSUANCE_DATE);
            getNotDetectedProperties().remove(EXPIRY_DATE_PROPERTY);
        } catch (Exception e) {

        }

    }


    public void setParams(DireTextDetector.ReadMode readMode) {

        this.readMode = readMode;

        if (readMode == DireTextDetector.ReadMode.Front) {
            getNotFriendlyDetectedProperties().put(NAME_PROPERTY, "Nome");
            getNotFriendlyDetectedProperties().put(SURNAME_PROPERTY, "Apelido");
            //getNotDetectedProperties().put(ADDRESS_PROPERTY, "Endereço");
            getNotDetectedProperties().put(BIRTH_DATE_PROPERTY, "Data de Nascimento");
            getNotFriendlyDetectedProperties().put(DIRE_CARD_ID, "Numero");
        } else {
            getNotDetectedProperties().put(EXPIRY_DATE_PROPERTY, "Data De Validade");
            getNotDetectedProperties().put(ISSUANCE_DATE, "Data de emissao");
        }
    }

    public Dire getDire() {
        return dire;
    }

}
