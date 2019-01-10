/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.generator;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedUnweightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedWeightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastUndirectedUnweightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastUndirectedWeightedGraph;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;

/**
 * Empty graph generator.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class EmptyGraphGenerator<U> implements GraphGenerator<U>
{
    /**
     * Indicates if the graph is going to be directed.
     */
    private boolean directed;
    /**
     * Indicates if the graph has been configured.
     */
    private boolean configured = false;
    /**
     * Indicates if the graph has weights.
     */
    private boolean weighted;
    
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Object... configuration) 
    {
        if(!(configuration == null) && configuration.length == 2)
        {
            boolean auxDirected = (boolean) configuration[0];
            boolean auxWeighted = (boolean) configuration[1];
            
            
            this.configure(auxDirected, auxWeighted);
        }
        else
        {
            configured = false;
        }
        
    }
    
    /**
     * Configures the graph
     * @param directed if the graph is going to be directed.
     * @param weighted if the graph is going to be weighted.
     */
    public void configure(boolean directed, boolean weighted)
    {
        this.directed = directed;
        this.weighted = weighted;
        this.configured = true;
    }
    
    @Override
    public Graph<U> generate() throws GeneratorNotConfiguredException, GeneratorBadConfiguredException
    {
        if(configured == false)
            throw new GeneratorNotConfiguredException("Empty graph: The generator was not configured");
        
        Graph<U> graph;
        if(directed)
            if(weighted)
                graph = new FastDirectedWeightedGraph<>();
            else
                graph = new FastDirectedUnweightedGraph<>();
        else
            if(weighted)
                graph = new FastUndirectedWeightedGraph<>();
            else
                graph = new FastUndirectedUnweightedGraph<>();
        
        return graph; 
    }
}
