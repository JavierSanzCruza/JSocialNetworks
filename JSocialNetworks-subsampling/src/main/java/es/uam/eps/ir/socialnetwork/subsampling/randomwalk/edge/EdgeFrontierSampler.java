/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.edge;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.AbstractEdgeSampler;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that generates a sample from a graph, using the Frontier Sampler Algorithm.
 * This algorithm follows the next steps:
 * 
 * 1. S nodes are chosen at random
 * 2. A seed node, v in S, is chosen, proportionally to the degree.
 * 3. An edge containing v that goes outside the sample is selected (depending on the chosen directionality selection - originally out)
 * 4. The other node in the sample replaces node v
 * 5. Add the edge to the visited set
 * 6. Iterate until the desired number of edges is reached.
 * This algorithm starts with a random selection of nodes. 
 * Then, iteratively, it finds the neighbourhood of this nodes, and then,
 * selects the neighbour with greater degree.
 * 
 * This version obtains a list of nodes and each edge among them in the original graph. It should be noted
 * that the original algorithm was oriented to the retrieval of edges (See Edge version).
 * 
 * Note: If number of seed nodes is equal to 1, this is equivalent to a random walk without teleport in case of sink.
 * 
 * Ribeiro, B. Towsley, D. Estimating and Sampling Graphs with Multidimensional Random Walks. IMC 2010
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class EdgeFrontierSampler<U> extends AbstractEdgeSampler<U> 
{
    /**
     * Neighborhood and degree selection.
     */
    private final EdgeOrientation orientation;
    /**
     * Number of initial nodes.
     */
    private final int numInitial;
    /**
     * Random number generator.
     */
    private final Random rng;
    
    /**
     * Constructor.
     * @param orientation Neighborhood (and degree) selection
     * @param numInitial Number of initial nodes.
     */
    public EdgeFrontierSampler(EdgeOrientation orientation, int numInitial)
    {
        this.orientation = orientation;
        this.numInitial = numInitial;
        this.rng = new Random();
    }
    
    @Override
    protected Collection<Pair<U>> sampleEdges(Graph<U> fullGraph, int num) 
    {      
        Set<Pair<U>> visited = new HashSet<>();
        Map<U, Double> frontierMap = new HashMap<>();
        List<U> frontierNodes = new ArrayList<>();
        
        double sum = 0.0;
        
        List<U> allVertices = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));

        // Selects the seed nodes.
        while(visited.size() < numInitial)
        {
            U node = allVertices.get(rng.nextInt(visited.size()));
            double degree = fullGraph.degree(node, orientation) + 0.0;
            frontierMap.put(node, degree);
            frontierNodes.add(node);
            sum += degree;
        }
        
        while(visited.size() < num && sum > 0.0 && !frontierNodes.isEmpty())
        {
            // First, we select a neighborhood proportionally to its degree
            double value = rng.nextDouble();
            double currentSum = 0.0;
            U current = null;
            int i  = 0;            
            for(U u : frontierNodes)
            {
                if(current == null) current = u; // We set the first node (to prevent problems)
                currentSum += frontierMap.get(u) / sum;
                if(value < currentSum)
                {
                    current = u;
                    break;
                }
                ++i;
            }
            
            double degree = frontierMap.get(current);
            
            // Substitute the current node for another one in the frontier.
            List<U> neighs = fullGraph.getNeighbourhood(current, orientation).collect(Collectors.toCollection(ArrayList::new));
            U substituted = neighs.get(rng.nextInt(neighs.size()));
            double substitutedDegree = fullGraph.degree(substituted, orientation) + 0.0;
            
            sum += (substitutedDegree - degree);
            frontierNodes.set(i, substituted);
            frontierMap.put(substituted, substitutedDegree);
            
            // Add the edge to the sample
            if(this.orientation.equals(EdgeOrientation.IN))
            {
                visited.add(new Pair<>(substituted, current));
            }
            else if(this.orientation.equals(EdgeOrientation.OUT))
            {
                visited.add(new Pair<>(current, substituted));
            }
            else
            {
                if(fullGraph.containsEdge(current, substituted))
                    visited.add(new Pair<>(current, substituted));
                if(fullGraph.containsEdge(substituted, current))
                    visited.add(new Pair<>(substituted, current));
            }
            
        }
        
        return visited;
    }
}
