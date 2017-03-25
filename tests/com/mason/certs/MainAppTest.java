package com.mason.certs;

import org.junit.Assert;

import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Created by masonb on 2/14/2017.
 */
public class MainAppTest {
    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.Test
    public void probeIp() throws Exception {
        MainApp x=new MainApp();
        InetAddress addr=InetAddress.getByName("216.58.193.78");
        //InetAddress addr=InetAddress.getByName("0.0.0.0");
        boolean b=addr.isAnyLocalAddress();
        b=addr.isLinkLocalAddress();
        HostData hostData = x.probeIp(addr,443,true);

        Assert.assertNotNull(hostData);
    }



}