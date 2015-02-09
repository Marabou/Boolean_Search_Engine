/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    private HashMap<String,String> discIndex = new HashMap<String,String>(); //<word, filepath>
    private HashMap<String,ObjectOutputStream> discIndexStreams = new HashMap<String,ObjectOutputStream>(); //<word, filepath>
    
    //Where to store the index
    String indexFilePath = "index\\"; 
    String readIndexPath = "index\\"; 
    
    //true if index should be written to disc
    boolean writeToDisc = false;  
    boolean readIndex = false; 
    
    //Used for indexing
    int currentDoc; 
    HashMap<String,PostingsEntry> entriesBuffer = new HashMap<String, PostingsEntry>(); 
    HashSet<String> newFileWords = new HashSet<String>(); 
    int numberOfDocs;
    boolean lastDocument = false; 


    
    /**
     * index = path to where index is saved
     * 
     * @param index
     * @param writeToDisc 
     */
    public HashedIndex(String indexPath, boolean writeToDisc, String readIndexPath, boolean readIndex){
        this.writeToDisc = writeToDisc;
        this.readIndex = readIndex; 
        if(indexPath != null) {
            indexFilePath = this.readIndexPath + indexPath + "\\";
        } 
        if(readIndexPath != null) {
            this.readIndexPath = this.readIndexPath + readIndexPath + "\\";
        } 
    }
    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        if(writeToDisc){
            try { 
                insertDisc(token, docID, offset);
            } catch (IOException ex) {
                Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            if(index.get(token) == null){ 
                index.put(token, new PostingsList()); 
                PostingsEntry tmpPosting = new PostingsEntry(docID, 0); //Score is 0 for now 
                tmpPosting.addOffset(offset);
                index.get(token).add(tmpPosting);
                //System.out.println("doc: " + docID + " , token: " + token + ", offset: " +  offset);

            }
            else{
                if(index.get(token).get(index.get(token).size()-1).docID != docID){ //if it is a new doc, create new posting
                    PostingsEntry tmpPosting = new PostingsEntry(docID, 0); //Score is 0 for now
                    tmpPosting.addOffset(offset);
                    index.get(token).add(tmpPosting);
                    //System.out.println("doc: " + docID + " , token(IN): " + token + ", offset: " +  offset); 
                }
                else{
                    index.get(token).get(index.get(token).size()-1).addOffset(offset);
                    //System.out.println("doc(IN): " + docID + " , token(IN): " + token + ", offset: " +  offset); 
                }

            }
        }
    }
    
    /**
     * Insert method when not storing the entire index in main memory. 
     * @param token
     * @param docID
     * @param offset 
     */
    public void insertDisc( String token, int docID, int offset ) throws IOException, FileNotFoundException, ClassNotFoundException{
        if(token.length() < 30){        //Cuz I can! 
            if(docID != currentDoc){
                flushEntries(); 
                currentDoc = docID; 
            }
            if(discIndexStreams.get(token) == null){
                discIndexStreams.put(token, new ObjectOutputStream(new FileOutputStream(indexFilePath+"_"+token)));
                PostingsEntry tmpPosting = new PostingsEntry(docID, 0);
                tmpPosting.addOffset(offset);
                entriesBuffer.put(token, tmpPosting); 
                newFileWords.add(token); 
            }
            else{
                if(!entriesBuffer.containsKey(token)){
                    PostingsEntry tmpPosting = new PostingsEntry(docID, 0);
                    tmpPosting.addOffset(offset);
                    entriesBuffer.put(token, tmpPosting); 
                }
                else{
                    entriesBuffer.get(token).addOffset(offset);
                }
            }
        }
    }
    
    /**
     * Writes the postingentries to disc for the current file
     */
    public void flushEntries() throws FileNotFoundException, IOException, ClassNotFoundException{
        for(String key: entriesBuffer.keySet()){
            discIndexStreams.get(key).writeObject(entriesBuffer.get(key));
            discIndexStreams.get(key).flush();
        }
        entriesBuffer = new HashMap<String, PostingsEntry>(); 
    }
    
    @Override
    public void finalFlushAndCloseStreams() {
        try { 
            flushEntries();
        } catch (IOException ex) {
            Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(String key: discIndexStreams.keySet()){
            try { 
                discIndexStreams.get(key).flush(); 
                discIndexStreams.get(key).close();
                
            } catch (IOException ex) {
                Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        if(query.terms.size() == 1){
            if(writeToDisc){
                if(discIndexStreams.get(query.terms.getFirst()) != null){
                    PostingsList tmpList = new PostingsList(); 
                    try { 
                        FileInputStream fIn = new FileInputStream(indexFilePath+"_"+(query.terms.getFirst()));
                        ObjectInputStream oIn = new ObjectInputStream(fIn); 
                        PostingsEntry tmpEntry = (PostingsEntry) oIn.readObject();
                        while(tmpEntry != null){
                            tmpList.add(tmpEntry);
                            tmpEntry = (PostingsEntry) oIn.readObject();
                        }
                        
                        return tmpList; 
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else
                    return new PostingsList(); 
            }
            else{
                if(index.get(query.terms.getFirst()) != null)
                    return index.get(query.terms.getFirst());
                else
                    return new PostingsList(); 
            }
        }
        else if(query.terms.size() > 1)
        {
            for(String term: query.terms){
                if(writeToDisc){
                    if(discIndexStreams.get(term) == null){
                        return new PostingsList(); 
                    }
                }
                else{
                    if(index.get(term) == null){
                        return new PostingsList(); 
                    }
                }
            }
            switch (queryType) {
                case 0: {
                try {
                    //Intersection query
                    return intersectionQuery(query, queryType, rankingType, structureType);
                } catch (IOException ex) {
                    Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(HashedIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
                case 1: //Phrase query
                    return phraseQuery(query, queryType, rankingType, structureType); 
                case 2: //Ranked query
                    return rankedQuery(query, queryType, rankingType, structureType); 
                default: 
                    return null; 
            }
        }
        else
            return new PostingsList();
        
        //Because Netbeans is forcing me :E 
        return null;
    }

    
    /**
     *  Performs intersection query on several words
     */
    private PostingsList intersectionQuery(Query query, int queryType, int rankingType, int structureType) throws IOException, FileNotFoundException, ClassNotFoundException{
        return intersectionSearch(query.terms); 
    }

    /**
     * 
     */
    private PostingsList phraseQuery(Query query, int queryType, int rankingType, int structureType) {
        return phraseSearch(query.terms); 
    }

    private PostingsList rankedQuery(Query query, int queryType, int rankingType, int structureType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PostingsList intersectionSearch(LinkedList<String> wordsInQuery) throws FileNotFoundException, IOException, ClassNotFoundException {
        PostingsList postings = new PostingsList(); 
        int[] currentPostings = new int[wordsInQuery.size()]; 
        ArrayList<Iterator> postingsIterators = new ArrayList<Iterator>(); 
        ArrayList<LinkedList<PostingsEntry>> postingsFromDisc = null; 
        if(writeToDisc){
            postingsFromDisc = new ArrayList<LinkedList<PostingsEntry>>(); 
        }
        
        //Get iterators for each postingslist for each term
        int i = 0; 
        for(String searchWord: wordsInQuery){
            if(writeToDisc){
                LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
                //FileInputStream fIn = new FileInputStream(discIndex.get(searchWord)); 
                FileInputStream fIn = new FileInputStream(indexFilePath+"_"+searchWord);
                //System.out.println("checking for file "+indexFilePath+"_"+searchWord); 
                
                ObjectInputStream oIn = new ObjectInputStream(fIn); 
                PostingsEntry tmp = (PostingsEntry) oIn.readObject(); 
                try{
                    while(tmp != null){
                        list.addLast(tmp); 
                        tmp = (PostingsEntry) oIn.readObject(); 
                    }
                }
                catch(Exception e){}
                postingsFromDisc.add(i,list); 
                postingsIterators.add(i, postingsFromDisc.get(i).iterator()); 
                
                //DEBUGZ0R
                /*System.out.println("List size for " + searchWord + " " + list.size());
                for(int k = 0; k < list.size(); k++){
                    System.out.println("Document ID " + list.get(k).docID + " has word " + searchWord);
                }*/
            }
            else{
                postingsIterators.add(i, index.get(searchWord).getIterator());
            }
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
    
    private PostingsList phraseSearch(LinkedList<String> wordsInQuery) {
        PostingsList postings = new PostingsList(); 
        PostingsEntry[] currentPostings = new PostingsEntry[wordsInQuery.size()]; 
        ArrayList<Iterator> postingsIterators = new ArrayList<Iterator>(); 
        
        //Get iterators for each postingslist for each term
        int i = 0; 
        for(String searchWord: wordsInQuery){
            postingsIterators.add(i, index.get(searchWord).getIterator());
            PostingsEntry tmpPostingsEntry = (PostingsEntry) postingsIterators.get(i).next();
            currentPostings[i] = tmpPostingsEntry; 
            i++; 
        }
        
        //Intersection algorithms
        //Keep iterating until iterators reach end of list
        boolean done = false; 
        while(!done){
            int minIndex = 0; 
            for(int j = 1; j < currentPostings.length; j++){
                if(currentPostings[j].docID < currentPostings[minIndex].docID){
                    minIndex = j; 
                }
            }
            
            //Check if all are alike. If yes, add to postings. Else do nothing and continue
            boolean match = true; 
            for(int k = 1; k < currentPostings.length; k ++){
                if(currentPostings[k].docID != currentPostings[0].docID){
                    match = false; 
                    break; 
                }
            }
            if(match) {
                //System.out.println("Match on doc: " + currentPostings[0].docID); 
                //Check if phrase occurs by going through iterators and checking offsets
                boolean phraseMatch = false; 
                HashSet<Integer> offsetsFirstWord = currentPostings[0].getOffsets(); 
                
                /*System.out.println("Offsets for first word:");
                for(int os: offsetsFirstWord){
                    System.out.print(os + ", "); 
                }
                System.out.println();*/
                
                for(int offset: offsetsFirstWord){
                    for(int k = 1; k < wordsInQuery.size(); k++){
                        //System.out.println("Is word " + wordsInQuery.get(k) + " at pos " + (offset+k)); 
                        if(currentPostings[k].isAtPosition(offset+k)){
                            phraseMatch = true;
                        }
                    }
                    if(phraseMatch) break; 
                }
                //System.out.println("PhraseMatch is "+ phraseMatch); 
                if(phraseMatch)postings.add(new PostingsEntry(currentPostings[0].docID, 0));
            }
            if(postingsIterators.get(minIndex).hasNext()){
                PostingsEntry tmpPostingsEntry = (PostingsEntry) postingsIterators.get(minIndex).next();
                currentPostings[minIndex] = tmpPostingsEntry; 
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
    
    /**
     * To string of index
     * @return 
     */
    public String toString(){
        StringBuilder sb = new StringBuilder(); 
        for(String key: index.keySet()){
            sb.append(key + ": "); 
            for(PostingsEntry pe: index.get(key).getList()){
                sb.append(pe.docID + " [ ");
                for(int os: pe.getOffsets()){
                    sb.append(os + ","); 
                }
                sb.append(" ], "); 
            }
            sb.append("\n"); 
        }
        return sb.toString(); 
    }

    @Override
    public void setNumberOfDocs(int n) {
        this.numberOfDocs = n; 
    }
}
