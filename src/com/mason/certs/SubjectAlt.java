package com.mason.certs;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

/**
 * Created by masonb on 2/15/2017.
 */
@DynamoDBDocument
public class SubjectAlt {
    /**
     * Type is pulled from the x509 stuff, it has meaning documented somewhere
     */
    private int type ;
    private String value;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
