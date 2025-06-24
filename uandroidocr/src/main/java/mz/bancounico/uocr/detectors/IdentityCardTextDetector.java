package mz.bancounico.uocr.detectors;

import android.content.Context;
import android.util.Log;

import mz.bancounico.uocr.models.IdentityCard;


/**
 * Created by Barros on 2/17/2018.
 */

public class IdentityCardTextDetector extends TextDetector {


    private IdentityCard identityCard;

    private final String BASE_INTEGRETY_REGEX = "NOME|NAME|BRITH|ESTADO|LOCAL|SEXO|SEX|ASSINATURA|CIVIL|REPUBLICA|DATA|ADDRESS|BIRTHDATE|:|/|REPÚBLICA|NATURALIDADE";
    private final String NAME_INTEGRETY_REGEX = BASE_INTEGRETY_REGEX + "|\\d";
    private final String GENDER_INTEGRETY_REGEX = "^M|^F";
    private final String DATE_INTEGRETY_REGEX = "\\s*(3[01]|[12][0-9]|0[1-9])\\/(1[012]|0[1-9])\\/((?:19|20)\\d{2})\\s*$";

    // Reading properties
    public final static String IDENTITY_CARD_ID = "DIRE_CARD_ID";
    public final static String NAME_PROPERTY = "NAME";
    public final static String ADDRESS_PROPERTY = "ADDRESS";
    public final static String BIRTH_DATE_PROPERTY = "BIRTH_DATE";
    public final static String GENRE = "GENRE";
    public final static String EXPIRY_DATE_PROPERTY = "EXPIRY_DATE";
    public final static String ISSUANCE_DATE = "ISSUANCE_DATE";


    public String identityCardId;

    @Override
    public void setParams() {
        setParams(readMode);
    }


    public static enum ReadMode {
        Front,
        BACK
    }

    private ReadMode readMode;


    public IdentityCardTextDetector(Context context, ReadMode readMode) {
        super(context);
        identityCardId = "";
        identityCard = new IdentityCard();
        identityCard.setNumber("");
        this.readMode = readMode;
        setParams();
    }


    @Override
    public IdentityCard getData() {
        if (readMode == ReadMode.Front) {

            if (getNotDetectedProperties().get(NAME_PROPERTY) != null) {
                identityCard.setName(getName());
            }

            if (getNotDetectedProperties().get(BIRTH_DATE_PROPERTY) != null) {
                identityCard.setBirthDate(getBirthDate());
            }

            if (getNotDetectedProperties().get(ADDRESS_PROPERTY) != null) {
                identityCard.setAddress(getEndereco());
            }

            if (getNotDetectedProperties().get(IDENTITY_CARD_ID) != null) {
                identityCard.setNumber(getIdentityCardNumberFront());
            }

        } else {
            if (identityCardId.isEmpty()) {
                identityCardId = getIdentityCardNumber();
                if (!identityCardId.isEmpty())
                    identityCard.setNumber(getIdentityCardNumber());
            }
            if (getNotDetectedProperties().get(EXPIRY_DATE_PROPERTY) != null) {
                identityCard.setExpiryDate(getDateFromBack());
            }
            if (getNotDetectedProperties().get(GENRE) != null) {
                identityCard.setGender(getGenre());
            }

            if (getNotDetectedProperties().get(ISSUANCE_DATE) != null && !identityCard.getExpiryDate().isEmpty()) {
                identityCard.setIssuanceDate(getIssuanceDate());
            }
        }

        clearLines();
        return identityCard;

    }


    public String getName() {

        int position = find("^NOME\\/|\\/NAME");
        String name;

        if (position == -1) {
            return "";

        }

        try {
            name = lines.get(position + 1).getValue().toUpperCase();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        if (find(name, NAME_INTEGRETY_REGEX)|| name.length() < 9) {
            return "";

        }

        getNotDetectedProperties().remove(NAME_PROPERTY);
        return name;
    }

    public String getEndereco() {

        String address = "";

        int position = find("^LOCAL|\\/ADDRESS|ADDRESS");
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
        address = address.replaceAll("\\W*((?i)Altura(?-i))\\W*|\\W*((?i)Height(?-i))\\W*", "").toUpperCase();
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
        getNotDetectedProperties().remove(BIRTH_DATE_PROPERTY);
        return lines.get(position).getValue();
    }

    public String getIssuanceDate() {


        int position = find(DATE_INTEGRETY_REGEX);

        if (position == -1) {
            return "";
        }

        if (lines.get(position).getValue().trim().equals(identityCard.getExpiryDate().trim())) {
            int position2 = find(DATE_INTEGRETY_REGEX, position + 1);

            if (position2 == -1) {
                return "";
            }
            position = position2;
        }

        String date = lines.get(position).getValue();

        if (!find(date, DATE_INTEGRETY_REGEX)) {
            return "";
        }

        getNotDetectedProperties().remove(ISSUANCE_DATE);
        return date;
    }

    public String getIdentityCardNumber() {

        int firstPosition = find("^BI");
        int secondPosition = find("(\\d{7})([M]|[F])");
        if ((firstPosition == -1) && (secondPosition == -1)) {
            return "";
        }

        String id = lines.get(secondPosition).getValue().substring(18, 22) + lines.get(firstPosition).getValue().substring(5, 13) + lines.get(secondPosition).getValue().charAt(26);

        if (!find(id, "\\d{12}([A-Za-z])")) {
            return "";
        }

        getNotDetectedProperties().remove(IDENTITY_CARD_ID);
        return id;
        //getNotDetectedProperties().remove(DIRE_CARD_ID);
        //return lines.get(firstPosition).getValue().replaceAll("^[^0-9]+", "").replaceAll("\\s","");
    }

    public String getIdentityCardNumberFront() {

        int position = find("\\d{6}");

        if (position == -1) {
            return "";
        }
        String id = lines.get(position).getValue().replaceAll("^[^0-9]+", "").replaceAll("\\s", "").toUpperCase();

        if(id.length() < 13){
            return "";
        }

        id = id.replaceAll("O","0");

        getNotDetectedProperties().remove(IDENTITY_CARD_ID);
        return id;
    }


    private String getGenre() {
        int position = find("(\\d{7})([M]|[F])");

        if (position == -1) {
            return "";
        }
        Log.d("Genre", "" + lines.get(position).getValue().charAt(7));

        String gender = lines.get(position).getValue().charAt(7)+"";

        if(!find(gender,GENDER_INTEGRETY_REGEX))
            return "";

        getNotDetectedProperties().remove(GENRE);


        return lines.get(position).getValue().charAt(7) + "";
    }


    public String getDateFromBack() {


        String date;

        int position = find("(\\d{7})([M]|[F])");

        if (position == -1) {
            return "";
        }

        String line = lines.get(position).getValue();

        date = line.substring(12, 14) + "/" + line.substring(10, 12) + "/" + "20" + line.substring(8, 10);

        if (find(date, "[A-Za-z]")) {
            return "";
        }

        if (!find(date, "\\s*(3[01]|[12][0-9]|0?[1-9])\\/(1[012]|0?[1-9])\\/((?:19|20)\\d{2})\\s*$")) {
            return "";
        }

        // Pode ser que a data de emissao coincida com a data de validade;
//        if(date.trim().equals(identityCard.getIssuanceDate().trim()))
//            getNotDetectedProperties().put(ISSUANCE_DATE,"Data de emissao");

        getNotDetectedProperties().remove(EXPIRY_DATE_PROPERTY);
        return date;
    }


    public void setParams(ReadMode readMode) {

        this.readMode = readMode;

        if (readMode == ReadMode.Front) {
            getNotDetectedProperties().put(NAME_PROPERTY, "Nome");
            getNotDetectedProperties().put(ADDRESS_PROPERTY, "Endereço");
            getNotDetectedProperties().put(BIRTH_DATE_PROPERTY, "Data de Nascimento");
            getNotDetectedProperties().put(IDENTITY_CARD_ID, "Numero");
        } else {
            getNotDetectedProperties().put(EXPIRY_DATE_PROPERTY, "Data De Validade");
            getNotDetectedProperties().put(ISSUANCE_DATE, "Data de emissao");
            getNotDetectedProperties().put(GENRE, "Genero");
            //getNotDetectedProperties().put(DIRE_CARD_ID, "Numero");
        }

    }

    public IdentityCard getIdentityCard() {
        return identityCard;
    }


}




