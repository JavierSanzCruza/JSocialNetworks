/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.basic;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.subsampling.AbstractEdgeSampler;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Sampler that randomly selects edges in the network.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class RandomEdgeSampler<U> extends AbstractEdgeSampler<U> 
{
    @Override
    protected Collection<Pair<U>> sampleEdges(Graph<U> fullGraph, int num) 
    {
        List<Pair<U>> edgeList = new ArrayList<>();
        fullGraph.getAllNodes().forEach(u -> 
        {
            fullGraph.getAdjacentNodes(u).forEach(v ->
            {
                edgeList.add(new Pair<>(u,v));
            });
        });
        
        Random rng = new Random();
        
        Set<Pair<U>> sampledEdges = new HashSet<>();
        while(sampledEdges.size() < num)
        {
            int idx = rng.nextInt(edgeList.size());
            sampledEdges.add(edgeList.get(idx));
        }
        
        return sampledEdges;
    }
}
