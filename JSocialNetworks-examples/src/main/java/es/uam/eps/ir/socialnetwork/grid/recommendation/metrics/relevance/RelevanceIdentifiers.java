/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance;

/**
 * Identifiers for the different relevance models
 * available in the library
 * @author Javier Sanz-Cruzado Puig
 */
public class RelevanceIdentifiers 
{
    public final static String BACKGROUND = "Background Binary";
    public final static String BINARY = "Binary";
    public final static String NOREL = "No Relevance";
    public final static String NDCG = "NDCG";
    public final static String ERR = "ERR";

   
    
    /**
     * Prints the list of available relevance models
     */
    public static void printRelevanceModelList()
    {
        System.out.println("Relevance Model:");
        System.out.println("\t" + BACKGROUND);
        System.out.println("\t" + BINARY);
        System.out.println("\t" + NOREL);
        System.out.println("\t" + NDCG);
        System.out.println("\t" + ERR);

        
    }
}
