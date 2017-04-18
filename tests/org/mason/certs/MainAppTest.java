package org.mason.certs;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;


/**
 * Created by masonb on 2/14/2017.
 */
public class MainAppTest {
    @BeforeMethod
    public void setUp() throws Exception {
    }

    @Test
    public void probeIp() throws Exception {
        MainApp x=new MainApp();
        InetAddress addr=InetAddress.getByName("216.58.193.78");
        //InetAddress addr=InetAddress.getByName("0.0.0.0");
        boolean b=addr.isAnyLocalAddress();
        b=addr.isLinkLocalAddress();
        HostData hostData = x.probeIp(addr,443,true);
        Assert.assertNotNull(hostData);
        ObjectMapper mapper=new ObjectMapper();
        String json = mapper.writeValueAsString(hostData);
        System.out.println(json);
    }



}