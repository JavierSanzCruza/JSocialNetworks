/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation;

import es.uam.eps.ir.socialnetwork.grid.recommendation.twitter.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.mf.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.randomwalk.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.ir.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.distances.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.degree.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.cf.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.bipartite.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.baselines.*;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import java.util.*;
import java.util.function.Supplier;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.cf.SocialCollabGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.ir.QLDGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.ir.QLLGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.degree.DestinationDegreeGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.degree.MostCommonNeighborsDocumentBasedGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.degree.MostCommonNeighborsTermBasedGridSearch;
import java.util.function.BiFunction;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;


/**
 * Class that translates from a grid to the different contact recommendation algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class AlgorithmGridSelector<U>
{
    /**
     * Given preference data, obtains recommenders.
     * @param algorithm the name of the algorithm.
     * @param grid the parameter grid for the algorithm.
     * @param graph the training graph.
     * @param prefData the training data
     * @return the suppliers for the different algorithm variants, indexed by name.
     */
    public Map<String, Supplier<Recommender<U,U>>> getRecommenders(String algorithm, Grid grid, FastGraph<U> graph, FastPreferenceData<U,U> prefData)
    {
        AlgorithmGridSearch<U> gridsearch = this.selectGridSearch(algorithm);
        if(gridsearch != null)
            return gridsearch.grid(grid, graph, prefData);
        return null;
    }
    
    /**
     * Given preference data, obtains recommenders.
     * @param algorithm the name of the algorithm.
     * @param grid the parameter grid for the algorithm.
     * @return functions for obtaining for the different algorithm variants given the graph and preference data, indexed by name.
     */
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U,U>, Recommender<U,U>>> getRecommenders(String algorithm, Grid grid)
    {
        AlgorithmGridSearch<U> gridsearch = this.selectGridSearch(algorithm);
        if(gridsearch != null)
            return gridsearch.grid(grid);
        return null;
    }
    
    public AlgorithmGridSearch<U> selectGridSearch(String algorithm)
    {
        AlgorithmGridSearch<U> gridsearch;
        switch(algorithm)
        {
            // IR algorithms
            case BIR:
                gridsearch = new BIRGridSearch();
                break;
            case BIRDOC:
                gridsearch = new BIRDocumentBasedGridSearch();
                break;
            case BIRTERM:
                gridsearch = new BIRTermBasedGridSearch();
                break;
            case BM25:
                gridsearch = new BM25GridSearch();
                break;
            case BM25DOC:
                gridsearch = new BM25DocumentBasedGridSearch();
                break;
            case BM25TERM:
                gridsearch = new BM25TermBasedGridSearch();
                break;
            case EBM25:
                gridsearch = new ExtremeBM25GridSearch();
                break;
            case EBM25DOC:
                gridsearch = new ExtremeBM25DocumentBasedGridSearch();
                break;
            case EBM25TERM:
                gridsearch = new ExtremeBM25TermBasedGridSearch();
                break;
            case QLJM:
                gridsearch = new QLJMGridSearch();
                break;
            case QLJMDOC:
                gridsearch = new QLJMDocumentBasedGridSearch();
                break;
            case QLJMTERM:
                gridsearch = new QLJMTermBasedGridSearch();
                break;
            case QLD:
                gridsearch = new QLDGridSearch();
                break;
            case QLDDOC:
                gridsearch = new QLDDocumentBasedGridSearch();
                break;
            case QLDTERM:
                gridsearch = new QLDTermBasedGridSearch();
                break;
            case QLL:
                gridsearch = new QLLGridSearch();
                break;
            case QLLDOC:
                gridsearch = new QLLDocumentBasedGridSearch();
                break;
            case QLLTERM:
                gridsearch = new QLLTermBasedGridSearch();
                break;
            case TFIDF:
                gridsearch = new TfIdfGridSearch();
                break;
            case TFIDFDOC:
                gridsearch = new TfIdfDocumentBasedGridSearch();
                break;
            case TFIDFTERM:
                gridsearch = new TfIdfTermBasedGridSearch();
                break;

            // Bipartite
            case AVGCOS:
                gridsearch = new AverageCosineSimilarityGridSearch();
                break;
            case CENTROIDCOS:
                gridsearch = new CentroidCosineSimilarityGridSearch();
                break;
            case MAXCOS:
                gridsearch = new MaximumCosineSimilarityGridSearch();
                break;
                
            // Link prediction - degree based algorithms
            case ADAMIC:
                gridsearch = new AdamicGridSearch();
                break;
            case ADAMICDOC:
                gridsearch = new AdamicDocumentBasedGridSearch();
                break;
            case ADAMICTERM:
                gridsearch = new AdamicTermBasedGridSearch();
                break;
            case DESTDEGREE:
                gridsearch = new DestinationDegreeGridSearch();
                break;
            case HDI:
                gridsearch = new HubDepressedIndexGridSearch();
                break;
            case HDIDOC:
                gridsearch = new HubDepressedIndexDocumentBasedGridSearch();
                break;
            case HDITERM:
                gridsearch = new HubDepressedIndexTermBasedGridSearch();
                break;
            case HPI:
                gridsearch = new HubPromotedIndexGridSearch();
                break;
            case HPIDOC:
                gridsearch = new HubPromotedIndexDocumentBasedGridSearch();
                break;
            case HPITERM:
                gridsearch = new HubPromotedIndexTermBasedGridSearch();
                break;
            case JACCARD:
                gridsearch = new JaccardGridSearch();
                break;
            case JACCARDDOC:
                gridsearch = new JaccardDocumentBasedGridSearch();
                break;
            case JACCARDTERM:
                gridsearch = new JaccardTermBasedGridSearch();
                break;
            case LOCALLHN:
                gridsearch = new LocalLHNIndexGridSearch();
                break;
            case LOCALLHNDOC:
                gridsearch = new LocalLHNIndexDocumentBasedGridSearch();
                break;
            case LOCALLHNTERM:
                gridsearch = new LocalLHNIndexTermBasedGridSearch();
                break;
            case MCN:
                gridsearch = new MostCommonNeighborsGridSearch();
                break;
            case MCNTERM:
                gridsearch = new MostCommonNeighborsTermBasedGridSearch();
                break;
            case MCNDOC:
                gridsearch = new MostCommonNeighborsDocumentBasedGridSearch();
                break;
            case PREFATTACH:
                gridsearch = new PreferentialAttachmentGridSearch();
                break;
            case RECIPROCAL:
                gridsearch = new ReciprocalGridSearch();
                break;
            case RESALLOC:
                gridsearch = new ResourceAllocationGridSearch();
                break;
            case RESALLOCDOC:
                gridsearch = new ResourceAllocationDocumentBasedGridSearch();
                break;
            case RESALLOCTERM:
                gridsearch = new ResourceAllocationTermBasedGridSearch();
                break;
            case SALTON:
                gridsearch = new SaltonGridSearch();
                break;
            case SALTONDOC:
                gridsearch = new SaltonDocumentBasedGridSearch();
                break;
            case SALTONTERM:
                gridsearch = new SaltonTermBasedGridSearch();
                break;
            case SORENSEN:
                gridsearch = new SorensenGridSearch();
                break;
            case SORENSENDOC:
                gridsearch = new SorensenDocumentBasedGridSearch();
                break;
            case SORENSENTERM:
                gridsearch = new SorensenTermBasedGridSearch();
                break;
            case NONRECPREFATTACH:
                gridsearch = new NonReciprocalPreferentialAttachmentGridSearch();
                break;
                
            case DIST2POP:
                gridsearch = new DistanceTwoPopularityGridSearch();
                break;
            case DIST2POPDOC:
                gridsearch = new DistanceTwoPopularityDocumentBasedGridSearch();
                break;
            case DIST2POPTERM:
                gridsearch = new DistanceTwoPopularityTermBasedGridSearch();
                break;
                
            // Link prediction - distance based algorithms
            case DISTANCE:
                gridsearch = new DistanceGridSearch();
                break;
            case KATZ:
                gridsearch = new KatzGridSearch();
                break;
            case GLOBALLHN:
                gridsearch = new GlobalLHNIndexGridSearch();
                break;
            case LPI:
                gridsearch = new LocalPathIndexGridSearch();
                break;
                
            // Link Prediction - random walk based algorithms
            case COMMUTETIME:
                gridsearch = new CommuteTimeGridSearch();
                break;
            case HITS:
                gridsearch = new HITSGridSearch();
                break;
            case HITTINGTIME:
                gridsearch = new HittingTimeGridSearch();
                break;
            case PAGERANK:
                gridsearch = new PageRankGridSearch();
                break;
            case PERSONALIZEDHITS:
                gridsearch = new PersonalizedHITSGridSearch();
                break;
            case PERSONALIZEDPAGERANK:
                gridsearch = new PersonalizedPageRankGridSearch();
                break;
            case PERSONALIZEDSALSA:
                gridsearch = new PersonalizedSALSAGridSearch();
                break;
            case PROPFLOW:
                gridsearch = new PropFlowGridSearch();
                break;
            case PUREPERSPAGERANK:
                gridsearch = new PurePersonalizedPageRankGridSearch();
                break;
            case SALSA:
                gridsearch = new SALSAGridSearch();
                break;
            case SIMRANK:
                gridsearch = new SimRankGridSearch();
                break;
                
            case POPFRAC:
                gridsearch = new PopularityFractionGridSearch();
                break;
            case D2POPFRAC:
                gridsearch = new DistanceTwoPopularityFractionTermBasedGridSearch();
                break;
            // Matrix factorization
            case HKV:
                gridsearch = new HKVGridSearch();
                break;
            case PZT:
                gridsearch = new PZTGridSearch();
                break;
            case PMFBASIC:
                gridsearch = new PMFBasicGridSearch();
                break;
            case PMFSIGMOID:
                gridsearch = new PMFSigmoidGridSearch();
                break;
            
            // Memory-based collaborative filtering
            case UB:
                gridsearch = new UserBasedCFGridSearch();
                break;
            case IB:
                gridsearch = new ItemBasedCFGridSearch();
                break;
            case CLASSICIB:
                gridsearch = new ClassicItemBasedCFGridSearch();
                break;
            case SOCIALCOLLAB:
                gridsearch = new SocialCollabGridSearch();
                break;
                
            // Twitter algorithms
            case MONEY:
                gridsearch = new MoneyGridSearch();
                break;
            case LOVE:
                gridsearch = new LoveGridSearch();
                break;
            case TWITTERCENTROIDCOS:
                gridsearch = new TwitterAverageCosineSimilarityGridSearch();
                break;
            case TWITTERAVGCOS:
                gridsearch = new TwitterCentroidCosineSimilarityGridSearch();
                break;
            case TWITTERMAXCOS:
                gridsearch = new TwitterMaximumCosineSimilarityGridSearch();
                break;
                
            // Baseline algorithms
            case RANDOM:
                gridsearch = new RandomGridSearch();
                break;
            case POPULARITY:
                gridsearch = new PopularityGridSearch();
                break;
                
            // Default behavior
            default:
                gridsearch = null;
        }
        
        return gridsearch;
    }
}
