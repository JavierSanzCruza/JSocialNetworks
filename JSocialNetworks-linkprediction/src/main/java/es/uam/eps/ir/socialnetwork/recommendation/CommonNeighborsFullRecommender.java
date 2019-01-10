/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recommender that uses an implementation similar to a term-based query processing task from search. This recommenders just take users at
 * two steps away from the target user.
 * 
 * Büttcher, S., Clarke, C.L.A. and Cormack, G.V. Information retrieval: implementing and evaluating search engines. The MIT Press, 2010.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class CommonNeighborsFullRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Neighbour selection for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighbour selection for the candidate user.
     */
    private final EdgeOrientation vSel;
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel neighbour selection for the target user
     * @param vSel neighbour selection for the candidate user
     */
    public CommonNeighborsFullRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx)
    {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);

        Set<U> uNeigh = this.getGraph().getNeighbourhood(this.uidx2user(uidx), uSel).collect(Collectors.toCollection(HashSet::new));
       
        uNeigh.forEach(w -> 
        {
            this.getGraph().getNeighbourhood(w, vSel.invertSelection()).forEach(v -> scoresMap.put(this.user2uidx(v), scoresMap.getOrDefault(this.user2uidx(v), scoresMap.defaultReturnValue()) + this.getValue(uidx, this.user2uidx(v), w)));
        });
       
        return scoresMap;
    }
    
    /**
     * Obtains the neighbour selection for the target user
     * @return the neighbour selection for the target user
     */
    protected EdgeOrientation getTargetOrientation()
    {
        return this.uSel;
    }
    
    /**
     * Obtains the neighbour selection for the candidate user
     * @return the neighbour selection for the candidate user
     */
    protected EdgeOrientation getCandidateOrientation()
    {
        return this.vSel;
    }
    
    /**
     * Obtains the recommendation value for a pair of target/candidate users.
     * @param uidx identifier for the target user.
     * @param vidx identifier for the candidate user.
     * @param w the intermediate user between the target and the candidate.
     * @return the corresponding value.
     */
    protected abstract double getValue(int uidx, int vidx, U w);
}
