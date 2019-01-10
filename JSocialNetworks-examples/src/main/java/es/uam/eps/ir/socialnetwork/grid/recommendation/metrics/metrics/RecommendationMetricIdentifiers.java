/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics;

/**
 * Identifiers for the different recommendation metrics
 * available in the library
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommendationMetricIdentifiers 
{
    // Accuracy
    public final static String PRECISION = "P";
    public final static String RECALL = "R";
    public final static String NDCG = "nDCG";
    public final static String RR = "RR";
    public final static String ERR = "ERR";
    public final static String AVERPREC = "AP";
    public final static String KCALL = "KCall";
    public final static String COVER = "Coverage";
    // Novelty
    public final static String EFD = "EFD";
    public final static String EPC = "EPC";
    public final static String EPD = "EPD";
    // Diversity
    public final static String EILD = "EILD";
    public final static String MODEILD = "ModEILD";
    public final static String ALPHANDCG = "Alpha nDCG";
    public final static String ERRIA = "ERRIA";
    public final static String SRECALL = "SRecall";
    // Sales diversity
    public final static String AGGRDIV = "Aggregate Diversity";
    public final static String EIUDC = "EIUDC";
    public final static String EIURD = "EIURD";
    public final static String ENTROPY = "Entropy";
    public final static String GINI = "Gini";
    public final static String GINISIMPSON = "Gini-Simpson";
    public final static String IUD = "IUD";
    

   
    
    /**
     * Prints the list of available relevance models
     */
    public static void printSelectionMechanismList()
    {
        System.out.println("Recommendation metrics");
        // Accuracy metrics
        System.out.println("\tAccuracy");
        System.out.println("\t\t" + PRECISION);
        System.out.println("\t\t" + RECALL);
        System.out.println("\t\t" + NDCG);
        System.out.println("\t\t" + RR);
        System.out.println("\t\t" + ERR);
        System.out.println("\t\t" + AVERPREC);
        System.out.println("\t\t" + KCALL);
        System.out.println("\t\t" + COVER);
        // Novelty metrics
        System.out.println("\tNovelty");
        System.out.println("\t\t" + EFD);
        System.out.println("\t\t" + EPC);
        System.out.println("\t\t" + EPD);
        // Diversity metrics
        System.out.println("\tDiversity");
        System.out.println("\t\t" + EILD);
        System.out.println("\t\t" + MODEILD);
        System.out.println("\t\t" + ALPHANDCG);
        System.out.println("\t\t" + ERRIA);
        System.out.println("\t\t" + SRECALL);
        // Sales diversity
        System.out.println("\tSales Diversity");
        System.out.println("\t\t" + AGGRDIV);
        System.out.println("\t\t" + EIUDC);
        System.out.println("\t\t" + EIURD);
        System.out.println("\t\t" + ENTROPY);
        System.out.println("\t\t" + GINI);
        System.out.println("\t\t" + GINISIMPSON);
        System.out.println("\t\t" + IUD);
        
    }
}
