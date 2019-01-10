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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class for random walk samplers.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * This variant recovers a certain number of edges, and, after they have been found,
 * retrieves all nodes in their endpoints and add them to the graph.
 * 
 */
public abstract class AbstractEdgeRandomWalkSampler<U> extends AbstractEdgeSampler<U> 
{

    /**
     * Teleport rate of the random walk.
     */
    private final double r;
    /**
     * Edge orientation. Indicates in which direction links are traversed.
     */
    private final EdgeOrientation orientation;
    
    /**
     * Maximum number of iterations before the actual number of nodes is retrieved.
     */
    private final int maxNumIterMultiplier;
    
    /**
     * Number of independent random walks
     */
    private final int numInitial;
    /**
     * Random number generator
     */
    private final Random rng;
    
    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param r Teleport rate. With probability equal to r, the random walk will teleport.
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public AbstractEdgeRandomWalkSampler(EdgeOrientation orientation, double r, int maxNumIterMultiplier, int numInitial)
    {
        this.r = r;
        this.orientation = orientation;
        this.maxNumIterMultiplier = maxNumIterMultiplier;
        this.numInitial = numInitial;
        this.rng = new Random();
    }
    
    @Override
    protected Collection<Pair<U>> sampleEdges(Graph<U> fullGraph, int num) 
    {
        List<U> vertices = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        int max = vertices.size();
        List<U> origins = new ArrayList<>();
        List<U> randomWalks = new ArrayList<>();

        while(randomWalks.size() < this.numInitial)
        {
            int idx = rng.nextInt(max);
            origins.add(vertices.get(idx));
            randomWalks.add(vertices.get(idx));
        }
        
        Set<Pair<U>> visited = new HashSet<>();
        
        int iter = 0;
        while(visited.size() < num)
        {
            for(int i = 0; i < randomWalks.size(); ++i)
            {
                U actual = randomWalks.get(i);
                
                U next;
                if(iter >= maxNumIterMultiplier*num)
                {
                    // Randomly select another node: the rw is stuck. This new node will be the new origin.
                    int idx = rng.nextInt(max);
                    next = vertices.get(idx);
                    origins.set(i, next);
                }
                else if (rng.nextDouble() < this.r)
                {
                    next = this.teleport(fullGraph, actual, origins.get(i));
                }
                else // Randomly select a neighborhood node
                {
                    List<U> neighbors = fullGraph.getNeighbourhood(actual, orientation).collect(Collectors.toCollection(ArrayList::new));
                    next = this.selectNeighbor(fullGraph, actual, neighbors);
                    
                    if(next == null)
                        next = actual;
                    
                }
                
                // Store the new edge
                if(next != actual)
                {
                    if(!fullGraph.isDirected() || this.orientation.equals(EdgeOrientation.OUT))
                    {
                        visited.add(new Pair<>(actual, next));
                    }
                    else if(this.orientation.equals(EdgeOrientation.IN))
                    {
                        visited.add(new Pair<>(next, actual));
                    }
                    else
                    {
                        if(fullGraph.containsEdge(actual, next))
                            visited.add(new Pair<>(actual, next));
                        if(fullGraph.containsEdge(next, actual))
                            visited.add(new Pair<>(next, actual));
                    }
                }
                
                
                // Advance the random walk.
                randomWalks.set(i, next);
            }
            ++iter;
        }
        return visited;
    }
    
    /**
     * Finds a node to teleport to.
     * @param fullGraph the full graph.
     * @param actual the actual visiting node.
     * @param origin the origin node of the random walk.
     * @return the new node to teleport to.
     */
    protected abstract U teleport(Graph<U> fullGraph, U actual, U origin);
    
    /**
     * Finds a neighbor node of the actual node to travel to.
     * @param fullGraph the full graph.
     * @param actual the actual visiting node.
     * @param neighbors the set of neighbors of the visiting node
     * @return the selected neighbor, or null if none has been selected.
     */
    protected abstract U selectNeighbor(Graph<U> fullGraph, U actual, List<U> neighbors);
    
    /**
     * Get the random number generator.
     * @return The random number generator
     */
    protected Random getRNG()
    {
        return this.rng;
    }
    
    /**
     * Get the orientation
     * @return the orientation
     */
    protected EdgeOrientation getOrientation()
    {
        return this.orientation;
    }
}
