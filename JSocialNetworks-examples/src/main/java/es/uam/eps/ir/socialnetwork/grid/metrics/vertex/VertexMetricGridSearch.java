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
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class for performing the grid search for a given algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public interface VertexMetricGridSearch<U>
{
    /**
     * Obtains the different vertex metrics to compute in a grid.
     * @param grid The grid for the algorithm
     * @param distCalc A distance calculator.
     * @return the grid parameters.
     */
    public Map<String, Supplier<VertexMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc);
    
    /**
     * Obtains the different vertex metrics to compute in a grid.
     * @param grid The grid for the algorithm
     * @return the grid parameters.
     */
    public Map<String, Function<DistanceCalculator<U>,VertexMetric<U>>> grid(Grid grid);
}
