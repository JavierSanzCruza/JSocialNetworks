/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.gini.edge;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricGridSearch;
import static es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricIdentifiers.INTERCOMMUNITYEDGEGINI;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.communities.graph.gini.edges.InterCommunityEdgeGini;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid for the inter-community edge gini of the graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class InterCommunityEdgeGiniGridSearch<U> implements GlobalCommunityMetricGridSearch<U> 
{
    
    @Override
    public Map<String, Supplier<CommunityMetric<U>>> grid(Grid grid) 
    {
        Map<String, Supplier<CommunityMetric<U>>> metrics = new HashMap<>();

        metrics.put(INTERCOMMUNITYEDGEGINI, () -> 
        {
            return new InterCommunityEdgeGini();
        });
        
        return metrics;
    }
    
}
