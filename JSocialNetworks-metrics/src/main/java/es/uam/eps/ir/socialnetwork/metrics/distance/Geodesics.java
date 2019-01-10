/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
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
 * Measures the number of geodesic paths between two different nodes in the network.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class Geodesics<U> implements PairMetric<U> 
{
    /**
     * Distance calculator.
     */
    private final DistanceCalculator<U> dc;
    
    /**
     * Constructor
     */
    public Geodesics()
    {
        dc = new DistanceCalculator<>();
    }
    
    /**
     * Constructor
     * @param dc distance calculator.
     */
    public Geodesics(DistanceCalculator<U> dc)
    {
        this.dc = dc;
    }
    
    @Override
    public double compute(Graph<U> graph, U orig, U dest) 
    {
        this.dc.computeDistances(graph);
        return this.dc.getGeodesics(orig, dest);
    }

    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph) 
    {
        Map<Pair<U>,Double> pairs = new HashMap<>();
        this.dc.computeDistances(graph);
        
        graph.getAllNodes().forEach(u -> {
            graph.getAllNodes().forEach(v -> {
                pairs.put(new Pair<>(u,v), this.dc.getGeodesics(u, v));
            });
        });
        return pairs;
    }
    
    @Override
    public Map<Pair<U>, Double> compute(Graph<U> graph, Stream<Pair<U>> pairs) 
    {
        Map<Pair<U>, Double> values = new HashMap<>();
        this.dc.computeDistances(graph);
        
        pairs.forEach(pair -> {
            values.put(pair, this.dc.getGeodesics(pair.v1(), pair.v2()));
        });
        return values;
    }

    @Override
    public double averageValue(Graph<U> graph) 
    {
        if(graph.getVertexCount() > 1)
            return this.compute(graph).values().stream().mapToDouble(val -> val).average().getAsDouble();
        else return 0.0;
    }

    @Override
    public double averageValue(Graph<U> graph, Stream<Pair<U>> pairs, int pairCount) 
    {
        this.dc.computeDistances(graph);
        if(graph.getVertexCount() > 1)
            return pairs.mapToDouble(pair -> this.dc.getGeodesics(pair.v1(), pair.v2())).average().getAsDouble();
        return 0.0;
    }

    
    
}
