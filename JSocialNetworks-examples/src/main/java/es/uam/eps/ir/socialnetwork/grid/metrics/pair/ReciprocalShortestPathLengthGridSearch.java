/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.pair;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.pair.PairMetricIdentifiers.RECIPROCALSPL;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.distance.ReciprocalShortestPathLength;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid search for the Reciprocal Shortest Path Length metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * 
 * @see es.uam.eps.ir.socialnetwork.metrics.distance.ReciprocalShortestPathLength
 * 
 */
public class ReciprocalShortestPathLengthGridSearch<U> implements PairMetricGridSearch<U>
{
    @Override
    public Map<String, Supplier<PairMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<PairMetric<U>>> map = new HashMap<>();
        map.put(RECIPROCALSPL, () -> {
            return new ReciprocalShortestPathLength(distCalc);
        });
        return map;    
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, PairMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>,PairMetric<U>>> map = new HashMap<>();
        map.put(RECIPROCALSPL, (distCalc) -> {
            return new ReciprocalShortestPathLength(distCalc);
        });
        return map;     
    }
    
}
