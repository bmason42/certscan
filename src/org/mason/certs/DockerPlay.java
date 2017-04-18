package org.mason.certs;

import java.util.Map;

/**
 * Created by masonb on 4/8/2017.
 */
public class DockerPlay {
    public static void main(String[] args){
        System.out.println("Hello Docker");
        Map<String, String> env = System.getenv();
        for (Map.Entry<String,String> entry:env.entrySet()){
            System.out.println(entry.getKey() + " : " +entry.getValue());
        }
        System.out.println("Cores:" + Runtime.getRuntime().availableProcessors());
    }
}
