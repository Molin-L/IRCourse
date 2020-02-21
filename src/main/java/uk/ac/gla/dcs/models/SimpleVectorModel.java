package uk.ac.gla.dcs.models;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** You should use this sample class to implement a Simple TF*IDF weighting model for Exercise 1
  * of the exercise. You can tell Terrier to use your weighting model by specifying the 
  * -w commandline option, or the property trec.model=uk.ac.gla.dcs.models.MyWeightingModel.
  * NB: There is a corresponding unit test that you should also complete to test your model.
  * @author Molin Liu
  */

public class SimpleVectorModel extends WeightingModel
{
	private static final long serialVersionUID = 1L;

	public String getInfo() { return this.getClass().getSimpleName(); }
	public static Map<Integer, HashMap<String, Double>> tfIdfWeight;
	boolean init = false;
	
	//init() will NOT be needed in your Simple TF*IDF implementation but 
	//will be needed for your vector space model implementation
	
	void init() throws IOException, ClassNotFoundException {

        File binFile = new File("/home/ubuntu/IR/Coursework/IRcourse/res/tfIdfWeigh.csv");
        if(binFile.exists() && !binFile.isDirectory()) {
            ObjectInputStream objectInputStream =
                    null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(binFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            tfIdfWeight= (HashMap<Integer, HashMap<String, Double>>) objectInputStream.readObject();
            init = true;
            return;
        }
        binFile.createNewFile(); // if file already exists will do nothing
                
        FileWriter fw = new FileWriter(binFile, true);    
        //tfIdfWeight = new HashMap<Integer, HashMap<String, Double>>();
        
        //Preset
        int num_doc = 807775;
        double [] init_array = new double[num_doc];
        double N_doc = super.numberOfDocuments;

        Index index = Index.createIndex();
        PostingIndex<?> di = index.getDirectIndex();
        DocumentIndex doi = index.getDocumentIndex();
        Lexicon<String> lex = index.getLexicon();

        ExecutorService pool = Executors.newCachedThreadPool();
        final Map<String, Double> docFreqHashMap = new HashMap<String, Double>();

        final Queue lockQ = new LinkedList();
        for(int i = 0; i<num_doc; i++) {
            final int j = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    int docid = j; //docids are 0-based

                    IterablePosting postings = null;
                    try {
                        postings = di.getPostings(doi.getDocumentEntry(docid));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //NB: postings will be null if the document is empty
                    double magnitude = 0.0;
                    try {
                        while (postings.next() != IterablePosting.EOL){
                            double docLength = postings.getDocumentLength();
                            Map.Entry<String, LexiconEntry> lee = lex.getLexiconEntry(postings.getId());
                            double tf = postings.getFrequency();
                            double pDocFreq = 0.0;
                            if(lockQ.isEmpty()){
                                if (docFreqHashMap.containsKey(lee.getKey())) {
                                    pDocFreq = docFreqHashMap.get(lee.getKey());
                                } else {
                                    LexiconEntry le = lex.getLexiconEntry(lee.getKey());
                                    pDocFreq = le.getDocumentFrequency();
                                    lockQ.add(1);
                                    docFreqHashMap.put(lee.getKey(), pDocFreq);
                                    lockQ.remove();
                                }
                            }

                            double D_k = pDocFreq;
                            // Calculate the TF
                            double TF = Math.log(tf) / Math.log(10);
                            // Calculate the idf
                            double idf = Math.log((N_doc - D_k + 0.5) / (D_k + 0.5)) / Math.log(10);
                            double tf_idf = (1 + TF) * idf;
                            //docFreqHashMap.put(lee.getKey(), tf_idf);
                            magnitude = magnitude + Math.pow(tf_idf, 2);
                        }
                    }catch (Exception e){

                    }
                    /*
                    for (Object value : docFreqHashMap.values()) {
                        magnitude = magnitude + Math.pow(((Double) value), 2);
                    }
                    */
                    try {
                        fw.write(String.format("%d, %f\n", j, magnitude));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
            pool.execute(run);
        }
        
		//you may complete any initialisation code here.
		//you may assume access to 
		//averageDocumentLength (numberOfTokens /numberOfDocuments )
   		//keyFrequency (The frequency of the term in the query)
   		//documentFrequency (The document frequency of the term in the collection)
   		//termFrequency (The frequency of the term in the collection)
   		//numberOfDocuments (The number of documents in the collection)
		//numberOfTokens (the total length of all documents in the collection)

		//rq.getIndex() (the underlying Index)

		//rq.getMatchingQueryTerms() (the MatchingQueryTerms object, 
		//which is the system's low level representation of the query)
		
		//Terrier will only have one index loaded at the once time, so
		//to share variables between weighting model instances, use static variables
		init = true;
	}

	@Override
	public double score(Posting p) {
		if (! init) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

		double tf = p.getFrequency();
		double docLength = p.getDocumentLength();
		double N_doc = super.numberOfDocuments;
		double D_k = super.documentFrequency;
		
		// Calculate the TF
		double TF = Math.log(tf) / Math.log(10);
		// Calculate the idf
		double idf = Math.log((N_doc - D_k + 0.5) / (D_k + 0.5)) / Math.log(10);
		double tf_idf = keyFrequency*(1 + TF) * idf;
		//you should implement this method to return a score for a term occurring tf times in a document of docLength tokens.

		//you may assume access to the following member variables of the superclass:
		//averageDocumentLength (numberOfTokens /numberOfDocuments )
   		//keyFrequency (The frequency of the term in the query)
   		//documentFrequency (The document frequency of the term in the collection)
   		//termFrequency (The frequency of the term in the collection)
   		//numberOfDocuments (The number of documents in the collection)
		//numberOfTokens (the total length of all documents in the collection)
		//as well as any member variables you create   


		return tf_idf;
	}

	@Override
	public double score(double tf, double docLength) {
		throw new UnsupportedOperationException("other method is in use");
	}

}
