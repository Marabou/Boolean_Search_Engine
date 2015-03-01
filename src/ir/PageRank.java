/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import com.sun.javafx.css.CalculatedValue;
import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {     
        
	//Power iteration with c = 0.85
        //Setting initial vector to the first vector 
        double[] score = new double[numberOfDocs]; 
        score[0] = 1;
        double[] newScore = new double[numberOfDocs]; 
        double[] diffScore = new double[numberOfDocs]; 
        double c = 0.85;
        double difference = 1; 
        score[0] = 1.0;    //Starting position
        double jcElement = (1.0-c)*(1.0/numberOfDocs); 
        
        //Initial difference
        for(int i = 0; i < numberOfDocs; i++){
            diffScore[i] = Math.abs(score[i] - newScore[i]);
        }
        difference = computeMagnitude(diffScore); 

        while(difference > 0.0001){
            for(int i = 0; i < numberOfDocs; i++){
                double newProb = 0; 
                double Gij = 0; 
                for(int j = 0; j < numberOfDocs; j++){
                    if(score[j] != 0){  //Will make the term zero anyway
                        if(link.get(j) != null){
                            if(link.get(j).get(i) != null){
                                Gij = (c*1.0/((double) link.get(j).size())) + jcElement;  //c*Pij + (1-c)Jij 
                            }
                            else Gij = jcElement; 
                        }
                        else{
                            Gij = (c*1.0/((double)( numberOfDocs-1.0))) + jcElement; 
                        }
                        newProb += score[j]*Gij;
                    }
                }
                
                newScore[i] = newProb; 
                if(i%1000 == 0) System.out.println("NewProb for " +  " " + i + ": " + newProb); 
            }
            //Calculate the difference
            for(int i = 0; i < numberOfDocs; i++){
                diffScore[i] = Math.abs((score[i] - newScore[i])); 
            }
            difference = computeMagnitude(diffScore); 
            System.out.println("Difference: " + difference);
            
            score = newScore.clone();   
            
        }
        
        ArrayList<scoreObject> scoreObjects = new ArrayList<scoreObject>(); 
        for(int i = 0; i < score.length; i++){
            scoreObjects.add(i, new scoreObject(docName[i], score[i]));
        }
        
        Collections.sort(scoreObjects, Collections.reverseOrder());
        int listSize = scoreObjects.size() >= 50 ? 50 : scoreObjects.size(); 
        for(int i = 0; i < listSize; i++){
            System.out.println((i+1) + " : " + scoreObjects.get(i).name + " " + scoreObjects.get(i).score); 
        }
        
    }

    
    double computeMagnitude(double[] diffScore){
        double magnitude = 0; 
        for(int i = 0; i < diffScore.length; i++){
            magnitude += Math.pow(diffScore[i], 2.0); 
        }
        return Math.sqrt(magnitude); 
    }
    
    
    void printVector(double[] vector){
        for(int i = 0; i < vector.length; i++){
            System.out.print(vector[i] + ", ");
        }
        System.out.println(); 
    }
    
    void normalize(double[] vector){
        double sum = 0; 
        for(int i = 0; i < vector.length; i++){
            sum += vector[i]; 
        }
        double scaleFactor = 1.0/sum; 
        
        for(int i = 0; i < vector.length; i++){
            vector[i] = vector[i]*scaleFactor; 
        }
    }
    
    void prinkLinks(){
        for(Integer key: link.keySet()){
            System.out.print(key + "; ");
            for(Integer value: link.get(key).keySet()){
                System.out.print(" " + value + ", ");
            }
            System.out.println(); 
        }
    }

    
    
    void printNumberOfLinks(int docID){
        int numberOfLinks = 0; 
        for(int i: link.keySet()){
            if(link.get(i) != null){
                if(link.get(i).get(docID) != null && link.get(i).get(docID) == true) numberOfLinks++; 
            }
        }
        System.out.println("Number of links to " + docName[docID] + " is: " + numberOfLinks);
    }
    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}

/**
 * For sorting
 * @author alexn_000
 */
class scoreObject implements Comparable<scoreObject>{
    public double score; 
    public String name; 
    
    public scoreObject(String name, double score){
        this.score = score; 
        this.name = name; 
    }

    @Override
    public int compareTo(scoreObject o) {
        if(o.score == score) return 0; 
        else if(o.score < score ) return 1; 
        else return -1; 
    }
    
    
    
}
