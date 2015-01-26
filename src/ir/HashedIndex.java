/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//Check if Postingslist is null
        if(index.get(token) == null){
            index.put(token, new PostingsList()); 
        }
        else{
            index.get(token).add(new PostingsEntry(docID, 0)); //TODO 0 is temporary!!
        }
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	Set<String> dictionary = index.keySet(); 
        Iterator wordsIterator = dictionary.iterator(); 
        
        return wordsIterator;
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	return index.get(query.terms.getFirst());
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
