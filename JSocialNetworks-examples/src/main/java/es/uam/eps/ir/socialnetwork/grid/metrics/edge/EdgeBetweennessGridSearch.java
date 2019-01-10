/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.edge;


import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.edge.EdgeMetricIdentifiers.BETWEENNESS;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.distance.EdgeBetweenness;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid search for the edge betweenness.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class EdgeBetweennessGridSearch<U> implements EdgeMetricGridSearch<U>
{
    @Override
    public Map<String, Supplier<EdgeMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<EdgeMetric<U>>> map = new HashMap<>();
        map.put(BETWEENNESS, () -> {
            return new EdgeBetweenness(distCalc);
        });
        return map;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, EdgeMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, EdgeMetric<U>>> map = new HashMap<>();
        map.put(BETWEENNESS, (distCalc) -> {
            return new EdgeBetweenness(distCalc);
        });
        return map;
    }

}
