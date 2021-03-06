/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import es.uam.eps.ir.socialnetwork.metrics.distance.modes.ASLMode;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Computes the Average Shortest path Length of graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class ASL<U> implements GraphMetric<U>
{
    /**
     * Calculation mode
     */
    private final ASLMode mode;
    /**
     * Calculates the distances
     */
    private final DistanceCalculator<U> dc;
    
    /**
     * Constructor
     * @param dc distance calculator.
     * @param mode the calculation mode.
     */
    public ASL(DistanceCalculator<U> dc, ASLMode mode)
    {
        this.mode = mode;
        this.dc = dc;
    }
    
    /**
     * Constructor. Applies the default mode (Averages over all the pairs of distinct nodes without infinite distances
     * @param dc distance calculator.
     */
    public ASL(DistanceCalculator<U> dc)
    {
        this(dc, ASLMode.NONINFINITEDISTANCES);
    }
    
    /**
     * Constructor
     * @param mode the calculation mode.
     */
    public ASL(ASLMode mode)
    {
        this(new DistanceCalculator<>(), mode);
    }
    
    /**
     * Default constructor. Applies the default mode (Averages over all the pairs of distinct nodes without infinite distances
     */
    public ASL()
    {
        this(new DistanceCalculator<>(), ASLMode.NONINFINITEDISTANCES);
    }
        
    @Override
    public double compute(Graph<U> graph) 
    {
        if(graph.getVertexCount() <= 1L || graph.getEdgeCount() == 0L)
            return 0.0;
        
        this.dc.computeDistances(graph);
        double asl = -1.0;
        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        switch(this.mode)
        {
            case NONINFINITEDISTANCES: // Averages over the pairs of distinct nodes without infinite distances
                asl = this.dc.getDistances().entrySet().stream().mapToDouble(entry -> 
                {
                    return entry.getValue().entrySet().stream().mapToDouble(dist -> {
                        if(dist.getValue().isInfinite() || dist.getValue().equals(0.0))
                            return 0.0;
                        counter.incrementAndGet();
                        return dist.getValue();
                    }).sum();
                }).sum();
                asl /= (counter.get() + 0.0);
            break;
            case COMPONENTS: // Computes the metric for each strongly connected component, and averages
                Communities<U> scc = this.dc.getSCC();
                asl = scc.getCommunities().mapToDouble(comm -> 
                {
                    long users = scc.getUsers(comm).count();
                    double aux = scc.getUsers(comm).mapToDouble(u -> {
                        return scc.getUsers(comm).mapToDouble(v -> this.dc.getDistances(u, v)).sum();
                    }).sum();
                    if(users > 1L)
                        return aux/((users+0.0)*(users-1.0));
                    return 0;
                }).average().getAsDouble();
            break;
            
        }
        
        return asl;
    }
    
}
