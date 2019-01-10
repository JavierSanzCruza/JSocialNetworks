/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface for user related metrics of graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public interface VertexMetric<U> 
{
    /**
     * Computes the value of the metric for a single user.
     * @param graph The graph.
     * @param user The user to compute.
     * @return the value of the metric.
     */
    public double compute(Graph<U> graph, U user);
    
    /**
     * Computes the value of the metric for all the users in the graph.
     * @param graph The graph.
     * @return A map relating the users with the values of the metric.
     */
    public default Map<U, Double> compute(Graph<U> graph)
    {
        Map<U, Double> res = new HashMap<>();
        graph.getAllNodes().forEach(u -> 
        {
            res.put(u,this.compute(graph,u));
        });
        return res;
    }
    /**
     * Computes the average value of the metric in the graph.
     * @param graph The graph.
     * @return the average value of the metric.
     */
    public default double averageValue(Graph<U> graph)
    {
        return averageValue(graph, graph.getAllNodes());
    }
    
    /**
     * Computes the average value of the metric for a set of users.
     * @param graph The graph.
     * @param users A stream of users.
     * @return the average value of the metric for those users.
     */
    public default double averageValue(Graph<U> graph, Stream<U> users)
    {
        Stream<U> filteredUsers = users.filter(graph::containsVertex);
        if(graph.getVertexCount() == 0 || users.equals(Stream.empty())) 
        {
            return 0.0;
        }
        
        return filteredUsers.mapToDouble(u -> this.compute(graph, u))
                            .average()
                            .getAsDouble();
    }
}
