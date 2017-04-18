package org.mason.certs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

/**
 * Created by masonb on 4/8/2017.
 */
public class DBTest {
    AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard().withCredentials(new ProfileCredentialsProvider("personal")).withRegion(Regions.US_WEST_2);
    AmazonDynamoDB client = builder.build();//new AmazonDynamoDBClient(new ProfileCredentialsProvider("personal"));
    DynamoDBMapper dbMapper = new DynamoDBMapper(client);
    @Test
    public void writeRecord()throws Exception{
        ObjectMapper mapper=new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);

        HostData[] data;
        try(InputStream in = this.getClass().getClassLoader().getResourceAsStream("hostdata-1.json")){
            data=mapper.readValue(in,HostData[].class);
        }
        Assert.assertNotNull(data);
        Assert.assertTrue(data.length==1);
        dbMapper.save(data[0]);
        for (CertData d:data[0].getCertData()) {
            dbMapper.save(d);
        }

    }
}
