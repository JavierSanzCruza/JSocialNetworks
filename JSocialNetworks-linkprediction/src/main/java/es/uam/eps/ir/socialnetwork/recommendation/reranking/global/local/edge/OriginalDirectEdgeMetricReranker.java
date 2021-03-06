/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.edge;

import org.ranksys.core.util.tuples.Tuple2od;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import java.util.HashMap;
import java.util.Map;
import org.ranksys.core.Recommendation;

/**
 * Reranks a graph according to a edge graph metric which we want to improve.
 * The value of the metric is taken as the novelty score. We use as novelty
 * score the original value of the metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class OriginalDirectEdgeMetricReranker<U> extends EdgeMetricReranker<U> 
{

    /**
     * A map containing the edge metric values for each pair in the original graph.
     */
    private Map<U, Map<U, Double>> values;
    
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
    public OriginalDirectEdgeMetricReranker(double lambda, int cutoff, boolean norm, boolean rank, Graph<U> graph, PairMetric<U> graphMetric) 
    {
        super(lambda, cutoff, norm, rank, graph, graphMetric);
    }
    
    
    @Override
    protected double nov(U u, Tuple2od<U> iv) 
    {
        U item = iv.v1;

        if(values.containsKey(u))
        {
            if(values.get(u).containsKey(item))
                return values.get(u).get(item);
            double value = metric.compute(graph, u, item);
            values.get(u).put(item, value);
            return value;
        }
                
        double value = metric.compute(graph, u, item);
        values.put(u, new HashMap<>());
        values.get(u).put(item, value);
        return value;
    }

    @Override
    protected void update(U user, Tuple2od<U> bestItemValue) {
    }

    @Override
    protected void update(Recommendation<U, U> reranked) {
    }

    
        
    
    
    
}
