package org.mason.certs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by masonb on 2/17/2017.
 */
public class IPIterator {
    private int[] start;
    private int[] end;

    public IPIterator(String startAddr, String endAddr) {
        String[] startParts=startAddr.split("\\.");
        String[] endParts=endAddr.split("\\.");
        if (startParts.length != endParts.length){
            throw new IllegalStateException("Address sizes must match");
        }
        start=new int[startParts.length];
        end=new int[endParts.length];
        for (int i=0;i<start.length;i++){
            start[i]=Integer.parseInt(startParts[i]);
            end[i]=Integer.parseInt(endParts[i]);
        }

    }
    public void iterate(){
        int pos=0;
        doIterate(start,end,pos);
    }

    private void doIterate(int[] start, int[] end, int pos) {
        int[] l1=Arrays.copyOf(start,start.length);
        int[] l2=Arrays.copyOf(end,end.length);
        int x,y;
        //flip the range around inscase someone supplied it backwards
        if (l1[pos] >l2[pos]){
            x=l2[pos];
            y=l1[pos];
        }else{
            x=l1[pos];
            y=l2[pos];
        }
        for (int i=x;i<=y;i++){
            l1[pos]=i;
            handleAddress(l1);
            int nextPos=pos+1;
            if (nextPos <end.length) {
                doIterate(l1, l2, nextPos);
            }
        }
    }
    private void handleAddress(int[] data){
        byte[] bits=new byte[data.length];
        for (int i=0;i<data.length;i++){
            Integer x=data[i];
            bits[i]=x.byteValue();
        }
        try {
            InetAddress addr=InetAddress.getByAddress(bits);
            doStuff(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void doStuff(InetAddress addr) {
        System.out.println(addr.toString());
    }
}
