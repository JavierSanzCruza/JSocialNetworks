/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.graph;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;

/**
 * Reciprocity rate of the graph (proportion of reciprocal links)
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ReciprocityRate<U> implements GraphMetric<U> 
{
    @Override
    public double compute(Graph<U> graph) 
    {
        if(!graph.isDirected())
            return 1.0;
        
        double num = graph.getAllNodes().mapToDouble(u -> 
        {
            double countU = graph.getAdjacentNodes(u)
                                .filter(v -> graph.containsEdge(v,u))
                                .count() + 0.0;
            return countU;
        }).sum();
        
        double den = graph.getEdgeCount();
        return num / den;
    }
    
}
