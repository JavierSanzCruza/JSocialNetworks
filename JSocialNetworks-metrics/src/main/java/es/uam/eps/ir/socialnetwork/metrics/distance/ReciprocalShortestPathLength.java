/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Computes the Reciprocal Shortest Path Length.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class ReciprocalShortestPathLength<U> implements PairMetric<U>
{
    /**
     * Distance calculator.
     */
    private final DistanceCalculator<U> dc;

    /**
     * Constructor.
     */
    public ReciprocalShortestPathLength() 
    {
        this.dc = new DistanceCalculator<>();
    }

    /**
     * Constructor.
     * @param dc distance calculator.
     */
    public ReciprocalShortestPathLength(DistanceCalculator<U> dc) 
    {
        this.dc = dc;
    }

    @Override
    public double compute(Graph<U> graph, U orig, U dest) 
    {
        if(!graph.containsVertex(orig) || !graph.containsVertex(dest))
        {
            return Double.NaN;
        }
        dc.computeDistances(graph);
        Double dist = dc.getDistances(orig, dest);
        if(dist.isInfinite())
            return 0.0;
        else if(dist.equals(0.0) || orig.equals(dest))
            return Double.POSITIVE_INFINITY;
        else
            return dist;
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph) 
    {
        Map<Pair<U>, Double> map = new HashMap<>();
        graph.getAllNodes().forEach(u -> 
        {
            graph.getAllNodes().forEach(v -> 
            {
                if(!u.equals(v))
                    map.put(new Pair<>(u,v), this.compute(graph, u, v));
            });
        });
        
        return map;       
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph, Stream<Pair<U>> pairs) 
    {
        Map<Pair<U>, Double> map = new HashMap<>();
        pairs.forEach(pair -> map.put(pair, this.compute(graph, pair.v1(), pair.v2())));
        return map;
    }

    @Override
    public double averageValue(Graph<U> graph) 
    {
        double sum = graph.getAllNodes().mapToDouble(u -> 
        {
            return graph.getAllNodes().mapToDouble(v -> 
            {
                if(u.equals(v)) return 0;
                else return this.compute(graph, u, v);
            }).sum();
        }).sum();
        
        double numN = graph.getVertexCount() + 0.0;
        return sum/(numN*(numN-1));
    }

    @Override
    public double averageValue(Graph<U> graph, Stream<Pair<U>> pairs, int pairCount) 
    {
        return 1.0/(pairCount+0.0)*pairs.mapToDouble(pair -> this.compute(graph, pair.v1(), pair.v2())).sum();
    }


    
}
