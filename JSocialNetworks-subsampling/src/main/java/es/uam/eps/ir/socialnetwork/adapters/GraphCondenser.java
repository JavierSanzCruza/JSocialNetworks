/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
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
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Given a multigraph, condenses it into a simple graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class GraphCondenser
{
    /**
     * Transforms a multigraph into a weighted graph.
     * @param <U> type of the users.
     * @param graph the graph to condense.
     * @param considerWeights true if the final weight is the sum of the individual weights, false if it is just the number of them.
     * @param weightcalculator computes the weights.
     * @return the weighted graph condensing the multigraph. If the graph is not a multigraph, it is not modified.
     */
    public static <U> Graph<U> condense(Graph<U> graph, boolean considerWeights, Function<Stream<Double>, Double> weightcalculator)
    {
        try 
        {
            // If the graph is not a multigraph, then, it will stay the same
            if(graph.isMultigraph() == false)
            {
                return graph;
            }
            
            MultiGraph<U> multi = (MultiGraph<U>) graph;
            
            // Generate an empty graph
            GraphGenerator<U> ggen = new EmptyGraphGenerator<>();
            ggen.configure(graph.isDirected(), true);
            Graph<U> res = ggen.generate();
            
            // Add all nodes
            multi.getAllNodes().forEach(u ->
            {
                res.addNode(u);
            });
            
            // Add edges
            if(considerWeights) // If weights are considered, the weight of the edge will be equal to the sum.
            {
                multi.getAllNodes().forEach(u -> 
                {
                    multi.getAdjacentNodesWeightsLists(u).forEach(adj -> 
                    {
                        U v = adj.getIdx();
                        double sum = weightcalculator.apply(adj.getValue().stream());
                        res.addEdge(u, v, sum);
                    });
                });
            }
            else // Otherwise, we will just assign weight equal to 1 for each edge.
            {
                multi.getAllNodes().forEach(u -> 
                {
                   multi.getAdjacentNodesWeightsLists(u).forEach(adj -> 
                   {
                       U v = adj.getIdx();
                       double sum = weightcalculator.apply(adj.getValue().stream().map(x -> 1.0));
                       res.addEdge(u,v,sum);
                   });
                });
            }
            
            return res;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
        
    }
}
