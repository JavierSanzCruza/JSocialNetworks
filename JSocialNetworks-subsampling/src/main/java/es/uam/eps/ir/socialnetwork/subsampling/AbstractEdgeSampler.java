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
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.Collection;

/**
 * Abstract class for representing node samplers.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractEdgeSampler<U> implements Sampler<U> 
{
    
    @Override
    public Graph<U> sample(Graph<U> fullGraph, double percentage) 
    {
        return this.sample(fullGraph, new Double(Math.ceil(percentage*fullGraph.getEdgeCount())).intValue());
    }
    
    @Override
    public Graph<U> sample(Graph<U> fullGraph, int num)
    {
        try 
        {
            GraphGenerator<U> graphgen = new EmptyGraphGenerator<>();
            graphgen.configure(fullGraph.isDirected(), fullGraph.isWeighted());
            Graph<U> sample = graphgen.generate();
            
            if(num > fullGraph.getEdgeCount()) // If the graph needs more edges than the current number
            {
                return fullGraph;
            }
            else if(num < 0) // If the graph needs less than 0 edges, return an empty graph.
            {
                return sample;
            }
            
            Collection<Pair<U>> sampledEdges = this.sampleEdges(fullGraph, num);
            sampledEdges.forEach(e -> 
            {
                U u = e.v1();
                U v = e.v2();
                sample.addEdge(u,v,fullGraph.getEdgeWeight(u,v), fullGraph.getEdgeType(u, v), true);
            });
            
            return sample;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    
    /**
     * Samples a subset of edges. 
     * @param fullGraph The original graph
     * @param num The number of edges to retrieve
     * @return the sampled edges.
     */
    protected abstract Collection<Pair<U>> sampleEdges(Graph<U> fullGraph, int num);
}
