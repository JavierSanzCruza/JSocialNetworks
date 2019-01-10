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
import static es.uam.eps.ir.socialnetwork.grid.metrics.graph.GraphMetricIdentifiers.CLUSTCOEF;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.graph.ClusteringCoefficient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the clustering coefficient of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ClusteringCoefficientGridSearch<U> implements GraphMetricGridSearch<U> 
{    
    /**
     * Identifier for the node first neighbor selection
     */
    private final static String USEL = "uSel";
    /**
     * Identifier for the node second neighbor selection
     */
    private final static String VSEL = "vSel";

    @Override
    public Map<String, Supplier<GraphMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<GraphMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.stream().forEach(uSel -> {
            vSels.stream().forEach(vSel -> {
               metrics.put(CLUSTCOEF + "_" + uSel + "_" + vSel, () -> {
                   return new ClusteringCoefficient(uSel, vSel);
               });
            });
        });
        
        return metrics;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, GraphMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, GraphMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.stream().forEach(uSel -> {
            vSels.stream().forEach(vSel -> {
               metrics.put(CLUSTCOEF + "_" + uSel + "_" + vSel, (distcalc) -> {
                   return new ClusteringCoefficient(uSel, vSel);
               });
            });
        });
        
        return metrics;
    }
    
}
