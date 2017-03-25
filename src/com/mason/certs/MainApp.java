package com.mason.certs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javaws.Main;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by masonb on 2/14/2017.
 */
public class MainApp {

    final ObjectMapper mapper = new ObjectMapper();
    final BlockingQueue<InetAddress> ipsToScan;
    final BlockingQueue<HostData> hostRecordsToWrite;
    final AtomicBoolean doneWithIPs = new AtomicBoolean(false);
    ThreadGroup workerThreads;
    Thread writerThread;
    final private int workerThreadCount;
    final CountDownLatch latch;// = new CountDownLatch(WORKER_THREAD_COUNT + 1);
    public static final String TESTURL = "https://google.com";

    public static void main(String[] args) {
        MainApp x = new MainApp();
        String start="0.0.0.0";
        String end="1.255.255.255";
        if (args.length ==2){
            start=args[0];
            end=args[1];
        }
        x.doScan(start,end);
    }

    public MainApp() {
        workerThreadCount=Integer.parseInt(System.getProperty("worker.count","2000"));
        int queueSize=Integer.parseInt(System.getProperty("queue.size","1000"));
        ipsToScan= new LinkedBlockingQueue<>(queueSize);
        hostRecordsToWrite= new LinkedBlockingQueue<>(queueSize);
        latch=new CountDownLatch(workerThreadCount+1);
        disableSslChecks();
    }

    public static void disableSslChecks() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc
                    .getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processor() {
        try {
            InetAddress addr = this.ipsToScan.poll(10, TimeUnit.SECONDS);
            while (!doneWithIPs.get() || (addr != null)) {
                if (addr != null) {
                    try {
                        HostData hostData;
                        hostData= probeIp(addr,80,false);
                        if (hostData != null) {
                            hostRecordsToWrite.put(hostData);
                        }
                        hostData= probeIp(addr,8080,false);
                        if (hostData != null) {
                            hostRecordsToWrite.put(hostData);
                        }
                        hostData= probeIp(addr,443,true);
                        if (hostData != null) {
                            hostRecordsToWrite.put(hostData);
                        }
                        hostData = probeIp(addr,8443,true);
                        if (hostData != null) {
                            hostRecordsToWrite.put(hostData);
                        }
                    } catch (Exception e) {
                        //ignore these
                    }
                }
                addr = this.ipsToScan.poll(10, TimeUnit.SECONDS);

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    HostData probeIp(InetAddress addr,int port,boolean ssl)  {
        HostData hostData = null;
        if (addr.isAnyLocalAddress()){
            return null;
        }

        String urlString;
        if (ssl){
            urlString="https://" + addr.getHostAddress() + ":" + port;
        } else{
            urlString="http://" + addr.getHostAddress() + ":" + port;
        }
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            hostData = new HostData();
            hostData.setPort( port);
            hostData.setIp(addr.getHostAddress());
            hostData.setName( addr.getHostName());
            Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> x : headerFields.entrySet()) {
                HttpHeader header = new HttpHeader();
                header.setHeader(x.getKey());
                header.setValues(x.getValue());
                hostData.addHeader(header);
            }

            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection https = (HttpsURLConnection) urlConnection;
                hostData.setCipherSuite( https.getCipherSuite());
                hostData.ssl = true;
                Certificate[] serverCertificates = https.getServerCertificates();
                for (Certificate x : serverCertificates) {
                    CertData d = new CertData();
                    d.setCertType(x.getType());
                    d.setPubKey( DatatypeConverter.printHexBinary(x.getPublicKey().getEncoded()));
                    if (x instanceof X509Certificate) {
                        X509Certificate x509 = (X509Certificate) x;
                        d.setStart( x509.getNotBefore());
                        d.setEnd(x509.getNotAfter());
                        Principal issuerDN = x509.getIssuerDN();
                        if (issuerDN != null) {
                            d.setIssureDN( issuerDN.getName());
                        }
                        String sn=x509.getSerialNumber().toString();
                        d.setSerialNumber(sn);
                        d.setDn(x509.getSubjectDN().getName());
                        Collection<List<?>> sans = x509.getSubjectAlternativeNames();
                        if (sans != null) {
                            for (List<?> san : sans) {
                                SubjectAlt sa = new SubjectAlt();
                                Integer type = (Integer) san.get(0);
                                sa.setType( type.intValue());
                                sa.setValue((String)san.get(1));
                                d.addSAN(sa);
                            }
                        }
                    }
                    hostData.addCertData(d);
                }
            }
        }catch (Exception e){
            //no connection, just return null
            hostData=null;
        }
        return hostData;
    }

    private void doScan(String start,String end) {
        initThreads();
        final AtomicLong counter=new AtomicLong(0);
        try{
            IPIterator it=new IPIterator(start,end){
                @Override
                public void doStuff(InetAddress addr) {
                    try {
                        long l=counter.incrementAndGet();
                        if ( (l%1000)==0){
                            System.out.println(l + " - " + addr );
                        }
                        ipsToScan.put(addr);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            it.iterate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doneWithIPs.set(true);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    private void initThreads() {
        workerThreads = new ThreadGroup("scanner threads");
        for (int i = 0; i < workerThreadCount; i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    processor();
                    latch.countDown();
                }
            };
            Thread t = new Thread(workerThreads, run, "Processor-" + i);
            t.start();
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                writterFunc();
                latch.countDown();
            }
        };
        writerThread = new Thread(workerThreads, run, "Writer Thread");
        writerThread.start();
    }

    private static boolean checkPrivate(int a, int b, int c, int d) {
        if (a == 169) {
            return true;
        }
        if (a == 10) {
            return true;
        }
        if ((a == 192) && (b == 168)) {
            return true;
        }
        if ((a == 172) && ((b >= 16) && (b <= 31))) {
            return true;
        }
        return false;
    }

    private void writterFunc() {
        try (FileOutputStream fout = new FileOutputStream("certdata.json");
             PrintStream out = new PrintStream(fout)) {
            out.println("[");
            HostData data = hostRecordsToWrite.poll(2, TimeUnit.MINUTES);
            int n=0;
            boolean first=true;
            while (!this.doneWithIPs.get() || (data != null)) {
                if (data != null) {
                    String json = mapper.writeValueAsString(data);
                    if (first){
                        first=false;
                    }else{
                        //print the comma for the previous line
                        out.println(",");
                    }
                    out.print(json);
                    if (++n >100){
                        out.flush();
                        n=0;
                    }
                }
                data = hostRecordsToWrite.poll(2, TimeUnit.MINUTES);
            }
            out.println();
            out.println("]");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
