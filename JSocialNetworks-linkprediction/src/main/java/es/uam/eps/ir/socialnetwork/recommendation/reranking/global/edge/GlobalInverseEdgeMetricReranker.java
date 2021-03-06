/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.edge;

import com.rits.cloning.Cloner;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a graph according to a global graph metric which we want to update.
 * The value of the metric is taken as the novelty score.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class GlobalInverseEdgeMetricReranker<U> extends GlobalEdgeMetricReranker<U> 
{

    public GlobalInverseEdgeMetricReranker(double lambda, int cutoff, boolean norm, Graph<U> graph, PairMetric<U> graphMetric) 
    {
        super(lambda, cutoff, norm, graph, graphMetric);
    }

    @Override
    protected double nov(U user, Tuple2od<U> iv) {
        U item = iv.v1;

        Cloner cloner = new Cloner();
        Graph<U> cloneGraph = cloner.deepClone(this.graph);
        cloneGraph.addEdge(user, item);
        return -metric.compute(cloneGraph,user,item);
    }       
    
}
