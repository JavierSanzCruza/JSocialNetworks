/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.local.RandomRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.local.communities.StrongTiesRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.local.communities.WeakTiesRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.edge.AverageEmbeddednessRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.edge.AverageWeaknessRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.edge.HeuristicAverageEmbeddednessRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.edge.HeuristicAverageWeaknessRerankerGridSearch;
import java.util.*;
import java.util.function.Supplier;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.ClusteringCoefficientComplementRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.ClusteringCoefficientRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.DegreeGiniComplementRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.CompleteCommunityDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.CompleteCommunityOuterDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.InterCommunityDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.InterCommunityOuterDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.sizenormalized.SizeNormalizedCompleteCommunityDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.sizenormalized.SizeNormalizedCompleteCommunityOuterDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.sizenormalized.SizeNormalizedInterCommunityDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.degree.sizenormalized.SizeNormalizedInterCommunityOuterDegreeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.AlternativeSemiCompleteCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.AlternativeSemiCompleteCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.CompleteCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.CompleteCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.InterCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.InterCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.SemiCompleteCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.SemiCompleteCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedCompleteCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedCompleteCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedInterCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedInterCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedSemiCompleteCommunityEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized.SizeNormalizedSemiCompleteCommunityOuterEdgeGiniRerankerGridSearch;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;


/**
 * Class that translates from a grid to the different contact recommendation algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class RerankerGridSelector<U>
{
    /**
     * Algorithm name
     */
    private final String reranker;
    /**
     * Grid that contains the different possible parameters for the algorithm.
     */
    private final Grid grid;
    /**
     * Maximum number of edges in the definitive ranking
     */
    private final int cutoff;
    /**
     * Indicates if scores have to be normalized
     */
    private final boolean norm;
    /**
     * Training graph.
     */
    private final Graph<U> graph;
    /**
     * Communities
     */
    private final Communities<U> comms;
    /**
     * Indicates if the normalization is done by ranking or by score.
     */
    private final boolean rank;
    
    /**
     * Constructor.
     * @param reranker name of the reranker
     * @param grid grid parameters.
     * @param cutoff The cutoff to apply to the reranker.
     * @param norm true if the scores have to be normalized or not.
     * @param graph the training graph.
     * @param comms community partition of the graph
     * @param rank true if the normalization is by ranking or false if it is done by score
     */
    public RerankerGridSelector(String reranker, Grid grid, int cutoff, boolean norm, boolean rank, Graph<U> graph, Communities<U> comms)
    {
        this.reranker = reranker;
        this.grid = grid;
        this.cutoff = cutoff;
        this.norm = norm;
        this.rank = rank;
        this.graph = graph;
        this.comms = comms;
        
    }
    
    /**
     * Obtains the different variants of the available algorithms using the different
     * parameters in the grid.
     * @return a map containing the different algorithm suppliers.
     */
    public Map<String, Supplier<GlobalReranker<U,U>>> getRecommenders()
    {
        RerankerGridSearch<U> gridsearch;
        switch(this.reranker)
        {
            case RANDOM:
                gridsearch = new RandomRerankerGridSearch(cutoff, norm, rank);
                break;
            case WEAKTIES:
                gridsearch = new WeakTiesRerankerGridSearch(cutoff, norm, rank, graph, comms);
                break;
            case STRONGTIES:
                gridsearch = new StrongTiesRerankerGridSearch(cutoff, norm, rank, graph, comms);
                break;
            case CLUSTCOEF:
                gridsearch = new ClusteringCoefficientRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case CLUSTCOEFCOMPL:
                gridsearch = new ClusteringCoefficientComplementRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case DEGREEGINICOMPL:
                gridsearch = new DegreeGiniComplementRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case AVGEMBEDDEDNESS:
                gridsearch = new AverageEmbeddednessRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case AVGWEAKNESS:
                gridsearch = new AverageWeaknessRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case HEURISTICAVGEMBEDDEDNESS:
                gridsearch = new HeuristicAverageEmbeddednessRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case HEURISTICAVGWEAKNESS:
                gridsearch = new HeuristicAverageWeaknessRerankerGridSearch<>(cutoff, norm, rank, graph);
                break;
            case ICDEGREEGINI:
                gridsearch = new InterCommunityDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERICDEGREEGINI:
                gridsearch = new InterCommunityOuterDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case CDEGREEGINI:
                gridsearch = new CompleteCommunityDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERCDEGREEGINI:
                gridsearch = new CompleteCommunityOuterDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SNICDEGREEGINI:
                gridsearch = new SizeNormalizedInterCommunityDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSNICDEGREEGINI:
                gridsearch = new SizeNormalizedInterCommunityOuterDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SNCDEGREEGINI:
                gridsearch = new SizeNormalizedCompleteCommunityDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSNCDEGREEGINI:
                gridsearch = new SizeNormalizedCompleteCommunityOuterDegreeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case ICEDGEGINI:
                gridsearch = new InterCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERICEDGEGINI:
                gridsearch = new InterCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case CEDGEGINI:
                gridsearch = new CompleteCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERCEDGEGINI:
                gridsearch = new CompleteCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SCEDGEGINI:
                gridsearch = new SemiCompleteCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case ALTSCEDGEGINI:
                gridsearch = new AlternativeSemiCompleteCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSCEDGEGINI:
                gridsearch = new SemiCompleteCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case ALTOUTERSCEDGEGINI:
                gridsearch = new AlternativeSemiCompleteCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SNICEDGEGINI:
                gridsearch = new SizeNormalizedInterCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSNICEDGEGINI:
                gridsearch = new SizeNormalizedInterCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SNCEDGEGINI:
                gridsearch = new SizeNormalizedCompleteCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSNCEDGEGINI:
                gridsearch = new SizeNormalizedCompleteCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case SNSCEDGEGINI:
                gridsearch = new SizeNormalizedSemiCompleteCommunityEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
            case OUTERSNSCEDGEGINI:
                gridsearch = new SizeNormalizedSemiCompleteCommunityOuterEdgeGiniRerankerGridSearch<>(cutoff, norm, rank, graph, comms);
                break;
                
            // Default behavior
            default:
                gridsearch = null;
        }
        
        if(gridsearch != null)
            return gridsearch.grid(grid);
        return new HashMap<>();
    }
}
