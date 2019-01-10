/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation;

/**
 * Identifiers for the different contact recommendation algorithms available in 
 * the library
 * @author Javier Sanz-Cruzado Puig
 */
public class AlgorithmIdentifiers 
{
    // IR algorithms
    public final static String BIR = "BIR";
    public final static String BIRTERM = "BIR Term Based";
    public final static String BIRDOC = "BIR Doc Based";
    public final static String BM25 = "BM25";
    public final static String BM25TERM = "BM25 Term Based";
    public final static String BM25DOC = "BM25 Doc Based";
    public final static String EBM25 = "ExtremeBM25";
    public final static String EBM25TERM = "ExtremeBM25 Term Based";
    public final static String EBM25DOC = "ExtremeBM25 Doc Based";
    public final static String QLJM = "QLJM";
    public final static String QLJMTERM = "QLJM Term Based";
    public final static String QLJMDOC = "QLJM Doc Based";
    public final static String QLD = "QLD";
    public final static String QLDTERM = "QLD Term Based";
    public final static String QLDDOC = "QLD Doc Based";
    public final static String QLL = "QLL";
    public final static String QLLTERM = "QLL Term Based";
    public final static String QLLDOC = "QLL Doc Based";
    public final static String TFIDF = "TFIDF";
    public final static String TFIDFTERM = "TFIDF Term Based";
    public final static String TFIDFDOC = "TFIDF Doc Based";
    
    // Bipartite algorithms
    public final static String AVGCOS = "Average Cosine Similarity";
    public final static String CENTROIDCOS = "Centroid Cosine Similarity";
    public final static String MAXCOS = "Maximum Cosine Similarity";
    
    // Link Prediction: Degree-based
    public final static String ADAMIC = "Adamic";
    public final static String ADAMICTERM = "Adamic Term Based";
    public final static String ADAMICDOC = "Adamic Doc Based";
    public final static String DESTDEGREE = "Destination Degree";
    public final static String HDI = "Hub Depressed Index";
    public final static String HDITERM = "Hub Depressed Index Term Based";
    public final static String HDIDOC = "Hub Depressed Index Doc Based";
    public final static String HPI = "Hub Promoted Index";
    public final static String HPITERM = "Hub Promoted Index Term Based";
    public final static String HPIDOC = "Hub Promoted Index Doc Based";
    public final static String JACCARD = "Jaccard";
    public final static String JACCARDTERM = "Jaccard Term Based";
    public final static String JACCARDDOC = "Jaccard Doc Based";
    public final static String LOCALLHN = "Local LHN Index";
    public final static String LOCALLHNTERM = "Local LHN Index Term Based";
    public final static String LOCALLHNDOC = "Local LHN Index Doc Based";
    public final static String MCN = "Most Common Neighbors";
    public final static String MCNTERM = "Most Common Neighbors Term Based";
    public final static String MCNTERMAUX = "Most Common Neighbors Term Based Aux";
    public final static String MCNDOC = "Most Common Neighbors Doc Based";
    public final static String PREFATTACH = "Preferential Attachment";
    public final static String RECIPROCAL = "Reciprocal";
    public final static String RESALLOC = "Resource Allocation";
    public final static String RESALLOCTERM = "Resource Allocation Term Based";
    public final static String RESALLOCDOC = "Resource Allocation Doc Based";
    public final static String SALTON = "Salton";
    public final static String SALTONTERM = "Salton Term Based";
    public final static String SALTONDOC = "Salton Doc Based";
    public final static String SORENSEN = "Sorensen";
    public final static String SORENSENTERM = "Sorensen Term Based";
    public final static String SORENSENDOC = "Sorensen Doc Based";
    public final static String NONRECPREFATTACH = "Non Reciprocal Preferential Attachment";
    public final static String DIST2POP = "Distance Two Popularity";
    public final static String DIST2POPTERM = "Distance Two Popularity Term Based";
    public final static String DIST2POPDOC = "Distance Two Popularity Doc Based";
    
    // Link Prediction: Distance
    public final static String DISTANCE = "Distance";
    public final static String KATZ = "Katz";
    public final static String GLOBALLHN = "LHN Index 2";
    public final static String LPI = "Local Path Index";
    
    // Link Prediction: Random Walks
    public final static String COMMUTETIME = "Commute Time";
    public final static String HITS = "HITS";
    public final static String HITTINGTIME = "Hitting Time";
    public final static String PAGERANK = "PageRank";
    public final static String PERSONALIZEDHITS = "Personalized HITS";
    public final static String PERSONALIZEDPAGERANK = "Personalized PageRank";
    public final static String PERSONALIZEDSALSA = "Personalized SALSA";
    public final static String PROPFLOW = "PropFlow";
    public final static String PUREPERSPAGERANK = "Pure Personalized PageRank";
    public final static String SALSA = "SALSA";
    public final static String SIMRANK = "SimRank";
    
    // Matrix factorization
    public final static String PMFBASIC = "PMF Basic";
    public final static String PMFSIGMOID = "PMF Sigmoid";
    public final static String HKV = "HKV";
    public final static String PZT = "PZT";
    
    // Classic recommendation
    public final static String UB = "User Based CF";
    public final static String IB = "Item Based CF";
    public final static String CLASSICIB = "Classic Item Based CF";
    public final static String SOCIALCOLLAB = "SocialCollab";
    
    // Content-based
    public final static String HANNON = "Hannon";
    public final static String SIMPLEHANNON = "SimpleHannon";
    
    // Twitter algorithms
    public final static String MONEY = "Money";
    public final static String LOVE = "Love";
    public final static String TWITTERCENTROIDCOS = "Twitter Centroid Cosine Similarity";
    public final static String TWITTERAVGCOS = "Twitter Average Cosine Similarity";
    public final static String TWITTERMAXCOS = "Twitter Maximum Cosine Similarity";
    // Baselines
    public final static String RANDOM = "Random";
    public final static String POPULARITY = "Popularity";
    
    // Complementary
    public final static String COMPLADAMIC = "Complementary Adamic";
    public final static String COMPLIB = "Complementary Classic Item Based CF";
    public final static String COMPLJACCARD = "Complementary Jaccard";
    public final static String COMPLMCN = "Complementary Most Common Neighbors";
    public final static String COMPLPAGERANK = "Complementary PageRank";
    public final static String COMPLPERSPAGERANK = "Complementary Personalized PageRank";
    public final static String COMPLPOP = "Complementary Popularity";
    public final static String COMPLPREFATTACH = "Complementary Preferential Attachment";
    public final static String COMPLRECIPROCAL = "Complementary Reciprocal";
    public final static String COMPLUB = "Complementary User Based CF";
    
    public final static String POPFRAC = "Popularity fraction";
        public final static String D2POPFRAC = "Distance Two Popularity fraction";

    /**
     * Prints the list of available algorithms
     */
    public static void printAlgorithmList()
    {
        System.out.println("IR algorithms:");
        System.out.println("\t" + BM25);
        System.out.println("\t" + EBM25);
        System.out.println("\t" + QLJM);
        System.out.println("\t" + TFIDF);
        System.out.println("");
        
        System.out.println("Bipartite algorithms:");
        System.out.println("\t" + AVGCOS);
        System.out.println("\t" + CENTROIDCOS);
        System.out.println("\t" + MAXCOS);
        System.out.println("");
        
        System.out.println("Link Prediction algorithms - Degree based:");
        System.out.println("\t" + ADAMIC);
        System.out.println("\t" + HDI);
        System.out.println("\t" + HPI);
        System.out.println("\t" + JACCARD);
        System.out.println("\t" + LOCALLHN);
        System.out.println("\t" + MCN);
        System.out.println("\t" + PREFATTACH);
        System.out.println("\t" + RECIPROCAL);
        System.out.println("\t" + RESALLOC);
        System.out.println("\t"+ SALTON);
        System.out.println("\t"+ SORENSEN);
        System.out.println("");
        
        System.out.println("Link Prediction algorithms - Distance based:");
        System.out.println("\t" + DISTANCE);
        System.out.println("\t" + KATZ);
        System.out.println("\t" + GLOBALLHN);
        System.out.println("\t" + LPI);
        System.out.println("");
        
        System.out.println("Link Prediction algorithms - Random Walk based:");
        System.out.println("\t" + COMMUTETIME);
        System.out.println("\t" + HITS);
        System.out.println("\t" + HITTINGTIME);
        System.out.println("\t" + PAGERANK);
        System.out.println("\t" + PERSONALIZEDHITS);
        System.out.println("\t" + PERSONALIZEDPAGERANK);
        System.out.println("\t" + PERSONALIZEDSALSA);
        System.out.println("\t" + PROPFLOW);
        System.out.println("\t" + PUREPERSPAGERANK);
        System.out.println("\t" + SALSA);
        System.out.println("\t" + SIMRANK);
        System.out.println("");
        
        System.out.println("Matrix factorization algorithms:");
        System.out.println("\t" + HKV);
        System.out.println("\t" + PZT);
        System.out.println("\t" + PMFBASIC);
        System.out.println("\t" + PMFSIGMOID);
        System.out.println("");
        
        System.out.println("Memory-based collaborative filtering:");
        System.out.println("\t" + UB);
        System.out.println("\t" + IB);
        System.out.println("\t" + CLASSICIB);
        System.out.println("");
        
        System.out.println("Twitter:");
        System.out.println("\t" + MONEY);
        System.out.println("\t" + LOVE);
        System.out.println("\t" + TWITTERCENTROIDCOS);
        System.out.println("\t" + TWITTERAVGCOS);
        System.out.println("\t" + TWITTERMAXCOS);
        System.out.println("");
        
        
        System.out.println("Baselines:");
        System.out.println("\t" + RANDOM);
        System.out.println("\t" + POPULARITY);
        System.out.println("");
        
        System.out.println("Complementary graph:");
        System.out.println("\t" + COMPLADAMIC);
        System.out.println("\t" + COMPLIB);
        System.out.println("\t" + COMPLJACCARD);
        System.out.println("\t" + COMPLMCN);
        System.out.println("\t" + COMPLPAGERANK);
        System.out.println("\t" + COMPLPERSPAGERANK);
        System.out.println("\t" + COMPLPREFATTACH);
        System.out.println("\t" + COMPLRECIPROCAL);
        System.out.println("\t" + COMPLUB);
        System.out.println("");
    }
}
