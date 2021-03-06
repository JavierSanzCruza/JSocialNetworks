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
import java.util.HashMap;
import java.util.Map;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a graph according to an average vertex metric which we want to minimize.
 * The value of the metric is taken as the novelty score.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class OriginalInverseUserMetricReranker<U> extends UserMetricReranker<U> 
{

    private final Map<U,Double> values;
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
    public OriginalInverseUserMetricReranker(double lambda, int cutoff, boolean norm, boolean rank, Graph<U> graph, VertexMetric<U> graphMetric) 
    {
        super(lambda, cutoff, norm, rank, graph, graphMetric);
        values = new HashMap<>();
    }


    @Override
    protected double nov(U u, Tuple2od<U> iv) {
        U item = (U) iv.v1;
        if(values.containsKey(item))
            return values.get(item);
        double value = -metric.compute(graph, item);
        values.put(item, value);
        return value;
    }

    @Override
    protected void update(U user, Tuple2od<U> bestItemValue) {
    }

    @Override
    protected void update(Recommendation<U, U> reranked) {
    }
    
}
