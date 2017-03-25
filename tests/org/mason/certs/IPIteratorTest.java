package org.mason.certs;


import org.testng.annotations.Test;


/**
 * Created by masonb on 2/20/2017.
 */
public class IPIteratorTest {
    @Test
    public void test(){
        IPIterator it=new IPIterator("127.0.0.1","128.255.255.255");
        it.iterate();
    }
}