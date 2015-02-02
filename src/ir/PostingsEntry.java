/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.HashSet;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    HashSet<Integer> positionsInDoc; 

    public PostingsEntry(int docID, int score){
        this.docID = docID; 
        this.score = score; 
        this.positionsInDoc = new HashSet<>();
    }
    
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }
    
    /**
     * Adds a position to the positions set
     * @param position 
     */
    public void addOffset(int position){
        positionsInDoc.add(position); 
    }
    
    /**
     * Returns true if term is at given position
     * @param position
     * @return 
     */
    public boolean isAtPosition(int position){
        return positionsInDoc.contains(position); 
    }
    
    public HashSet<Integer> getOffsets(){
        return positionsInDoc; 
    }
    
}

    
