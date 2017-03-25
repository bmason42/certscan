package org.mason.certs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

/**
 * Created by masonb on 2/23/2017.
 */
public class LoaderMain {
    public static void main(String[] args){
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard().withCredentials(new ProfileCredentialsProvider("personal")).withRegion(Regions.US_WEST_2);
        AmazonDynamoDB client = builder.build();//new AmazonDynamoDBClient(new ProfileCredentialsProvider("personal"));
        DynamoDBMapper dbMapper = new DynamoDBMapper(client);
        ListTablesResult listTablesResult = client.listTables();


        ObjectMapper mapper=new ObjectMapper();
        File f=new File("certdata0.0.0.0-1.47.154.0.json");
        if (!f.exists()){
            System.err.println("need a file");
            System.exit(1);
        }
        HostData whoDoneIt=null;
        int count=0;
        try {
            HostData[] hostData = mapper.readValue(f, HostData[].class);
            for (HostData x:hostData){
                whoDoneIt=x;
                if (x.isSsl() ){
                    if (x.getCertData().size()>0) {
                        x.setCertStart(x.getCertData().get(0).getStart().getTime());
                        x.setCertEnd(x.getCertData().get(0).getEnd().getTime());
                    }
                    x.setSelfSigned(x.getCertData().size() <2);
                }
                try {
                    dbMapper.save(x);
                }catch (Exception e){
                    System.err.println(e.getMessage());
                    System.err.println(x.toString());
                }
                if ( (++count %100) ==0){
                    System.out.println(count);
                }
            }
            System.out.println("Finished: " + count);
        } catch (Exception e) {
            System.out.println(whoDoneIt.getName());
            e.printStackTrace();
        }
    }
}
