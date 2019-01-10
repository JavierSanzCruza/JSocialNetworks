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
* Finds a sample of a graph using the method known as Forest Fire Sampling. This method
* is equivalent to the BFS method, but taking edges with a given probability. In case of 
* directed networks, it is possible to determine a probability for incoming and outgoing neighbors.
 * 
 * In this version, only traversed edges are sampled, but the sample is oriented
 * to retrieve a fixed number of nodes.
 * 
* Lee, S., Kim, P., Jeong, H. Statistical properties of sampled networks. Physical Review E 73(1), 016102 (2006)
* 
* @author Javier Sanz-Cruzado Puig
* @param <U> type of users
*/
public class TraverseForestFireSampler<U> extends AbstractTraverseSearchSampler<U> 
{

    /**
     * Probability of selecting outgoing neighbors.
     */
    private final double probForward;
    /**
     * Probability of selecting incoming neighbors.
     */
    private final double probBackward;
    /**
     * Random number generator
     */
    
    private final Random rng;
    /**
     * Full constructor.
     * @param orientation neighborhood selection.
     * @param numInitial number of initial nodes.
     * @param probForward probability of selecting outgoing neighbors (between 0.0 and 1.0)
     * @param probBackward probability of selecting incoming neighbors (between 0.0 and 1.0)
     */
    private TraverseForestFireSampler(EdgeOrientation orientation, int numInitial, double probForward, double probBackward)
    {
        super(orientation, numInitial);
        this.probForward = probForward;
        this.probBackward = probBackward;
        this.rng = new Random();
    }
    
    /**
     * Constructor. It considers that all nodes have the same probability of
     * being sampled.
     * @param orientation neighborhood selection.
     * @param numInitial number of initial nodes.
     * @param prob probability that a neighbor node is selected (between 0.0 and 1.0).
     */
    public TraverseForestFireSampler(EdgeOrientation orientation, int numInitial, double prob) 
    {
        this(orientation, numInitial, prob, prob);
    }
    
    /**
     * Constructor. It considers that all neighbors (IN/OUT) are potentially sampled, but
     * with different probabilities. By default (undir. graphs), the probForward probability
     * will be used.
     * 
     * @param numInitial number of initial nodes.
     * @param probForward probability of selecting outgoing neighbors (between 0.0 and 1.0)
     * @param probBackward probability of selecting incoming neighbors (between 0.0 and 1.0)
     */
    public TraverseForestFireSampler(int numInitial, double probForward, double probBackward)
    {
        this(EdgeOrientation.UND, numInitial, probForward, probBackward);
    }

    @Override
    protected Collection<Tuple2oo<U,EdgeOrientation>> getChildren(Graph<U> fullGraph, U actual, List<U> allChildren) 
    {
        Set<Tuple2oo<U,EdgeOrientation>> children = new HashSet<>();
        
        for(U child : allChildren)
        {
            // If the graph is undirected, or the selected neighbors are the outgoing ones, use the forward probability
            if(!fullGraph.isDirected() || this.getOrientation().equals(EdgeOrientation.OUT))
            {
                if(rng.nextDouble() < this.probForward)
                {
                    children.add(new Tuple2oo<>(child, EdgeOrientation.OUT));
                }
            } // If the graph is directed and the selected neighbors are the incoming ones, use the backwards probability
            else if(this.getOrientation().equals(EdgeOrientation.IN))
            {
                if(rng.nextDouble() < this.probBackward)
                {
                    children.add(new Tuple2oo<>(child, EdgeOrientation.IN));
                }
            }
            else // In the undirected case, manage the outgoing children using the forward prob., and the incoming using the backwards prob.
            {
                boolean addedOut = false;
                boolean addedIn = false;
                if(fullGraph.containsEdge(actual, child)) //CASE 1: edge (actual, child) in the network
                {
                    if(rng.nextDouble() < this.probForward)
                    {
                        addedOut = true;
                    }
                }

                if(fullGraph.containsEdge(child, actual)) // CASE 2: edge (child, actual) in the network
                {
                    if(rng.nextDouble() < this.probBackward)
                    {
                        addedIn = true;
                    }
                }
                
                if(addedIn && addedOut)
                {
                    children.add(new Tuple2oo<>(child, EdgeOrientation.UND));
                }
                else if(addedIn)
                {
                    children.add(new Tuple2oo<>(child, EdgeOrientation.IN));
                }
                else if(addedOut)
                {
                    children.add(new Tuple2oo<>(child, EdgeOrientation.OUT));
                }
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
