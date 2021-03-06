/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.user;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.LocalLambdaReranker;

/**
 * Reranks a graph according to a user metric we want to optimize.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public abstract class UserMetricReranker<U> extends LocalLambdaReranker<U,U> 
{
    /**
     * The graph.
     */
    protected final Graph<U> graph;
    
    /**
     * The selected metric
     */
    protected final VertexMetric<U> metric;
    

    /**
     * Constructor.
     * @param lambda Param that establishes a balance between the score and the 
     * novelty/diversity value.
     * @param cutoff Number of elements to take.
     * @param norm Indicates if scores have to be normalized.
     * @param graph The graph.
     * @param graphMetric The graph metric to optimize.
     * @param rank Indicates if the normalization is by ranking (true) or by score (false)
     */
    public UserMetricReranker(double lambda, int cutoff, boolean norm, boolean rank, Graph<U> graph, VertexMetric<U> graphMetric) 
    {
        super(cutoff, lambda, norm, rank);
        this.graph = graph;
        this.metric = graphMetric;
    }
}
