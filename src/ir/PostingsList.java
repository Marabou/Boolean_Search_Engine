/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;
import java.util.Iterator;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    public static final long serialVersionUID = 1L;
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }
    
    /**
     * Returns iterator for postingslist
     * @return 
     */
    public Iterator getIterator(){
        return list.listIterator();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }
    
    /**
     * Adds a posting as docID to the list 
     **/
    public void add(PostingsEntry p){
        //Only add if not already in list
        if(list.size() == 0 || list.getLast().docID != p.docID) {
            list.add(p); 
        } 
    }
    
    public LinkedList<PostingsEntry> getList(){
        return list; 
    }
}
	

			   
