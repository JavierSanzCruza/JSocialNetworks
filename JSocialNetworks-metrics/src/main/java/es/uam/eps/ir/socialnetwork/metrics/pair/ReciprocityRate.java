/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.pair;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Checks if a graph has the reciprocal edge of a pair.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class ReciprocityRate<U> implements PairMetric<U> 
{
    @Override
    public double compute(Graph<U> graph, U orig, U dest) 
    {
        if(graph.containsEdge(dest, orig))
            return 1.0;
        else
            return 0.0;
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph) 
    {
        Map<Pair<U>, Double> res = new HashMap<>();
        graph.getAllNodes().forEach(u -> 
        {
            graph.getAllNodes().forEach(v -> 
            {
                res.put(new Pair<>(u,v), this.compute(graph, u, v));
            });
        });
        return res;
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph, Stream<Pair<U>> pairs) 
    {
        Map<Pair<U>, Double> res = new HashMap<>();
        pairs.forEach(pair -> {
            res.put(pair, this.compute(graph, pair.v1(), pair.v2()));
        });
        return res;
    }

    @Override
    public double averageValue(Graph<U> graph) 
    {
        Map<Pair<U>, Double> res = this.compute(graph);
        return res.entrySet().stream().mapToDouble(entry -> entry.getValue()).average().getAsDouble();
    }

    @Override
    public double averageValue(Graph<U> graph, Stream<Pair<U>> pairs, int pairCount) 
    {
        return pairs.mapToDouble(pair -> this.compute(graph, pair.v1(), pair.v2())).average().getAsDouble();
    }
    
}
