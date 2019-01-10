/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.search.traverse;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Finds a sample of a graph using the method known as Snowball Sampling. This method
 * is equivalent to the BFS method, but taking only a fixed (maximum) number of connections
 * from each node.
 * 
 * In this version, only traversed edges are sampled, but the sample is oriented
 * to retrieve a fixed number of nodes.
 * 
 * Lee, S., Kim, P., Jeong, H. Statistical properties of sampled networks. Physical Review E 73(1), 016102 (2006)
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of users
 */
public class TraverseSnowballSampler<U> extends AbstractTraverseSearchSampler<U> 
{
    /**
     * Number of neighbors to select from the visited node.
     */
    private final int numNeighbors;

    /**
     * Random number generator.
     */
    private final Random rng = new Random();
    
    /**
     * Constructor.
     * @param orientation Neighborhood selection.
     * @param numInitialNodes Number of initial nodes.
     * @param numNeighbors Number
     */
    public TraverseSnowballSampler(EdgeOrientation orientation, int numInitialNodes, int numNeighbors)
    {
        super(orientation, numInitialNodes);
        this.numNeighbors = numNeighbors;
    }
    
    @Override
    protected Collection<Tuple2oo<U, EdgeOrientation>> getChildren(Graph<U> fullGraph, U actual, List<U> allChildren) 
    {
        int min = Math.min(allChildren.size(), this.numNeighbors);
        Set<Tuple2oo<U,EdgeOrientation>> children = new HashSet<>();
        while(children.size() < min)
        {
            int idx = rng.nextInt(allChildren.size());
            Tuple2oo<U, EdgeOrientation> child = new Tuple2oo<>(allChildren.get(idx), this.getOrientation());
            if(!children.contains(child))
            {
                children.add(child);
            }
        }
        return children;
    }
    
    @Override
    protected U nextActual(LinkedList<U> queue) 
    {
        return queue.pollFirst();
    }
}
