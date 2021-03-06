/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.local.graph;

import com.rits.cloning.Cloner;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a graph according to a global graph metric which we want to update.
 * The value of the metric is taken as the novelty score.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class DirectGraphMetricReranker<U> extends GraphMetricReranker<U> 
{

    public DirectGraphMetricReranker(double lambda, int cutoff, boolean norm, Graph<U> graph, GraphMetric<U> graphMetric) 
    {
        super(lambda, cutoff, norm, graph, graphMetric);
    }

    @Override
    protected GreedyUserReranker<U, U> getUserReranker(Recommendation<U, U> recommendation, int maxLength) {
        Cloner cloner = new Cloner();
        return new DirectGraphMetricUserReranker(recommendation, maxLength, cloner.deepClone(this.graph), this.metric);
    }
    
    protected class DirectGraphMetricUserReranker extends GraphMetricUserReranker
    {
        public DirectGraphMetricUserReranker(Recommendation<U, U> recommendation, int maxLength, Graph<U> graph, GraphMetric<U> metric) 
        {
            super(recommendation, maxLength, graph, metric);
        }

        @Override
        protected double nov(Tuple2od<U> iv) {
            U user = recommendation.getUser();
            U item = iv.v1;
            
            Cloner cloner = new Cloner();
            Graph<U> cloneGraph = cloner.deepClone(this.graph);
            cloneGraph.addEdge(user, item);
            return metric.compute(cloneGraph);
        }
        
    }
    
}
