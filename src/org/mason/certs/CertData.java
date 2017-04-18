package org.mason.certs;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by masonb on 2/14/2017.
 */
@DynamoDBTable(tableName="CertData")
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

    @DynamoDBAttribute(attributeName = "certType")
    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    @DynamoDBAttribute(attributeName = "pubkey")
    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    @DynamoDBAttribute(attributeName = "start")
    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    @DynamoDBAttribute(attributeName = "end")
    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @DynamoDBHashKey(attributeName = "dn")
    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    @DynamoDBAttribute(attributeName = "sans")
    public List<SubjectAlt> getSans() {
        return sans;
    }

    public void setSans(List<SubjectAlt> sans) {
        this.sans = sans;
    }

    @DynamoDBAttribute(attributeName = "issuerDN")
    public String getIssureDN() {
        return issureDN;
    }

    public void setIssureDN(String issureDN) {
        this.issureDN = issureDN;
    }

    @DynamoDBAttribute(attributeName = "serialNum")
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
