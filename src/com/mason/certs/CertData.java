package com.mason.certs;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by masonb on 2/14/2017.
 */
@DynamoDBDocument
public class CertData {
    private String certType;
    private String pubKey;
    private Date start;
    private Date end;
    private String dn;
    private List<SubjectAlt> sans=new ArrayList<>();
    private String issureDN;
    private String serialNumber;


    public void addSAN(SubjectAlt a){
        sans.add(a);
    }
    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public List<SubjectAlt> getSans() {
        return sans;
    }

    public void setSans(List<SubjectAlt> sans) {
        this.sans = sans;
    }

    public String getIssureDN() {
        return issureDN;
    }

    public void setIssureDN(String issureDN) {
        this.issureDN = issureDN;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
