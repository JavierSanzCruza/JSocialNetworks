/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.user;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a graph according to an average vertex graph metric which we want to update.
 * The value of the metric in the original graph is taken as the novelty score.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class GlobalOriginalInverseUserMetricReranker<U> extends GlobalUserMetricReranker<U> 
{
    /**
     * Constructor
     * @param lambda Trade-off between original and novelty scores
     * @param cutoff Maximum length of the recommendation graph.
     * @param norm True if scores have to be normalized, false if not.
     * @param graph The original graph.
     * @param vertexMetric The vertex metric we want to compute.
     */
    public GlobalOriginalInverseUserMetricReranker(double lambda, int cutoff, boolean norm, Graph<U> graph, VertexMetric<U> vertexMetric) 
    {
        super(lambda, cutoff, norm, graph, vertexMetric);
    }

    @Override
    protected double nov(U user, Tuple2od<U> item) 
    {
        U recomm = item.v1;
        return -metric.compute(graph, recomm);
    }

    @Override
    protected void update(U user, Tuple2od<U> selectedItem) 
    {

    }
    
}
