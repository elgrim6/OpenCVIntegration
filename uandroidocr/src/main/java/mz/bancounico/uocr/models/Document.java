package mz.bancounico.uocr.models;

import java.io.Serializable;

/**
 * Created by dds_unico on 8/8/18.
 */

public class Document implements Serializable {

    private String number;
    private String expiryDate;
    private String name;
    private String birthDate;
    private String gender;
    private String placeOfBirth;
    private String issuanceDate;
    private String issuedIn;
    private String country;
    private String nacionality;
    private DocumentType docuemntType;


    public DocumentType getDocuemntType() {
        return docuemntType;
    }

    public void setDocuemntType(DocumentType docuemntType) {
        this.docuemntType = docuemntType;
    }

    public String getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIssuedIn() {
        return issuedIn;
    }

    public void setIssuedIn(String issuedIn) {
        this.issuedIn = issuedIn;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNacionality() {
        return nacionality;
    }


    public enum DocumentType {


        IDENTITY_CARD("Bilhete de Identidade"),


        PASSPORT("Passaporte"),


        DIRE("Dire"),


        DRIVER_LICENSE("Carta de Condução"),


        OTHERS("Outros");


        String id;

        DocumentType(String id){
            this.id=id;
        }

        public String getId() {
            return id;
        }

        public static DocumentType getDocumentTypeFromId(String id){
            for(DocumentType documentType: DocumentType.values() ){
                if(documentType.id.equals(id)) return documentType;
            }
            return null;
        }

    }

    public void setNacionality(String nacionality) {
        this.nacionality = nacionality;
    }
}