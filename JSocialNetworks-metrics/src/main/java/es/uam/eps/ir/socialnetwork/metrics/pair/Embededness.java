/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.pair;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes the embeddedness the edges of a graph
 * @author Javier Sanz-Cruzado Puig
 * @param <V> Type of the users in the graph
 */
public class Embededness<V> implements PairMetric<V>
{
    /**
     * Selection of the neighbours of the first node.
     */
    private final EdgeOrientation uSel;
    /**
     * Selection of the neighbour of the second node.
     */
    private final EdgeOrientation vSel;

    /**
     * Constructor.
     * @param uSel Selection of the neighbours of the first node.
     * @param vSel Selection of the neighbours of the second node.
     */
    public Embededness(EdgeOrientation uSel, EdgeOrientation vSel) {
        this.uSel = uSel;
        this.vSel = vSel;
    }
    
    @Override
    public double compute(Graph<V> graph, V orig, V dest)
    {
        if(graph.isMultigraph())
            return Double.NaN;
        
        
        Set<V> firstNeighbours = graph.getNeighbourhood(orig, uSel).collect(Collectors.toCollection(HashSet::new));
        Set<V> secondNeighbours = graph.getNeighbourhood(dest, vSel).collect(Collectors.toCollection(HashSet::new));
        firstNeighbours.remove(dest);
        secondNeighbours.remove(orig);

        Set<V> intersection = new HashSet<>(firstNeighbours);
        intersection.retainAll(secondNeighbours);

        if(firstNeighbours.isEmpty() && secondNeighbours.isEmpty())
        {
            return 0.0;
        }
        else
        {
            return (intersection.size() + 0.0)/(firstNeighbours.size() + secondNeighbours.size() - intersection.size() + 0.0);
        }
    }
    
    @Override
    public Map<Pair<V>, Double> compute(Graph<V> graph)
    {
        Map<Pair<V>, Double> values = new HashMap<>();
        if(!graph.isMultigraph())
        {
            graph.getAllNodes().forEach((orig) -> 
            {
               graph.getAllNodes().forEach(dest -> values.put(new Pair<>(orig, dest), this.compute(graph, orig, dest)));
            });
        }
        return values;
    }
    
    @Override
    public Map<Pair<V>, Double> compute(Graph<V> graph, Stream<Pair<V>> pairs) 
    {
        Map<Pair<V>, Double> values = new HashMap<>();
        if(!graph.isMultigraph())
        {
            pairs.forEach(pair -> 
            {
                values.put(pair, this.compute(graph, pair.v1(), pair.v2()));
            });
        }
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
        double value = edges.mapToDouble(edge -> this.compute(graph, edge.v1(), edge.v2())).sum();
        return value / (edgeCount + 0.0);
    }

    
        
}
