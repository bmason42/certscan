package com.mason.certs;

import com.datastax.driver.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

/**
 * Created by masonb on 3/5/2017.
 */
public class CasndraLoader {
    static String[] CONTACT_POINTS = {"127.0.0.1"};
    static int PORT = 9042;
    static String keyspace="mydata";

    public static void main(String[] args){
        Cluster cluster = null;
        try {
            // The Cluster object is the main entry point of the driver.
            // It holds the known state of the actual Cassandra cluster (notably the Metadata).
            // This class is thread-safe, you should create a single instance (per target Cassandra cluster), and share
            // it throughout your application.
            cluster = Cluster.builder()
                    .addContactPoints(CONTACT_POINTS)
                    .withPort(PORT)
                    .withClusterName("hostdata-cluster")
                    .build();

            // The Session is what you use to execute queries. Likewise, it is thread-safe and should be reused.
            Session session = cluster.connect();
            session.execute("use mydata;");

            // We use execute to send a query to Cassandra. This returns a ResultSet, which is essentially a collection
            // of Row objects.
            ResultSet rs = session.execute("select release_version from system.local");
            //  Extract the first row (which is the only one in this case).
            Row row = rs.one();

            // Extract the value of the first (and only) column from the row.
            String releaseVersion = row.getString("release_version");
            System.out.printf("Cassandra version is: %s%n", releaseVersion);
            mkTableIfNeeded(session);
            ObjectMapper mapper=new ObjectMapper();
            //File f=new File("certdata0.0.0.0-1.47.154.0.json");
            File f=new File("certdata.json-147.155.0.json");
            if (!f.exists()){
                System.err.println("need a file");
                System.exit(1);
            }

            HostData whoDoneIt=null;
            int count=0;
            try {
                PreparedStatement hostInsert = session.prepare("insert into hostdata (ip, port, name, certDn,cipherSuite) values (?,?,?,?,?)");
                PreparedStatement certInsert = session.prepare("insert into certs (dn ,serialNumber,certStart, certEnd, issuerDn) values (?,?,?,?,?)");
                PreparedStatement certSel = session.prepare("select serialNumber from  certs where serialNumber=?");


                HostData[] hostData = mapper.readValue(f, HostData[].class);
                for (HostData x:hostData){
                    whoDoneIt=x;
                    List<CertData> certData = x.getCertData();
                    try {
                        String json;
                        //json=mapper.writeValueAsString(x);
                        String certDn=x.isSsl() ? (certData.get(0).getDn()):null;
                        BoundStatement bound = hostInsert.bind(x.getIp(), x.getPort(), x.getName(),certDn,x.getCipherSuite());
                        session.execute(bound);
                        for (CertData cd:certData){
                            BoundStatement selBound = certSel.bind(cd.getSerialNumber());
                            ResultSet certRs = session.execute(selBound);
                            boolean alredayThere=certRs.iterator().hasNext();
                            if (!alredayThere) {
                                BoundStatement certBoundStm = certInsert.bind(cd.getDn(), cd.getSerialNumber(), cd.getStart(), cd.getEnd(), cd.getIssureDN());
                                session.execute(certBoundStm);
                            }
                        }
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
                if (whoDoneIt != null) {
                    System.out.println(whoDoneIt.getName());
                }
                e.printStackTrace();
            }





        } finally {
            // Close the cluster after we’re done with it. This will also close any session that was created from this
            // cluster.
            // This step is important because it frees underlying resources (TCP connections, thread pools...). In a
            // real application, you would typically do this at shutdown (for example, when undeploying your webapp).
            if (cluster != null)
                cluster.close();
        }
    }

    public static void mkTableIfNeeded(Session session){

        //session.execute("DROP TABLE IF EXISTS hostdata;");
        session.execute("create table IF NOT EXISTS hostdata (ip text, port int, name text, certdn text, primary key(ip,port));");
        session.execute("create table IF NOT EXISTS rawhostdata (ip text, port int,  data text, primary key(ip,port));");
        session.execute("create table IF NOT EXISTS certs (dn text,serialNumber text,  cipherSuite text,  certStart timestamp, certEnd timestamp, issuerDn text,  primary key(serialNumber));");

    }
}
