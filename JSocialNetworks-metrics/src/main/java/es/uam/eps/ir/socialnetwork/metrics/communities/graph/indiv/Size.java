/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.communities.graph.indiv;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.IndividualCommunityMetric;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Map;

/**
 * Computes the size of communities
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class Size<U> implements IndividualCommunityMetric<U> 
{

    @Override
    public double compute(Graph<U> graph, Communities<U> comm, int indiv) 
    {
        return comm.getUsers(indiv).count() + 0.0;
    }

    @Override
    public Map<Integer, Double> compute(Graph<U> graph, Communities<U> comm) {
        Map<Integer, Double> map = new Int2DoubleOpenHashMap();
        comm.getCommunities().forEach(c -> 
        {
            map.put(c, this.compute(graph, comm, c));
        });
        return map;
    }

    @Override
    public double averageValue(Graph<U> graph, Communities<U> comm) 
    {
        return this.compute(graph, comm).values().stream().mapToDouble(value -> value).average().getAsDouble();
    }
    
}
