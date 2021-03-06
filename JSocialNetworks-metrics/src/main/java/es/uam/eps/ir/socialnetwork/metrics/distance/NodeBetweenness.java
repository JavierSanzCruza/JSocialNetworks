/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import java.util.Map;

/**
 * Computes the betweenness of the nodes of a graph
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class NodeBetweenness<U> implements VertexMetric<U> 
{

    /**
     * Distance calculator.
     */
    private final DistanceCalculator<U> dc;
    
    /**
     * Constructor
     */
    public NodeBetweenness()
    {
        this.dc = new DistanceCalculator<>();
    }
    
    /**
     * Constructor.
     * @param dc Distance calculator.
     */
    public NodeBetweenness(DistanceCalculator<U> dc)
    {
        this.dc = dc;
    }
    
    @Override
    public double compute(Graph<U> graph, U user) {
        this.dc.computeDistances(graph);
        return this.dc.getNodeBetweenness(user);
    }

    @Override
    public Map<U, Double> compute(Graph<U> graph) {
        this.dc.computeDistances(graph);
        return this.dc.getNodeBetweenness();
    }

    @Override
    public double averageValue(Graph<U> graph) {
        if(graph.getVertexCount() > 0L)
            return this.compute(graph).values().stream().mapToDouble(val -> val).average().getAsDouble();
        return 0.0;
    }
    
}
