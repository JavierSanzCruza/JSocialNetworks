/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Recommends the most popular items at distance two from the target user.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class DistanceTwoPopularityRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Neighborhood selection for the target user
     */
    private final EdgeOrientation uSel;
    /**
     * Neighborhood selection for the candidate users.
     */
    private final EdgeOrientation vSel;
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel the neighborhood selection for the target user
     * @param vSel the neighborhood selection for the candidate user
     */
    public DistanceTwoPopularityRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        U u = this.uidx2user(uidx);
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        
        // Determine the set of candidates.
        Set<U> candidates = new HashSet<>();
        
        this.getGraph().getNeighbourhood(u, uSel).forEach(w -> this.getGraph().getNeighbourhood(w, vSel.invertSelection()).forEach(v -> candidates.add(v)));
        
        this.getGraph().getAllNodes().forEach(v -> 
        {
            if(candidates.contains(v)) scores.put(this.item2iidx(v), this.getGraph().degree(v, EdgeOrientation.IN) + 0.0);
            else scores.put(this.item2iidx(v), 0.0);
        });
        
        return scores;
    }
}
