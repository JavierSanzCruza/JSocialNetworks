/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.graph;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.graph.GraphMetricIdentifiers.INTEREDGEGINI;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.graph.InterEdgeGini;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the edge Gini between different nodes in a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class InterEdgeGiniGridSearch<U> implements GraphMetricGridSearch<U> 
{    
    @Override
    public Map<String, Supplier<GraphMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<GraphMetric<U>>> metrics = new HashMap<>();
        
        metrics.put(INTEREDGEGINI, () -> 
        {
            return new InterEdgeGini();
        });
        
        return metrics;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, GraphMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, GraphMetric<U>>> metrics = new HashMap<>();
        
        metrics.put(INTEREDGEGINI, (distCalc) -> 
        {
            return new InterEdgeGini();
        });
        
        return metrics;
    }
    
}
