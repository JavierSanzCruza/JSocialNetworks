/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.gini.degree;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricGridSearch;
import static es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricIdentifiers.COMPLETECOMMUNITYDEGREEGINI;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.communities.graph.gini.degree.CompleteCommunityDegreeGini;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid for the inter-community degree Gini of the graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class CompleteCommunityDegreeGiniGridSearch<U> implements GlobalCommunityMetricGridSearch<U> 
{
    /**
     * Identifier for the degree selection
     */
    private static final String ORIENT = "orientation";
    /**
     * Identifier for the autoloop selection
     */
    private static final String AUTOLOOPS = "autoloops";
    @Override
    public Map<String, Supplier<CommunityMetric<U>>> grid(Grid grid) 
    {
        Map<String, Supplier<CommunityMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> orients = grid.getOrientationValues(ORIENT);
        List<Boolean> loops = grid.getBooleanValues(AUTOLOOPS);
        
        orients.stream().forEach(orient -> 
        {
            loops.stream().forEach(loop -> 
            {
                metrics.put(COMPLETECOMMUNITYDEGREEGINI + "_" + orient + "_" + (loop ? "autoloops" : "noautoloops"), () -> 
                {
                    return new CompleteCommunityDegreeGini(orient, loop);
                });
            });
        });

        return metrics;
    }
    
}
