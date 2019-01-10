/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental;

/**
 * Identifiers for the different recommendation metrics
 * available in the library
 * @author Javier Sanz-Cruzado Puig
 */
public class IncrementalRecommendationMetricIdentifiers 
{
    // Accuracy
    public final static String RECALL = "Incr-R";
    public final static String SYSRECALL = "Incr-SysR";
    
    /**
     * Prints the list of available relevance models
     */
    public static void printSelectionMechanismList()
    {
        System.out.println("Recommendation metrics");
        // Accuracy metrics
        System.out.println("\tAccuracy");
        System.out.println("\t\t" + RECALL);
        System.out.println("\t\t" + SYSRECALL);       
    }
}
