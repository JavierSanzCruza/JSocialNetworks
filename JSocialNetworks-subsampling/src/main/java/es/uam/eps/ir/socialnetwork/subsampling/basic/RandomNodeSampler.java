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
import es.uam.eps.ir.socialnetwork.subsampling.AbstractNodeSampler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sampler that randomly selects users in the network.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class RandomNodeSampler<U> extends AbstractNodeSampler<U> 
{
    @Override
    public Collection<U> sampleNodes(Graph<U> fullGraph, int num) 
    {
        
        List<U> nodelist = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        Random rng = new Random();
            
        Set<U> sampledNodes = new HashSet<>();
        while(sampledNodes.size() < num)
        {
            int idx = rng.nextInt(nodelist.size());
            sampledNodes.add(nodelist.get(idx));
        }
        return sampledNodes; 
    }   
}
