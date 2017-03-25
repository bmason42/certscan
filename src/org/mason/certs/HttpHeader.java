package org.mason.certs;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by masonb on 2/23/2017.
 */
@DynamoDBDocument
public class HttpHeader {
    private String header="status";
    private List<String> values;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpHeader that = (HttpHeader) o;

        return header.equals(that.header);

    }

    public String getHeader() {
        return header==null ? "status" : header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        List<String> vals = scrubHeaderValList(values);
        this.values = vals;
    }
    public static List<String> scrubHeaderValList(List<String> values) {
        List<String> vals=new ArrayList<>();
        for (String x:values){
            if ( (x!= null) && (x.length()>0)){
                vals.add(x);
            }
        }
        return vals;
    }

    @Override
    public int hashCode() {
        return header.hashCode();
    }
}
