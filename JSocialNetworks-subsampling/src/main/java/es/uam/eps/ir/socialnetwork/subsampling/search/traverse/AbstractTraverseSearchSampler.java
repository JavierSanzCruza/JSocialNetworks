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
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.subsampling.Sampler;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
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
 * 
 * In this version, only traversed edges are sampled, but the sample is oriented
 * to retrieve a fixed number of nodes.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractTraverseSearchSampler<U> implements Sampler<U> 
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
     * Constructor
     * @param orientation neighborhood selection.
     * @param numInitial number of random seeds.
     */
    public AbstractTraverseSearchSampler(EdgeOrientation orientation, int numInitial)
    {
        this.numInitial = numInitial;
        this.orientation = orientation;
    }
    
    
    @Override
    public Graph<U> sample(Graph<U> fullGraph, double percentage) 
    {
        return this.sample(fullGraph, new Double(Math.ceil(percentage*fullGraph.getVertexCount())).intValue());
    }

    @Override
    public Graph<U> sample(Graph<U> fullGraph, int num) 
    {
        try 
        {
            GraphGenerator<U> graphgen = new EmptyGraphGenerator<>();
            graphgen.configure(fullGraph.isDirected(), fullGraph.isWeighted());
            Graph<U> sample = graphgen.generate();
            
            if(num > fullGraph.getVertexCount()) // If the graph needs more edges than the current number
            {
                return fullGraph;
            }
            else if(num < 0) // If the graph needs less than 0 edges, return an empty graph.
            {
                return sample;
            }
            
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
            
            List<Pair<U>> edges = new ArrayList<>();

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
                Collection<Tuple2oo<U,EdgeOrientation>> children = this.getChildren(fullGraph, actual, allChildren);
                
                List<U> defChildren = new ArrayList<>();
                for(Tuple2oo<U,EdgeOrientation> child : children)
                {
                    defChildren.add(child.v1());
                    if(!fullGraph.isDirected() || this.getOrientation().equals(EdgeOrientation.OUT) || (this.getOrientation().equals(EdgeOrientation.UND)) && child.v2().equals(EdgeOrientation.OUT))
                    {
                        edges.add(new Pair<>(actual, child.v1()));
                    }
                    else if(this.getOrientation().equals(EdgeOrientation.IN) || (this.getOrientation().equals(EdgeOrientation.UND)) && child.v2().equals(EdgeOrientation.IN))
                    {
                        edges.add(new Pair<>(child.v1(), actual));
                    }
                    else // if(this.getOrientation().equals(EdgeOrientation.UND) && child.v2().equals(EdgeOrientation.UND)
                    {
                        if(fullGraph.containsEdge(actual, child.v1()))
                            edges.add(new Pair<>(actual, child.v1()));
                        if(fullGraph.containsEdge(child.v1(), actual))
                            edges.add(new Pair<>(child.v1(), actual));
                    }
                }
                queue.addAll(defChildren);

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
            
            visited.forEach(u -> sample.addNode(u));
            edges.forEach(e -> 
            {
                U u = e.v1();
                U v = e.v2();
                
                if(visited.contains(u) && visited.contains(v))
                {
                    sample.addEdge(u, v, fullGraph.getEdgeWeight(u, v), fullGraph.getEdgeType(u, v));
                }
            });
            
            
            return sample;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    
    /**
     * Given a list of children, this method selects a few.
     * @param fullGraph the original graph.
     * @param actual the node the algorithm is currently visiting.
     * @param allChildren all children.
     * @return the list of children, along with the direction of the selected edge.
     */
    protected abstract Collection<Tuple2oo<U,EdgeOrientation>> getChildren(Graph<U> fullGraph, U actual, List<U> allChildren);
    
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
