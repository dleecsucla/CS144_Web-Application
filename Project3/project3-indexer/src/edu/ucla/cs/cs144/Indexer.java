package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    /** Creates a new instance of Indexer */
    public Indexer() {
    }
    
    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean create) throws IOException {
        if (indexWriter == null) {
            Directory indexDir = FSDirectory.open(new File("/var/lib/lucene/index"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            indexWriter = new IndexWriter(indexDir, config);
        }
        return indexWriter;
    } 

    public void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }

  //  public void indexItem(String itemID, String name, 
   //             String category, String description) throws IOException {

        
  //  }   



    public void rebuildIndexes() {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
  try {
      conn = DbManager.getConnection(true);
  } catch (SQLException ex) {
      System.out.println(ex);
  }


  /*
   * Add your code here to retrieve Items using the connection
   * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
   * 
   */
    try{
        PreparedStatement preparedRetrieveData = conn.prepareStatement
        (
                
            /* select a table of itemID, name, category list, description from Item */
         "SELECT I.ItemID, Name, Category, Description " +
         "FROM Item I, " +
         "(SELECT ItemID, GROUP_CONCAT(DISTINCT Category SEPARATOR ' ') AS Category " +
         "FROM Category GROUP BY ItemID) AS IC " +
         "WHERE I.ItemID = IC.ItemID;"

        );
        
        ResultSet rs = preparedRetrieveData.executeQuery();
            
        

        String name, description, category;
        String itemID;

        /* create Lucene IndexWriter */
        getIndexWriter(true);


        while (rs.next()) {
            itemID = rs.getString("ItemID");
            name = rs.getString("Name");
            category = rs.getString("Category");
            description = rs.getString("Description");
            if(description == null)
                description = "";
            IndexWriter writer = getIndexWriter(false);
            Document doc = new Document();
            doc.add(new StringField("ItemID", itemID, Field.Store.YES));
            doc.add(new StringField("Name", name, Field.Store.YES));
            doc.add(new StringField("Category", category, Field.Store.YES));
            doc.add(new StringField("Description", description, Field.Store.YES));
            String fullSearchableText = itemID + " " + name + " " + category + " " + description;
            doc.add(new TextField("content", fullSearchableText, Field.Store.NO));
            writer.addDocument(doc);
            // indexItem(itemID, name, category, description);
        }


        closeIndexWriter();

        }
        catch (SQLException | IOException ex){
            System.out.println(ex);
        };

        // close the database connection
  try {
      conn.close();
  } catch (SQLException ex) {
      System.out.println(ex);
  }
    }    

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}