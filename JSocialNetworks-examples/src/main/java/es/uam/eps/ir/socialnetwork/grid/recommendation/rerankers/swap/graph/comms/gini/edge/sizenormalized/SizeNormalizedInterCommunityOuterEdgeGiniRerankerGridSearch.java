/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.swap.graph.comms.gini.edge.sizenormalized;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerGridSearch;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.rerankers.RerankerIdentifiers.OUTERSNICEDGEGINI;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedInterCommunityOuterEdgeGiniReranker;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid search for a reranker that reduces the Gini index of the number of links between pairs of communities (restricted
 * to different pairs of communities). Number of links is divided by the maximum possible value between the endpoint communities.
 * It also promotes the number of links between communities.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * @see es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.SizeNormalizedInterCommunityOuterEdgeGiniReranker
 */
public class SizeNormalizedInterCommunityOuterEdgeGiniRerankerGridSearch<U> implements RerankerGridSearch<U> 
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
     * Communities
     */
    private final Communities<U> comms;
    /**
     * Indicates if the normalization is done by ranking or by score.
     */
    private final boolean rank;
    /**
     * Identifier for the parameter that takes the trade-off between relevance and diversity.
     */
    private final String LAMBDA = "lambda";
        
    /**
     * Constructor.
     * @param cutoff The cutoff to apply to the reranker.
     * @param norm true if the scores have to be normalized or not.
     * @param graph the training graph.
     * @param comms community partition of the graph
     * @param rank true if the normalization is by ranking or false if it is done by score
     */
    public SizeNormalizedInterCommunityOuterEdgeGiniRerankerGridSearch(int cutoff, boolean norm, boolean rank, Graph<U> graph, Communities<U> comms)
    {
        this.cutoff = cutoff;
        this.norm = norm;
        this.graph = graph;
        this.rank = rank;
        this.comms = comms;
    }
    
    @Override
    public Map<String, Supplier<GlobalReranker<U, U>>> grid(Grid grid) 
    {
        Map<String, Supplier<GlobalReranker<U,U>>> rerankers = new HashMap<>();
        
        grid.getDoubleValues(LAMBDA).forEach(lambda -> 
        {
            rerankers.put(OUTERSNICEDGEGINI + "-" + lambda, () -> 
            {
                return new SizeNormalizedInterCommunityOuterEdgeGiniReranker<>(lambda, cutoff, norm, rank, graph, comms);
            });               

        });
        
        return rerankers;
    }
    
}
