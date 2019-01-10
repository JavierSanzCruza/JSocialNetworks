/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.grid.metrics.comm.global;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricIdentifiers.NUMCOMMS;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.communities.graph.NumCommunities;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Grid search for the metric that finds the number of communities in a graph.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users
 * 
 * @see es.uam.eps.ir.socialnetwork.metrics.communities.graph.NumCommunities
 */
public class NumCommunitiesGridSearch<U> implements GlobalCommunityMetricGridSearch<U>
{
    @Override
    public Map<String, Supplier<CommunityMetric<U>>> grid(Grid grid)
    {
        Map<String, Supplier<CommunityMetric<U>>> map = new HashMap<>();
        map.put(NUMCOMMS, () -> new NumCommunities());
        return map;
    }

}
