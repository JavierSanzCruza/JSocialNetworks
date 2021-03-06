/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.generator;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import java.util.Collection;

/**
 * Generates a subgraph from another graph, containing only a selection of nodes.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class SubGraphGenerator<U> implements GraphGenerator<U>
{
    /**
     * The original graph.
     */
    private Graph<U> graph;
    /**
     * The subset of users to keep.
     */
    private Collection<U> users;
    /**
     * Indicates if the generator has been configured or not.
     */
    private boolean configured = false;
    
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Object... configuration)
    {
        if(!(configuration == null) && configuration.length == 2)
        {
            Graph<U> graphAux = (Graph<U>) configuration[0];
            Collection<U> usersAux = (Collection<U>) configuration[1];
            
            
            this.configure(graphAux, usersAux);
        }
        else
        {
            configured = false;
        }
    }

    /**
     * Configures the generator.
     * @param graph The original graph.
     * @param users A collection containing the users we want in the final graph.
     */
    public void configure(Graph<U> graph, Collection<U> users)
    {
        this.graph = graph;
        this.users = users;
        this.configured = true;
    }

    @Override
    public Graph<U> generate() throws GeneratorNotConfiguredException, GeneratorBadConfiguredException
    {
        if(configured == false)
        {
            throw new GeneratorNotConfiguredException("SubGraph: The generator was not configured");
        }
        else if(graph == null || users == null)
        {
            throw new GeneratorBadConfiguredException("SubGraph: The generator was not correctly configured");
        }
        
        GraphGenerator<U> ggen = new EmptyGraphGenerator<>();
        ggen.configure(new Object[]{this.graph.isDirected(), this.graph.isWeighted()});
        Graph<U> subGraph = ggen.generate();
        
        this.users.forEach(u -> subGraph.addNode(u));
        
        this.graph.getAllNodes().forEach(u -> 
        {
            if(subGraph.containsVertex(u))
            {
                this.graph.getAdjacentNodes(u).forEach(v -> 
                {
                    if(subGraph.containsVertex(v)) //If u and v belong to the subgraph, copy the edge
                    {
                        subGraph.addEdge(u, v, this.graph.getEdgeWeight(u, v), this.graph.getEdgeType(u, v), false);
                    }
                });
            }
        });
        
        return subGraph;
    }
    
}
