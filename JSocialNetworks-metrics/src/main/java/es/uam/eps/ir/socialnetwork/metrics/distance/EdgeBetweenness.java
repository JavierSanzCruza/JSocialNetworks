/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.exception.InexistentEdgeException;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Computes the edge betweenness of the graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class EdgeBetweenness<U> implements EdgeMetric<U> 
{

    /**
     * Distance calculator
     */
    private final DistanceCalculator<U> dc;
    
    /**
     * Constructor.
     * @param dc distance calculator.
     */
    public EdgeBetweenness(DistanceCalculator<U> dc)
    {
        this.dc = dc;
    }
    
    /**
     * Constructor.
     */
    public EdgeBetweenness()
    {
        this.dc = new DistanceCalculator<>();
    }
    
    
    @Override
    public double compute(Graph<U> graph, U orig, U dest) throws InexistentEdgeException 
    {
        this.dc.computeDistances(graph);
        if(graph.containsEdge(orig, dest))
        {
            return this.dc.getEdgeBetweenness(orig, dest);
        }
        
        throw new InexistentEdgeException("The edge " + orig + " and " + dest + " does not exist");
    }
    
    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph, Stream<Pair<U>> edges) 
    {
        Map<Pair<U>, Double> values = new HashMap<>();
        this.dc.computeDistances(graph);
        
        edges.forEach(edge -> 
        {
            if(graph.containsEdge(edge.v1(), edge.v2()))
            {
                values.put(edge, dc.getEdgeBetweenness(edge.v1(), edge.v2()));
            }
            else
            {
                values.put(edge, Double.NaN);
            }
        });
        return values;
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph) {
        Map<Pair<U>, Double> values = new HashMap<>();
        this.dc.computeDistances(graph);
        Map<U,Map<U,Double>> betw = this.dc.getEdgeBetweenness();
        
        graph.getAllNodes().forEach(u -> {
            graph.getAdjacentNodes(u).forEach(v ->{
                values.put(new Pair<>(u,v), betw.get(u).get(v));
            });
        });
        
        return values;
    }

    @Override
    public double averageValue(Graph<U> graph) 
    {
        if(graph.getEdgeCount() > 0L)
        {
            return this.compute(graph).values().stream().mapToDouble(val -> val).average().getAsDouble();           
        }
        return 0.0;
    }

    @Override
    public double averageValue(Graph<U> graph, Stream<Pair<U>> edges, int edgeCount) 
    {
        if(graph.getEdgeCount() > 0L)
        {
            this.dc.computeDistances(graph);
            double value = edges.mapToDouble(edge -> {
                if(graph.containsEdge(edge.v1(), edge.v2()))
                    return dc.getEdgeBetweenness(edge.v1(), edge.v2());
                return 0.0;
            }).sum();
            return value/edgeCount;
        }
        return 0.0;
    }


    
}
