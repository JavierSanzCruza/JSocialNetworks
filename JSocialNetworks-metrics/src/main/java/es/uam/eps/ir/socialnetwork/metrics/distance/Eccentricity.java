/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import java.util.HashMap;
import java.util.Map;

/**
 * Metric that computes the eccentricity of the nodes.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the vertices.
 */
public class Eccentricity<U> implements VertexMetric<U>
{
    /**
     * Distance calculator.
     */
    private final DistanceCalculator<U> dc;
    
    /**
     * Constructor.
     */
    public Eccentricity()
    {
        this.dc = new DistanceCalculator<>();
    }
    
    /**
     * Constructor. 
     * @param dc distance calculator.
     */
    public Eccentricity(DistanceCalculator<U> dc)
    {
        this.dc = dc;
    }

    @Override
    public double compute(Graph<U> graph, U user) {
        this.dc.computeDistances(graph);
        double value = 0.0;
        Map<U, Double> distances = this.dc.getDistances(user);
        for(double aux : distances.values())
        {
            if(aux > value && aux != Double.POSITIVE_INFINITY)
                value = aux;
        }
        return value;
    }

    @Override
    public Map<U, Double> compute(Graph<U> graph) {
        Map<U, Double> map = new HashMap<>();
        graph.getAllNodes().forEach(u -> map.put(u, this.compute(graph, u)));
        return map;
    }

    @Override
    public double averageValue(Graph<U> graph) 
    {
        if(graph.getVertexCount() == 0) return 0.0;
        return this.compute(graph).values().stream().mapToDouble(val -> val).average().getAsDouble();
    }
}
