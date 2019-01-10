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
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class for performing the grid search for a metric for a pair of nodes.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public interface PairMetricGridSearch<U>
{
    /**
     * Obtains the different metrics for pairs of nodes to compute in a grid.
     * @param grid The grid for the metric
     * @param distCalc a distance calculator.
     * @return a map containing suppliers for the metrics, indexed by name.
     */
    public Map<String, Supplier<PairMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc);
    
    /**
     * Obtains the different metrics for pairs of nodes to compute in a grid.
     * @param grid The grid for the metric
     * @return a map containing suppliers for the metrics which depend on a distance calculator, indexed by name.
     */
    public Map<String, Function<DistanceCalculator<U>,PairMetric<U>>> grid(Grid grid);
}
