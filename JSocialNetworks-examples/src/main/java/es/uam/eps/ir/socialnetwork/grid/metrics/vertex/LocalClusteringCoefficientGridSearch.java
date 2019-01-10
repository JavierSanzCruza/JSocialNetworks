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
import static es.uam.eps.ir.socialnetwork.grid.metrics.vertex.VertexMetricIdentifiers.LOCALCLUSTCOEF;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.vertex.LocalClusteringCoefficient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the local clustering coefficient of a node.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class LocalClusteringCoefficientGridSearch<U> implements VertexMetricGridSearch<U> 
{    

    /**
     * Identifier for the origin neighborhood selection
     */
    private final static String USEL = "uSel";
    /**
     * Identifier for the destination neighborhood selection
     */
    private final static String VSEL = "vSel";

    @Override
    public Map<String, Supplier<VertexMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<VertexMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.forEach(uSel -> 
        {
            vSels.forEach(vSel -> {
                metrics.put(LOCALCLUSTCOEF + "_" + uSel + "_" + vSel, () -> 
                {
                    return new LocalClusteringCoefficient(uSel, vSel);
                });
            });
        });

        return metrics;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, VertexMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, VertexMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.forEach(uSel -> 
        {
            vSels.forEach(vSel -> {
                metrics.put(LOCALCLUSTCOEF + "_" + uSel + "_" + vSel, (distCalc) -> 
                {
                    return new LocalClusteringCoefficient(uSel, vSel);
                });
            });
        });

        return metrics;
    }
    
}
