/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.edge;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.exception.InexistentEdgeException;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.openide.util.Exceptions;

/**
 * Computes the embeddedness the edges of a graph
 * @author Javier Sanz-Cruzado Puig
 * @param <V> Type of the users in the graph
 */
public class EdgeWeight<V> implements EdgeMetric<V>
{
    /**
     * Constructor.
     */
    public EdgeWeight() 
    {
    }
    
    @Override
    public double compute(Graph<V> graph, V orig, V dest) throws InexistentEdgeException
    {
        if(graph.isMultigraph())
            return Double.NaN;
        
        if(graph.containsEdge(orig, dest))
        {
            return graph.getEdgeWeight(orig, dest);
        }
        
        throw new InexistentEdgeException("Edge between nodes " + orig + " and " + dest + " does not exist");
    }
    
    @Override
    public Map<Pair<V>, Double> compute(Graph<V> graph)
    {
        Map<Pair<V>, Double> values = new HashMap<>();
        if(!graph.isMultigraph())
        {
            graph.getAllNodes().forEach((orig) -> 
            {
               graph.getAdjacentNodes(orig).forEach(dest -> 
               {
                   try {
                       values.put(new Pair<>(orig, dest), this.compute(graph, orig, dest));
                   } catch (InexistentEdgeException ex) {
                       Exceptions.printStackTrace(ex);
                   }
               });
            });
        }
        return values;
    }

    @Override
    public Map<Pair<V>, Double> compute(Graph<V> graph, Stream<Pair<V>> edges) 
    {
        Map<Pair<V>, Double> values = new HashMap<>();
        edges.forEach(edge -> 
        {
            if(graph.containsEdge(edge.v1(), edge.v2()))
            {
                try
                {
                    values.put(edge, this.compute(graph, edge.v1(), edge.v2()));
                }
                catch(InexistentEdgeException e)
                {
                    values.put(edge, Double.NaN);
                }
            }
            else
            {
                values.put(edge, Double.NaN);
            }
        });
        return values;
    }
    
    @Override
    public double averageValue(Graph<V> graph) 
    {
        double value = this.compute(graph).values().stream().reduce(0.0, (acc, next)-> acc + next);
        return value/(graph.getEdgeCount()+0.0);
    }
    
    @Override 
    public double averageValue(Graph<V> graph, Stream<Pair<V>> edges, int edgeCount)
    {
        double value = edges.mapToDouble(edge -> 
        {
            try {
                return this.compute(graph, edge.v1(), edge.v2());
                
            } catch (InexistentEdgeException ex) {
                Exceptions.printStackTrace(ex);
                return 0.0;
            }
        }).sum();
        return value / (edgeCount + 0.0);
    }


        
}
