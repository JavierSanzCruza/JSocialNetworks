/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.highdegree.node;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.AbstractNodeSampler;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Node sampler. It greedily obtains nodes with the larger number of nodes in the sample
 * that point to it.
 * 
 * Maiya, A., Berger-Wolf, T. Benefits of bias: Towards Better Characterization of Network Sampling. KDD 2011
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class SampleEdgeCountSampler<U> extends AbstractNodeSampler<U> 
{
    /**
     * Number of initial nodes.
     */
    private final int numInitial;
    /**
     * Neighborhood selection for the nodes.
     */
    private final EdgeOrientation orientation;
    /**
     * Random number generator.
     */
    private final Random rng;
    
    /**
     * Constructor.
     * @param numInitial Number of initial nodes.
     * @param orientation Neighborhood selection for the nodes
     */
    public SampleEdgeCountSampler(int numInitial, EdgeOrientation orientation)
    {
        this.numInitial = numInitial;
        this.orientation = orientation;
        this.rng = new Random();
    }
    
    @Override
    protected Collection<U> sampleNodes(Graph<U> fullGraph, int num) 
    {
        Set<U> visited = new HashSet<>();
        List<U> allNodes = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        
        Map<U, Double> neighSizes = new HashMap<>();
        
        // Obtain the initial number of nodes and its neighbors.
        while(visited.size() < numInitial)
        {
            U node = allNodes.get(rng.nextInt(allNodes.size()));
            visited.add(node);
        }

        visited.forEach(u -> 
        {
            fullGraph.getNeighbourhood(u, orientation).forEach(v -> 
            {
                if(!visited.contains(v) && !neighSizes.containsKey(v))
                {
                    neighSizes.put(v, 1.0);
                }
                else if(!visited.contains(v))
                {
                    neighSizes.put(v, neighSizes.get(v) + 1.0);
                }
            });
        });
        
        
        while(visited.size() < num && !neighSizes.isEmpty())
        {
            double max = Double.NEGATIVE_INFINITY;
            U maxU = null;
            for(Entry<U,Double> entry : neighSizes.entrySet())
            {
                if(entry.getValue() > max)
                {
                    max = entry.getValue();
                    maxU = entry.getKey();
                }
            }
            
            if(maxU != null) // The contrary will never happen
            {
                fullGraph.getNeighbourhood(maxU, orientation).forEach(v -> 
                {
                    if(!visited.contains(v) && !neighSizes.containsKey(v))
                    {
                        neighSizes.put(v, 1.0);
                    }
                    else if(!visited.contains(v))
                    {
                        neighSizes.put(v, neighSizes.get(v) + 1.0);
                    }
                });
                
                neighSizes.remove(maxU);
                visited.add(maxU);
            }
            
        }
        
        return visited;
    }
    
}
