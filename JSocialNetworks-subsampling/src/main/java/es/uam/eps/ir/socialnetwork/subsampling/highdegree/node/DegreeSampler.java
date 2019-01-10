/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.highdegree.node;

import es.uam.eps.ir.socialnetwork.graph.DirectedGraph;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.UndirectedGraph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.AbstractNodeSampler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Class that generates samples from graphs, using the Degree Sampling algorithm
 * This algorithm starts with a random selection of nodes. 
 * Then, iteratively, it finds the neighbourhood of this nodes, and then,
 * selects the neighbour with greater degree.
 * 
 * Maiya, A., Berger-Wolf, T. Benefits of Bias: Towards Better Characterization of Network Sampling. KDD 2011
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class DegreeSampler<U> extends AbstractNodeSampler<U> 
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
    public DegreeSampler(EdgeOrientation orientation, int numInitial)
    {
        this.orientation = orientation;
        this.numInitial = numInitial;
        this.rng = new Random();
    }
    
    @Override
    protected Collection<U> sampleNodes(Graph<U> fullGraph, int num) 
    {             
        // Creates a priority queue
        PriorityQueue<Tuple2od<U>> queue  = new PriorityQueue<>(numInitial*2, (x,y) -> 
        {
            return (int) Math.signum(y.v2 - x.v2);
        });
        List<U> allVertices = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        Set<U> visited = new HashSet<>();
        Set<U> stored = new HashSet<>();     
        
        // Selects the initial number of nodes.
        while(visited.size() < numInitial)
        {
            U node = allVertices.get(rng.nextInt(visited.size()));
            visited.add(node);
            stored.add(node);
        }
        
        // Store the neighborhoods, ordered by degree.
        for(U node : visited)
        {
            fullGraph.getNeighbourhood(node, orientation).forEach(neigh -> 
            {               
                if(!stored.contains(neigh))
                    queue.add(new Tuple2od<>(neigh, this.getValue(fullGraph, neigh)));
            });
        }    
        
        // While there are not enough nodes, extract one from the queue and continue.
        while(!queue.isEmpty() && visited.size() < num)
        {
            U next = queue.poll().v1();
            fullGraph.getNeighbourhood(next, orientation).forEach(neigh -> 
            {
                if(!stored.contains(neigh))
                    queue.add(new Tuple2od<>(neigh, this.getValue(fullGraph, neigh)));
            });
        }
        return visited;
    }

    /**
     * Obtains a pair node-value to add to the queue
     * @param fullGraph the full graph.
     * @param node the node.
     * @return a tuple containing the node and the value.
     */
    protected double getValue(Graph<U> fullGraph, U node)
    {
        if(fullGraph.isDirected())
        {
            return getDirectedValue((DirectedGraph<U>) fullGraph, node);
        }
        else
        {
            return getUndirectedValue((UndirectedGraph<U>) fullGraph, node);
        }
    }
    
    /**
     * Obtains a pair node-value to add to the queue (for directed graphs)
     * @param dgraph the original directed graph
     * @param node the node to add.
     * @return a tuple containing the node and the value.
     */
    protected double getDirectedValue(DirectedGraph<U> dgraph, U node)
    {
        if(orientation.equals(EdgeOrientation.IN))
        {
            return dgraph.inDegree(node);
        }
        else if(orientation.equals(EdgeOrientation.OUT))
        {
            return dgraph.outDegree(node);
        }
        else // if(orientation.equals(EdgeOrientation.UND)
        {
            return dgraph.inDegree(node) + dgraph.outDegree(node);
        }
    }
    
    /**
     * Obtains a pair node-value to add to the queue (for undirected graphs)
     * @param ugraph the original undirected graph
     * @param node the node to add.
     * @return a tuple containing the node and the value.
     */
    protected double getUndirectedValue(UndirectedGraph<U> ugraph, U node)
    {
        return ugraph.degree(node);
    }
    
    
}
