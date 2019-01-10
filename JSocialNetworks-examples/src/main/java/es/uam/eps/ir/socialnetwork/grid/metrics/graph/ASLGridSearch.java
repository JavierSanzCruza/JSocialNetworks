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
import static es.uam.eps.ir.socialnetwork.grid.metrics.graph.GraphMetricIdentifiers.ASL;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.ASL;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.distance.modes.ASLMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the average shortest path length of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ASLGridSearch<U> implements GraphMetricGridSearch<U> 
{
    /**
     * Identifier for the computing mode
     */
    private final static String MODE = "mode";
    /**
     * Identifier for the harmonic mean mode
     */
    private final static String NONINFINITEDISTANCES = "Non Infinite Distances";
    /**
     * Identifier for the averaging over components
     */
    private final static String COMPONENTS = "Components";
       
    @Override
    public Map<String, Supplier<GraphMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc) 
    {
        Map<String, Supplier<GraphMetric<U>>> metrics = new HashMap<>();
        List<String> modes = grid.getStringValues(MODE);
        
        modes.forEach(mode -> 
        {
            if(mode.equals(NONINFINITEDISTANCES))
            {
                metrics.put(ASL + "_" + NONINFINITEDISTANCES, () -> 
                {
                    return new ASL(distCalc, ASLMode.NONINFINITEDISTANCES);
                });
            }
            else
            {
                metrics.put(ASL + "_" + COMPONENTS, () -> {
                    return new ASL(distCalc, ASLMode.COMPONENTS);
                });
            }
        });
        
        return metrics;
    }
    
    @Override
    public Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> grid(Grid grid) 
    {
        Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> metrics = new HashMap<>();
        List<String> modes = grid.getStringValues(MODE);
        
        modes.forEach(mode -> 
        {
            if(mode.equals(NONINFINITEDISTANCES))
            {
                metrics.put(ASL + "_" + NONINFINITEDISTANCES, (distCalc) -> 
                {
                    return new ASL(distCalc, ASLMode.NONINFINITEDISTANCES);
                });
            }
            else
            {
                metrics.put(ASL + "_" + COMPONENTS, (distCalc) -> {
                    return new ASL(distCalc, ASLMode.COMPONENTS);
                });
            }
        });
        
        return metrics;
    } 
}
