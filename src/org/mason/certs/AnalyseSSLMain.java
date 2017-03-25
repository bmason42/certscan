package org.mason.certs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by masonb on 3/1/2017.
 */
public class AnalyseSSLMain {
    public static void main(String[]args){
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard().withCredentials(new ProfileCredentialsProvider("personal")).withRegion(Regions.US_WEST_2);
        AmazonDynamoDB client = builder.build();//new AmazonDynamoDBClient(new ProfileCredentialsProvider("personal"));
        DynamoDBMapper dbMapper = new DynamoDBMapper(client);

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":port", new AttributeValue().withN("443"));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName("HostData")
                .withFilterExpression("port = :port")
                .withExpressionAttributeValues(expressionAttributeValues);


        ScanResult result = client.scan(scanRequest);
        Integer n = result.getCount();
        System.out.println(n);

        DynamoDBScanExpression scanExpression=new DynamoDBScanExpression()
                .withFilterExpression("port = :port")
                .withExpressionAttributeValues(expressionAttributeValues);
        PaginatedScanList<HostData> scan = dbMapper.scan(HostData.class, scanExpression);

        for (HostData x:scan){
            System.out.println(String.format("%b, %s",x.isSelfSigned(),new Date(x.getCertEnd()).toString()));
        }

    }
}
