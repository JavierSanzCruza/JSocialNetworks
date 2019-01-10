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
import static es.uam.eps.ir.socialnetwork.grid.metrics.edge.EdgeMetricIdentifiers.FOAF;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.edge.FOAF;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Grid for the number of common friends of the endpoints of an edge.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class FOAFGridSearch<U> implements EdgeMetricGridSearch<U> 
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
    public Map<String, Supplier<EdgeMetric<U>>> grid(Grid grid, DistanceCalculator<U> distCalc)
    {
        Map<String, Supplier<EdgeMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.forEach(uSel -> 
        {
            vSels.forEach(vSel -> {
                metrics.put(FOAF + "_" + uSel + "_" + vSel, () -> 
                {
                    return new FOAF(uSel, vSel);
                });
            });
        });

        return metrics;
    }

    @Override
    public Map<String, Function<DistanceCalculator<U>, EdgeMetric<U>>> grid(Grid grid)
    {
        Map<String, Function<DistanceCalculator<U>, EdgeMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        uSels.forEach(uSel -> 
        {
            vSels.forEach(vSel -> {
                metrics.put(FOAF + "_" + uSel + "_" + vSel, (distcalc) -> 
                {
                    return new FOAF(uSel, vSel);
                });
            });
        });

        return metrics;
    }
    
}
