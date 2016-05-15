package com.esereno.javalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentUriTemplate;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.io.StringHandle;


/**
 * 
 */
public class Multiinsert {

    
    int poolSize = 200;
    ExecutorService pool;
	DatabaseClient client;

    public Multiinsert (String host, int port, String db, String un, String pw) {

        pool = java.util.concurrent.Executors.newFixedThreadPool(poolSize);

		client = DatabaseClientFactory.newClient (host, port, db, un, pw, Authentication.DIGEST);
    }

    public void insertDoc (String doc) {
        pool.execute (new inserter (client, doc));
    }

    public void shutdown () {
        System.err.print ("shutting down pool, wait to drain . . . \n");
        pool.shutdown();
		// release the client
        while (true) { 
            if (pool.isTerminated ()) {
                client.release ();
                break;
            } else {
                System.err.print (" .");
                try { Thread.sleep (1000); } catch (Exception e) { }
            }
        }
        System.err.print ("\n");
    }


    private class inserter implements Runnable {

        private DatabaseClient dbClient = null;
        private String string = null;

        private inserter (DatabaseClient _dbClient, String _string) {
             dbClient = _dbClient;
             string = _string;
        }

        private void insertDocument () {
            // create a manager for XML documents
            XMLDocumentManager docMgr = dbClient.newXMLDocumentManager();
            docMgr.stopLogging();

            // create a uri template.  This one says "use an XML extension to generate URIs"
            DocumentUriTemplate uriTemplate = docMgr.newDocumentUriTemplate("xml");
            
            // create a handle on the content
            StringHandle handle = new StringHandle (string);

            // write the document content, returning a document descriptor.
            DocumentDescriptor documentDescriptor = docMgr.create(uriTemplate, handle);
        }

        public void run () {
            System.out.print (string);
            insertDocument ();
        }
    }

	public static void main(String[] args) throws IOException, InterruptedException {

        Multiinsert mi = new Multiinsert ("localhost", 8000, "Documents", "admin", "admin");

        System.err.println ("<");

		for (int i = 0; i < 300; i++) {
		    String s = "<doc>" + i + "</doc>";
            mi.insertDoc (s);
        }

        mi.shutdown ();
	}

}

