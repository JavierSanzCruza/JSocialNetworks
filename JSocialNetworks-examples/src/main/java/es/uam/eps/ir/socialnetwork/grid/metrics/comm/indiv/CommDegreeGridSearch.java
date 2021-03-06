/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.comm.indiv;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.comm.indiv.IndividualCommunityMetricIdentifiers.COMMDEGREE;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.IndividualCommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.communities.graph.indiv.CommunityDegree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid for the degree of a node.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class CommDegreeGridSearch<U> implements IndividualCommunityMetricGridSearch<U> 
{
    /**
     * Identifier for the orientation
     */
    private static final String ORIENT = "orientation";
    
    @Override
    public Map<String, Supplier<IndividualCommunityMetric<U>>> grid(Grid grid) 
    {
        Map<String, Supplier<IndividualCommunityMetric<U>>> metrics = new HashMap<>();
        List<EdgeOrientation> orients = grid.getOrientationValues(ORIENT);
        
        orients.forEach(orient -> {
            metrics.put(COMMDEGREE + "_" + orient, () -> 
            {
                return new CommunityDegree(orient);
            });
        });
        
        return metrics;
    }
    
}
