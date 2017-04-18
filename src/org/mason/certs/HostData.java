package org.mason.certs;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import javax.xml.bind.annotation.XmlTransient;
import java.beans.Transient;
import java.util.*;

/**
 * Created by masonb on 2/14/2017.
 */
@DynamoDBTable(tableName="host_data")
public class HostData {
    public boolean ssl=false;

    private String ip;
    private int port;
    private String cipherSuite;
    private List<CertData> certData=new ArrayList<>();
    private String name;
    private boolean selfSigned;
    private List<HttpHeader> headers=new ArrayList<>();

    //only aplies if ssl is true
    private long certStart=0;
    private long certEnd=0;

    @DynamoDBAttribute(attributeName = "certStart")
    public long getCertStart() {
        return certStart;
    }

    @DynamoDBAttribute(attributeName = "certStartAsDate")
    public Date getCertStartAsDate(){
        return new Date(certStart);
    }

    public void setCertStart(long certStart) {
        this.certStart = certStart;
    }

    @DynamoDBAttribute(attributeName = "certEnd")
    public long getCertEnd() {
        return certEnd;
    }

    @DynamoDBAttribute(attributeName = "certEndAsDate")
    public Date getCertEndAsDate(){
        return new Date(certEnd);
    }

    @DynamoDBAttribute(attributeName = "selfSigned")
    public boolean isSelfSigned() {
        return selfSigned;
    }

    public void setSelfSigned(boolean selfSigned) {
        this.selfSigned = selfSigned;
    }

    public void setCertEnd(long certEnd) {
        this.certEnd = certEnd;
    }

    @DynamoDBHashKey(attributeName="ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @DynamoDBAttribute(attributeName = "ssl")
    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    @DynamoDBAttribute(attributeName = "ciphers")
    public String getCipherSuite() {
        return scrub(cipherSuite);
    }

    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite == null ? "none" : cipherSuite;
    }

    @DynamoDBIgnore
    public List<CertData> getCertData() {
        return certData;
    }


    public void setCertData(List<CertData> certData) {
        this.certData = certData;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return scrub(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBRangeKey(attributeName = "port")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHeaders(List<HttpHeader> headers) {
        this.headers = headers;
    }


    @DynamoDBAttribute(attributeName = "headers")
    //@DynamoDBIgnore
    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public void addCertData(CertData d) {
        certData.add(d);
    }
    public void addHeader(HttpHeader h){
        HttpHeader newHeader = scrubHeader(h);
        headers.add(newHeader);
    }

    static public HttpHeader scrubHeader(HttpHeader h) {
        String headerKey= (h.getHeader() == null || h.getHeader().length()==0) ?"status" :h.getHeader();


        HttpHeader newHeader=new HttpHeader();
        newHeader.setHeader(headerKey);
        newHeader.setValues(h.getValues());
        return newHeader;
    }



    private String scrub(String in){
        String ret=in == null ? null: in;
        return ret;
    }

    @Override
    public String toString() {
        return "HostData{" +
                "ssl=" + ssl +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", name='" + name + '\'' +
                ", certStart=" + certStart +
                ", certEnd=" + certEnd +
                '}';
    }
}
