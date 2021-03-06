/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.edge;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerGridSearch;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.AVGEMBEDDEDNESS;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.CLUSTCOEF;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.HEURISTICAVGEMBEDDEDNESS;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.HEURISTICAVGWEAKNESS;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.edge.EmbedednessReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.edge.HeuristicEmbedednessReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.edge.WeaknessReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.graph.ClusteringCoefficientReranker;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid search for a reranker that optimizes the Average Embeddedness of the graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class HeuristicAverageEmbeddednessRerankerGridSearch<U> implements RerankerGridSearch<U> 
{
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
     * Indicates if the normalization is done by ranking or by score.
     */
    private final boolean rank;
    
    /**
     * Identifier for the parameter that takes the trade-off between relevance and diversity.
     */
    private final String LAMBDA = "lambda";
    
    /**
     * Identifier for the update mode of the reranker
     */
    private final String MODE = "mode";
    
    /**
     * Constructor.
     * @param cutoff The cutoff to apply to the reranker.
     * @param norm true if the scores have to be normalized or not.
     * @param graph the training graph.
     * @param rank true if the normalization is by ranking or false if it is done by score
     */
    public HeuristicAverageEmbeddednessRerankerGridSearch(int cutoff, boolean norm, boolean rank, Graph<U> graph)
    {
        this.cutoff = cutoff;
        this.norm = norm;
        this.graph = graph;
        this.rank = rank;
    }
    
    @Override
    public Map<String, Supplier<GlobalReranker<U, U>>> grid(Grid grid) 
    {
        Map<String, Supplier<GlobalReranker<U,U>>> rerankers = new HashMap<>();
        
        grid.getDoubleValues(LAMBDA).forEach(lambda -> 
        {
            grid.getIntegerValues(MODE).forEach(mode -> 
            {
                rerankers.put(HEURISTICAVGEMBEDDEDNESS + "-" + mode + "-" + lambda, () -> 
                {
                    return new HeuristicEmbedednessReranker<>(lambda, cutoff, norm, rank, graph, mode);
                });
            });
        });
        
        return rerankers;
    }
    
}
