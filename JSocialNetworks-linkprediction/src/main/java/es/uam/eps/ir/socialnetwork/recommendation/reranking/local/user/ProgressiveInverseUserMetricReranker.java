/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.local.user;

import com.rits.cloning.Cloner;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a graph according to a global graph metric which we want to update.
 * The value of the metric is taken as the novelty score.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ProgressiveInverseUserMetricReranker<U> extends UserMetricReranker<U> 
{

    public ProgressiveInverseUserMetricReranker(double lambda, int cutoff, boolean norm, Graph<U> graph, VertexMetric<U> graphMetric) 
    {
        super(lambda, cutoff, norm, graph, graphMetric);
    }

    @Override
    protected GreedyUserReranker<U, U> getUserReranker(Recommendation<U, U> recommendation, int maxLength) 
    {
        Cloner cloner = new Cloner();
        return new ProgressiveInverseUserMetricUserReranker(recommendation, maxLength, cloner.deepClone(graph), metric);
    }
    
    protected class ProgressiveInverseUserMetricUserReranker extends UserMetricUserReranker
    {
        public ProgressiveInverseUserMetricUserReranker(Recommendation<U, U> recommendation, int maxLength, Graph<U> graph, VertexMetric<U> metric) 
        {
            super(recommendation, maxLength, graph, metric);
        }        

        @Override
        protected double nov(Tuple2od iv) 
        {
            U user = (U) recommendation.getUser();
            U item = (U) iv.v1;
            
            Cloner cloner = new Cloner();
            Graph<U> cloneGraph = cloner.deepClone(graph);
            cloneGraph.addEdge(user, item);

            return -metric.compute(cloneGraph, item);        
        }
        
        @Override
        protected void update(Tuple2od bestItemValue) 
        {
            U user = (U) recommendation.getUser();
            U item = (U) bestItemValue.v1;
            
            this.graph.addEdge(user, item);
        }

        
    }
    
}
