/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.search.node;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.AbstractNodeSampler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class for search-based samplers, like BFS or SnowBall.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractNodeSearchSampler<U> extends AbstractNodeSampler<U> 
{
    /**
     * Number of initial nodes.
     */
    private final int numInitial;
    /**
     * Neighborhood selection
     */
    private final EdgeOrientation orientation;
    
    /**
     * Constructor.
     * @param orientation Neighborhood selection.
     * @param numInitial Number of initial nodes.
     */
    public AbstractNodeSearchSampler(EdgeOrientation orientation, int numInitial)
    {
        this.numInitial = numInitial;
        this.orientation = orientation;
    }
    
    
    @Override
    protected Collection<U> sampleNodes(Graph<U> fullGraph, int num) 
    {
        List<U> vertices = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        int max = vertices.size();
        Random r = new Random();
        LinkedList<U> queue = new LinkedList<>();

        while(queue.size() < this.numInitial)
        {
            int idx = r.nextInt(max);
            queue.add(vertices.get(idx));
        }
            
        U actual = queue.pollFirst();
        Set<U> visited = new HashSet<>();

        while(visited.size() < num)
        {
            if(visited.contains(actual))
            {
                if(queue.isEmpty() == false)
                {
                    actual = this.nextActual(queue);
                    continue;
                }
                else
                {
                    break;
                }
            }

            List<U> allChildren = fullGraph.getNeighbourhood(actual, orientation).collect(Collectors.toCollection(ArrayList::new));              
            Collection<U> children = this.getChildren(fullGraph, actual, allChildren);
            queue.addAll(children);

            if(visited.size() < num)
            {
                visited.add(actual);
                if(queue.isEmpty() == false)
                {
                    actual = this.nextActual(queue);
                }
                else
                {
                    break;
                }
            }
        }
        
        return visited;
    }
    /**
     * Given a list of children, this method selects a few.
     * @param fullGraph the original graph.
     * @param actual the currently visited node.
     * @param allChildren all children.
     * @return the list of children.
     */
    protected abstract Collection<U> getChildren(Graph<U> fullGraph, U actual, List<U> allChildren);
    
    /**
     * Given a queue, obtains the next node to visit, and removes it from the queue
     * @param queue the queue.
     * @return the next node to visit.
     */
    protected abstract U nextActual(LinkedList<U> queue);
    
    /**
     * Obtains the neighborhood selection.
     * @return the neighborhood selection.
     */
    public EdgeOrientation getOrientation()
    {
        return this.orientation;
    }
}
