/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.vertex;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.vertex.VertexMetricIdentifiers.HITS;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.vertex.HITS;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the HITS value of a node.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class HITSGridSearch<U> implements VertexMetricGridSearch<U> 
{
    /**
     * Identifier for the orientation
     */
    private static final String MODE = "mode";

    @Override
    public Map<String, Supplier<VertexMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
                Map<String, Supplier<VertexMetric<U>>> metrics = new HashMap<>();
        List<Boolean> modes = grid.getBooleanValues(MODE);
        
        modes.forEach(mode -> {
            metrics.put(HITS + "_" + (mode ? "auth" : "hubs"), () -> 
            {
                return new HITS(mode);
            });
        });
        
        return metrics;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, VertexMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, VertexMetric<U>>> metrics = new HashMap<>();
        List<Boolean> modes = grid.getBooleanValues(MODE);
        
        modes.forEach(mode -> {
            metrics.put(HITS + "_" + (mode ? "auth" : "hubs"), (distCalc) -> 
            {
                return new HITS(mode);
            });
        });
        
        return metrics;
    }
    
}
