/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.partition;

/**
 * Identifiers for the different contact recommendation algorithms available in 
 * the library
 * @author Javier Sanz-Cruzado Puig
 */
public class PartitionAlgorithmIdentifiers 
{
    // Random selection
    public final static String RANDOM = "Random";
    public final static String POPUNBIASEDTRAINRANDOM = "Pop. Unbiased Train Random";
    public final static String POPUNBIASEDTESTRANDOM = "Pop. Unbiased Test Random";
    public final static String COUNTRANDOM = "Count Random";
    
    // Weights selection
    public final static String PERCENTAGEWEIGHTS = "Percentage Weights";
    public final static String THRESHOLDWEIGHTS = "Threshold Weights";
    public final static String POPUNBIASEDTRAINWEIGHTS = "Pop. Unbiased Train Weights";
    public final static String POPUNBIASEDTESTWEIGHTS = "Pop. Unbiased Test Weights";
       
    /**
     * Prints the list of available algorithms
     */
    public static void printPartitionAlgorithmList()
    {
        System.out.println("Random algorithms:");
        System.out.println("\t" + RANDOM);
        System.out.println("\t" + POPUNBIASEDTRAINRANDOM);
        System.out.println("\t" + POPUNBIASEDTESTRANDOM);
        System.out.println("\t" + COUNTRANDOM);
        System.out.println("");
        
        System.out.println("Weight selection algorithms:");
        System.out.println("\t" + PERCENTAGEWEIGHTS);
        System.out.println("\t" + THRESHOLDWEIGHTS);
        System.out.println("\t" + POPUNBIASEDTRAINWEIGHTS);
        System.out.println("\t" + POPUNBIASEDTESTWEIGHTS);
        System.out.println("");
        
    }
}
