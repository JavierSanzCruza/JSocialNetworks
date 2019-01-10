/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.adapters;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;

/**
 * Methods for obtaining graphs fulfilling some properties, starting from a given graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class Adapters
{
    /**
     * Given a graph, obtains a version of it without autoloops.
     * @param <U> Type of the users.
     * @param graph the original graph.
     * @return a graph without autoloops.
     */
    public static <U> Graph<U> removeAutoloops(Graph<U> graph) 
    {
        try
        {
            GraphGenerator<U> ggen = new EmptyGraphGenerator<>();
            ggen.configure(graph.isDirected(), graph.isWeighted());
            Graph<U> auxGraph = ggen.generate();
            
            graph.getAllNodes().forEach(u -> auxGraph.addNode(u));
            graph.getAllNodes().forEach(u -> 
            {
                graph.getAdjacentNodesWeights(u).filter(v -> !u.equals(v.getIdx())).forEach(v -> 
                {
                    double weight = v.getValue();
                    int type = graph.getEdgeType(u, v.getIdx());
                    auxGraph.addEdge(u, v.getIdx(), weight, type);
                });
            });
            
            return graph;
        }
        catch(GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
        
    }
    
    /**
     * Given a graph, obtains a version of the graph with all the possible autoloops.
     * @param <U> Type of the users
     * @param graph the original graph
     * @return a graph with all the autoloops added.
     */
    public static <U> Graph<U> addAllAutoloops(Graph<U> graph) 
    {
        try
        {
            GraphGenerator<U> ggen = new EmptyGraphGenerator<>();
            ggen.configure(graph.isDirected(), graph.isWeighted());
            Graph<U> auxGraph = ggen.generate();
            
            graph.getAllNodes().forEach(u -> 
            {
                auxGraph.addNode(u);
                auxGraph.addEdge(u, u);
            });
            graph.getAllNodes().forEach(u -> 
            {
                graph.getAdjacentNodesWeights(u).forEach(v -> 
                {
                    double weight = v.getValue();
                    int type = graph.getEdgeType(u, v.getIdx());
                    auxGraph.addEdge(u, v.getIdx(), weight, type);
                });
            });
            
            return graph;
        }
        catch(GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
        
    }
    
    public static <U> Graph<U> onlyTrainUsers(Graph<U> graph, Graph<U> trainingGraph)
    {
        try
        {
            GraphGenerator<U> ggen = new EmptyGraphGenerator<>();
            ggen.configure(graph.isDirected(), graph.isWeighted());
            Graph<U> auxGraph = ggen.generate();
            
            trainingGraph.getAllNodes().forEach(u -> 
            {
                auxGraph.addNode(u);
            });

            graph.getAllNodes().forEach(u -> 
            {
                if(auxGraph.containsVertex(u))
                {
                    graph.getAdjacentNodesWeights(u).forEach(v -> 
                    {
                        if(auxGraph.containsVertex(v.getIdx()))
                        {
                            auxGraph.addEdge(u, v.getIdx(), v.getValue());
                        }
                    });
                }
            });
            
            return auxGraph;
        }
        catch(GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }   
}
