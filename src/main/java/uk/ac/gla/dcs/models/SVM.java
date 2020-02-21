package uk.ac.gla.dcs.models;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.postings.Posting;
import java.util.*;
/** You should use this sample class to implement a Simple TF*IDF weighting model for Exercise 1
  * of the exercise. You can tell Terrier to use your weighting model by specifying the 
  * -w commandline option, or the property trec.model=uk.ac.gla.dcs.models.MyWeightingModel.
  * NB: There is a corresponding unit test that you should also complete to test your model.
  * @author Molin Liu
  */
private static class CountDocOcc implements Callable<Response> {

}

public class SimpleVectorModel extends WeightingModel
{
	private static final long serialVersionUID = 1L;

	public String getInfo() { return this.getClass().getSimpleName(); }
	
	boolean init = false;
	
	//init() will NOT be needed in your Simple TF*IDF implementation but 
	//will be needed for your vector space model implementation
	
	void init() {

        public static Map<String, Vector<Double>> vectorHashMap = new HashMap<String, Vector<Double>>()
        int num_doc = 807775;
        double [] init_array = new double[num_doc];
        for(int i = 0; i<num_doc; i++){
            Index index = Index.createIndex();
            PostingIndex<?> di = index.getDirectIndex();
            DocumentIndex doi = index.getDocumentIndex();
            Lexicon<String> lex = index.getLexicon();
            int docid = i; //docids are 0-based
            IterablePosting postings = di.getPostings(doi.getDocumentEntry(docid));
            //NB: postings will be null if the document is empty
            while (postings.next() != IterablePosting.EOL) {
                Map.Entry<String,LexiconEntry> lee = lex.getLexiconEntry(postings.getId());

                if(vectorHashMap.containKey(lee.getKey())){
                    temp_vector = new Vector<Double>(Array.asList(init_array));
                    temp_vector.set(i, postings.getFrequency());
                    vectorHashMap.put(lee.getKey(), temp_vector);
                }else{
                    temp_vector = vectorHashMap.get(lee.getKey());
                    temp_vector.set(i, postings.getFrequency());
                    vectorHashMap.replace(lee.getKey(), temp_vector);
                }
                
        }
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
		if (! init)
			init();

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
