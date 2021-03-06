/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.twitter;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Closure recommender. Recommends reciprocal edges according to the number of common neighbors between
 * the already existing edge endpoints.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class ClosureRecommender<U> extends UserFastRankingRecommender<U> 
{
    
    /**
     * Constructor.
     * @param graph Graph.

     */
    public ClosureRecommender(FastGraph<U> graph) 
    {
        super(graph);

    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        
        U u = this.uidx2user(uidx);
        Set<U> uWs = this.getGraph().getAdjacentNodes(u).collect(Collectors.toCollection(HashSet::new));
        this.uIndex.getAllUidx().forEach(iidx -> 
        {
            U v = this.uidx2user(iidx);
            if(this.getGraph().containsEdge(v, u))
            {
                Set<U> vWs = this.getGraph().getIncidentNodes(v).collect(Collectors.toCollection(HashSet::new));
                if(uWs != null && vWs != null)
                {
                    vWs.retainAll(uWs);
                    scoresMap.put(iidx, vWs.size() + 0.0);
                }
                else
                {
                    scoresMap.put(iidx, 0.0);
                }
            }
            else
            {
                scoresMap.put(iidx, 0.0);
            }
        });

        return scoresMap;
    }

    @Override
    public boolean containsUser(U u) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<U> getAllUsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsItem(U i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<U> getAllItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
