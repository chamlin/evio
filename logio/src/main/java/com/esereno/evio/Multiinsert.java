package com.esereno.evio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentUriTemplate;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.io.StringHandle;


/**
 * 
 */
public class Multiinsert {

    
    int documentsInserted = 0;
    ExecutorService pool;
	DatabaseClient client;

    public Multiinsert (String host, int port, String db, String un, String pw, int poolSize) {

        pool = java.util.concurrent.Executors.newFixedThreadPool(poolSize);

		client = DatabaseClientFactory.newClient (host, port, db, un, pw, Authentication.DIGEST);
    }

    public void insertDoc (String doc) {
        pool.execute (new inserter (client, doc));
        documentsInserted++;
    }

    public void insertDocs (ArrayList<Event> docs) {
        documentsInserted += docs.size ();
        ArrayList<String> docStrings = new ArrayList<String>(docs.size());
        for (Event e: docs)  docStrings.add (e.toString ());
        pool.execute (new batchInserter (client, docStrings));
    }

    public int documentsInserted () {
        return documentsInserted;
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


    private class batchInserter implements Runnable {

        private DatabaseClient dbClient = null;
        private ArrayList<String> strings = null;

        private batchInserter (DatabaseClient _dbClient, ArrayList<String> _strings) {
             dbClient = _dbClient;
             strings = _strings;
        }

        private void insertDocuments () {
            // create a manager for XML documents
            XMLDocumentManager docMgr = dbClient.newXMLDocumentManager();
            docMgr.stopLogging();

            // create a uri template.  This one says "use an XML extension to generate URIs"
            DocumentUriTemplate uriTemplate = docMgr.newDocumentUriTemplate("xml");

            DocumentWriteSet batch = docMgr.newWriteSet ();

            for (String s: strings)  {
                batch.add ("/jalopar/data/" + UUID.randomUUID().toString (),  new StringHandle (s));
            }

            docMgr.write (batch);
        }

        public void run () {
            System.out.print (strings);
            insertDocuments ();
        }
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

        Multiinsert mi = new Multiinsert ("localhost", 8000, "Documents", "admin", "admin", 10);

        System.err.println ("<");

		for (int i = 0; i < 300; i++) {
		    String s = "<doc>" + i + "</doc>";
            mi.insertDoc (s);
        }

        mi.shutdown ();
	}

}

