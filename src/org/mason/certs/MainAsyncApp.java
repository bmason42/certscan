package org.mason.certs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Year;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by masonb on 2/14/2017.
 */
public class MainAsyncApp {

    final ObjectMapper mapper = new ObjectMapper();
    final BlockingQueue<InetSocketAddress> connectableAddresses;
    final BlockingQueue<HostData> hostRecordsToWrite;
    final AtomicBoolean doneWithIPs = new AtomicBoolean(false);
    ThreadGroup workerThreads;
    final private int workerThreadCount;
    private int writerThreadCount=5;
    final CountDownLatch latch;// = new CountDownLatch(WORKER_THREAD_COUNT + 1);
    public static final String TESTURL = "https://google.com";
    AmazonDynamoDB client ;
    DynamoDBMapper dbMapper;
    int conTimeOut;
    final Selector selector;
    final int maxSelSize;
    public static void main(String[] args) {
        MainAsyncApp x = null;
        try {
            x = new MainAsyncApp();
            String start="1.0.0.0";
            String end="10.255.255.255";
            if (args.length ==2){
                start=args[0];
                end=args[1];
            }
            x.doScan(start,end);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MainAsyncApp() throws IOException {
        final BasicAWSCredentials credentials=new BasicAWSCredentials("AKIAILGX5DKFUTBXFVQA","QEB5EeNIXWO/Ovf39wAqPRUCWgOqmL6u0nLI9d2X");
        AWSCredentialsProvider provider=new AWSCredentialsProvider(){
            @Override
            public AWSCredentials getCredentials() {
                return credentials;
            }

            @Override
            public void refresh() {

            }
        };
        selector=Selector.open();
        ProfileCredentialsProvider loadedProvider = new ProfileCredentialsProvider("personal");
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard().withCredentials(provider).withRegion(Regions.US_WEST_2);
        client= builder.build();
        dbMapper = new DynamoDBMapper(client);
        conTimeOut =Integer.parseInt(System.getProperty("con.timeout","5"));
        workerThreadCount=Integer.parseInt(System.getProperty("worker.count","200"));
        writerThreadCount=Integer.parseInt(System.getProperty("writer.count","3"));
        maxSelSize=Integer.parseInt(System.getProperty("sel.size","10000"));
        int queueSize=Integer.parseInt(System.getProperty("queue.size","500"));
        connectableAddresses=new LinkedBlockingQueue<>(queueSize);
        hostRecordsToWrite= new LinkedBlockingQueue<>(queueSize);
        latch=new CountDownLatch(workerThreadCount+writerThreadCount);
        disableSslChecks();
    }

    public static void disableSslChecks() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    X509Certificate[] certs, String authType) {
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
            InetSocketAddress addr = this.connectableAddresses.poll(10, TimeUnit.SECONDS);
            while (!doneWithIPs.get() || (addr != null)) {
                if (addr != null) {
                    try {
                        HostData hostData=null;
                        hostData= probeIp(addr);
                        if (hostData != null) {
                            hostRecordsToWrite.put(hostData);
                        }
                    } catch (Exception e) {
                        //ignore these
                    }
                }
                addr = this.connectableAddresses.poll(10, TimeUnit.SECONDS);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    HostData probeIp(InetSocketAddress addr)  {
        HostData hostData = null;
        if (addr.getAddress().isAnyLocalAddress()){
            return null;
        }

        String urlString;
        int port = addr.getPort();
        boolean ssl=(port == 443) || (port ==8443);
        if (ssl){
            urlString="https://" + addr.getAddress().getHostAddress() + ":" + port;
        } else{
            urlString="http://" + addr.getAddress().getHostAddress() + ":" + port;
        }
        try {
            String msg = "Trying: " + urlString;
            log(msg);
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(conTimeOut *1000);
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            hostData = new HostData();
            hostData.setPort( port);
            hostData.setIp(addr.getAddress().getHostAddress());
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
                boolean isFirstCertInChain=true;
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
                        if (isFirstCertInChain){
                            isFirstCertInChain=false;
                            hostData.setCertStart(x509.getNotBefore().getTime());
                            hostData.setCertEnd(x509.getNotAfter().getTime());
                        }
                    }
                    hostData.addCertData(d);
                }
            }
        }catch (Exception e){
            log("Failed Connect: " + urlString + " : " + e.getMessage());
            //no connection, just return null
            hostData=null;
        }
        return hostData;
    }

    private void log(String msg) {
        if (Boolean.getBoolean("verbose")) {
            System.out.println(msg);
        }
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
                            System.out.println("Channels: " + selector.keys().size() + " : Connectable Queue: " + connectableAddresses.size() + "  Write Queue:" + hostRecordsToWrite.size());
                        }
                        while (selector.keys().size() >maxSelSize){
                            Thread.sleep(1000);
                        }
                        if (!addr.isAnyLocalAddress()) {
                            createTestChannel(addr, 80);
                            createTestChannel(addr, 443);
                            createTestChannel(addr, 8080);
                            createTestChannel(addr, 8443);
                        }


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

    public void createTestChannel(InetAddress addr, int port) {
        try {
            SocketChannel ch;
            InetSocketAddress sockAddr;
            sockAddr=new InetSocketAddress(addr,port);
            ch=SocketChannel.open();
            ch.configureBlocking(false);
            synchronized (selector) {
                ch.register(selector, SelectionKey.OP_CONNECT);
            }
            ch.connect(sockAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initThreads() {
        Runnable selRun=new Runnable() {
            @Override
            public void run() {
                try {
                    selectThread();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread selThread=new Thread(workerThreads,selRun,"Select Thread");
        selThread.start();

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
                //writterFunc();
                dbWritterFunc();
                latch.countDown();
            }
        };
        for (int i=0;i<writerThreadCount;i++) {
            Thread writerThread = new Thread(workerThreads, run, "Writer Thread");
            writerThread.start();
        }
    }

    private void dbWritterFunc() {
        try {
            HostData data = hostRecordsToWrite.poll(2, TimeUnit.MINUTES);
            int n = 0;
            boolean first = true;
            while (!this.doneWithIPs.get() || (data != null)) {
                if (data != null) {
                    try {
                        dbMapper.save(data);
                        for (CertData d : data.getCertData()) {
                            dbMapper.save(d);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                data = hostRecordsToWrite.poll(2, TimeUnit.MINUTES);

            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
        }
    }

    public void selectThread() throws Exception{
        while (!Thread.currentThread().isInterrupted()){
            Thread.sleep(100);
            List<SelectionKey> keyList;
            synchronized (selector) {
                int available = selector.selectNow();
                keyList=new ArrayList<>(selector.keys());
            }
            Iterator<SelectionKey> keys=keyList.iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isValid()) {
                    if (key.isConnectable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        try {
                            socketChannel.finishConnect();
                            InetSocketAddress addr = (InetSocketAddress) socketChannel.getRemoteAddress();
                            connectableAddresses.put(addr);
                        }catch (Exception e){
                            log(e.getMessage());
                        }
                        socketChannel.close();
                        key.cancel();
                    }
                }
                Object attachment = key.attachment();
                if (attachment == null){
                    key.attach(new Long(System.currentTimeMillis()));
                }else {
                    Long ts = (Long) attachment;
                    long diff = System.currentTimeMillis() - ts;
                    if (diff > (conTimeOut * 1000)) {
                        key.channel().close();
                        key.cancel();
                    }
                }
            }
        }
    }


}
