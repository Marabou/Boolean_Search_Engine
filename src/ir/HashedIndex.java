/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
            index.get(token).add(new PostingsEntry(docID, 0)); //TODO 0 is temporary!!
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
        switch (queryType) {
            case 0: //Intersection query
                return intersectionQuery(query, queryType, rankingType, structureType); 
            case 1: //Phrase query
                return phraseQuery(query, queryType, rankingType, structureType); 
            case 2: //Ranked query
                return rankedQuery(query, queryType, rankingType, structureType); 
            default: 
                return null; 
        }
    }

    
    /**
     *  Performs intersection query on several words
     */
    private PostingsList intersectionQuery(Query query, int queryType, int rankingType, int structureType){
        if(query.terms.size() == 1){
            return index.get(query.terms.getFirst());
        }
        else{
            return intersectionSearch(query.terms); 
        }
    }

    /**
     * 
     */
    private PostingsList phraseQuery(Query query, int queryType, int rankingType, int structureType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PostingsList rankedQuery(Query query, int queryType, int rankingType, int structureType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PostingsList intersectionSearch(LinkedList<String> wordsInQuery) {
        PostingsList postings = new PostingsList(); 
        int[] currentPostings = new int[wordsInQuery.size()]; 
        ArrayList<Iterator> postingsIterators = new ArrayList<Iterator>(); 
        
        //Get iterators for each postingslist for each term
        int i = 0; 
        for(String searchWord: wordsInQuery){
            postingsIterators.add(i, index.get(searchWord).getIterator());
            PostingsEntry tmpPostingsEntry = (PostingsEntry) postingsIterators.get(i).next();
            currentPostings[i] = tmpPostingsEntry.docID; 
            i++; 
        }
        
        //Intersection algorithms
        //Keep iterating until iterators reach end of list
        boolean done = false; 
        while(!done){
            int minIndex = 0; 
            for(int j = 1; j < currentPostings.length; j++){
                if(currentPostings[j] < currentPostings[minIndex]){
                    minIndex = j; 
                }
            }
            //postingsIterators.get(minIndex).next(); 
            
            //Check if all are alike. If yes, add to postings. Else do nothing and continue
            boolean match = true; 
            for(int k = 1; k < currentPostings.length; k ++){
                if(currentPostings[k] != currentPostings[0]){
                    match = false; 
                    break; 
                }
            }
            if(match) {
                postings.add(new PostingsEntry(currentPostings[0], 0));
            }
            
            
            if(postingsIterators.get(minIndex).hasNext()){
                PostingsEntry tmpPostingsEntry = (PostingsEntry) postingsIterators.get(minIndex).next();
                currentPostings[minIndex] = tmpPostingsEntry.docID; 
            }
            else done = true; 
        }
        return postings; 
    }
    
        
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
