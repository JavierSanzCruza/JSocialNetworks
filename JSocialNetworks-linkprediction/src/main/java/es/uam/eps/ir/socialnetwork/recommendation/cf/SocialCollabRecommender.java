/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.cf;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.ranksys.recommenders.nn.item.neighborhood.ItemNeighborhood;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhood;

/**
 * Social collaborative filtering for contact recommendation.
 * 
 * Cai, Xiongcai, Bain, M., Krzywicki, A., Wobcke, W., Kim, Y.S., Compton, P., Mahidadia, A. Collaborative Filtering for People to People Recommendation in Social Networks. 23rd Australasian Joint Conference (AI 2010), Adelaide, Australia, December 2010.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class SocialCollabRecommender<U> extends UserFastRankingRecommender<U> 
{
    
    private final UserNeighborhood<U> uNeigh;
    private final ItemNeighborhood<U> iNeigh;
    
    /**
     * Constructor.
     * @param graph Social network graph.
     * @param uNeigh Users with similar tastes
     * @param iNeigh Users with similar attractiveness
     */
    public SocialCollabRecommender(FastGraph<U> graph, UserNeighborhood<U> uNeigh, ItemNeighborhood<U> iNeigh) 
    {
        super(graph);
        this.uNeigh = uNeigh;
        this.iNeigh = iNeigh;
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx)
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        U u = this.uidx2user(uidx);
        
        Set<U> uInc = this.getGraph().getIncidentNodes(u).collect(Collectors.toCollection(HashSet::new));
        Set<U> uAdj = this.getGraph().getAdjacentNodes(u).collect(Collectors.toCollection(HashSet::new));
        
        this.iIndex.getAllIidx().forEach(iidx -> 
        {
            U v = this.iidx2item(iidx);
            int uidx2 = this.user2uidx(v);
            Set<U> neighbors = new HashSet<>();
            
            
            // Find the users with similar tastes to the candidate
            uNeigh.getNeighbors(v).forEach(str -> 
            {
                if(uAdj.contains(str.v1))
                {
                    neighbors.add(str.v1);
                }
            });
            
            // Find the users with similar attractiveness to the candidate
            iNeigh.getNeighbors(v).forEach(sar -> 
            {
                if(uInc.contains(sar.v1))
                {
                    neighbors.add(sar.v1);
                }
            });
            
            // The score is equal to the number of common neighbors
            scores.put(iidx, neighbors.size() + 0.0);
        });
        
        return scores;
    }    
}
