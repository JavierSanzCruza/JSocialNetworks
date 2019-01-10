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
import java.util.stream.Collectors;

/**
 * Node sampler. It tries to greedily obtain a sample with maximum expansion:
 * 
 * \arg\max_{S:|S|=k} \frac{|\Gamma(S)|}{|S|}
 * 
 * It does so using the following target function to select the next node in the sample:
 * 
 * \arg \max_{v \in N(S)} |N(\{v\}) - (N(S)\cup S)|
 * 
 * Maiya, A., Berger-Wolf, T. Benefits of bias: Towards Better Characterization of Network Sampling. KDD 2011
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ExpansionSampler<U> extends AbstractNodeSampler<U> 
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
    public ExpansionSampler(int numInitial, EdgeOrientation orientation)
    {
        this.numInitial = numInitial;
        this.orientation = orientation;
        this.rng = new Random();
    }
    
    @Override
    protected Collection<U> sampleNodes(Graph<U> fullGraph, int num) 
    {
        Set<U> visited = new HashSet<>();
        Set<U> neighborhood = new HashSet<>();
        List<U> allNodes = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        
        Map<U, Set<U>> neighborhoods = new HashMap<>();
        
        // Obtain the initial number of nodes and its neighbors.
        while(visited.size() < numInitial)
        {
            U node = allNodes.get(rng.nextInt(allNodes.size()));
            visited.add(node);
            neighborhood.addAll(fullGraph.getNeighbourhood(node, orientation).collect(Collectors.toCollection(ArrayList::new)));
            neighborhood.add(node);
        }

        Set<U> newUsers = new HashSet<>(neighborhood);
        newUsers.removeAll(visited);
        
        do
        {
            for(U neigh : newUsers)
            {
                if(visited.contains(neigh))
                {
                    neighborhoods.put(neigh, fullGraph.getNeighbourhood(neigh, orientation).collect(Collectors.toCollection(HashSet::new)));
                }
            }
            
            if(neighborhoods.isEmpty())
            {
                break; // There are no more selectable users.
            }
            
            // Select the next user in the sample
            double max = Double.NEGATIVE_INFINITY;
            U maxU = null;
            for(U user : neighborhoods.keySet())
            {
                neighborhoods.get(user).removeAll(neighborhood);
                double size = neighborhoods.get(user).size() + 0.0;
                if(size > max)
                {
                    max = size;
                    maxU = user;
                }
            }
            newUsers.clear();
            
            // In case a node is selected (other option should never occur)
            if(maxU != null)
            {
                // Visit node maxU
                visited.add(maxU);
                neighborhoods.remove(maxU);
                // Store its neighborhood.
                fullGraph.getNeighbourhood(maxU, orientation).forEach(neigh -> 
                {
                    if(!neighborhood.contains(neigh))
                    {
                        neighborhood.add(neigh);
                        newUsers.add(neigh);
                    }
                });
            }
            
        }
        while(visited.size() < num);
        
        return visited;
    }
    
}
