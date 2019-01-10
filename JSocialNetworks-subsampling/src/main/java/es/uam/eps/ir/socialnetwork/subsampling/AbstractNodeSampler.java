/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import java.util.Collection;

/**
 * Abstract class for representing node samplers.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractNodeSampler<U> implements Sampler<U> 
{
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
            
            Collection<U> sampledNodes = this.sampleNodes(fullGraph, num);
            sampledNodes.forEach(u -> sample.addNode(u));
            sampledNodes.forEach(u -> 
            {
                sampledNodes.forEach(v -> 
                {
                    if(fullGraph.containsEdge(u, v))
                    {
                        sample.addEdge(u, v, fullGraph.getEdgeWeight(u, v), fullGraph.getEdgeType(u, v));
                    }
                });
            });
            
            return sample;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    
    /**
     * Samples a subset of nodes. 
     * @param fullGraph The original graph
     * @param num The number of nodes to retrieve
     * @return the sampled nodes.
     */
    protected abstract Collection<U> sampleNodes(Graph<U> fullGraph, int num);
}
