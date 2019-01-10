/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contact recommender based on Adamic coefficient.
 * 
 * Adamic, L. Adar, A. Friends and Neighbours on the Web. Social Networks Journal, 2003.
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks, CIKM, 2003
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users.
 */
public class AdamicRecommender<U> extends UserFastRankingRecommender<U>
{
    /**
     * Link orientation for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Link orientation for the candidate user.
     */
    private final EdgeOrientation vSel;
    /**
     * Link orientation for the common neighbours of the target and candidate users.
     */
    private final EdgeOrientation wSel;
    
    /**
     * Common neighbours cache
     */
    private final Map<U, Double> cache;
    
    /**
     * Constructor of the Adamic recommender.
     * @param graph Graph user graph.
     * @param uSel Link orientation for the target users
     * @param vSel Link orientation for the candidate users.
     * @param wSel Link orientation for the common neighbours
     */
    public AdamicRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation wSel) {
        super(graph);
        
        this.uSel = uSel;
        this.vSel = vSel;
        this.wSel = wSel;
        this.cache = new HashMap<>();
        this.getGraph().getAllNodes().forEach(u -> 
        {
            if(u == null)
                System.err.println(uSel + "_" + vSel + "_" + wSel + " error");
            
            Double val = this.getGraph().getNeighbourhood(u,wSel).count() + 0.0;
            this.cache.put(u, val);
        });
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(Double.NEGATIVE_INFINITY);
        Set<U> uNeigh = this.getGraph().getNeighbourhood(this.uidx2user(uidx), uSel).collect(Collectors.toCollection(HashSet::new));
        
        
        this.uIndex.getAllUidx().forEach(iidx -> 
        {
            scoresMap.put(iidx, 
                // Adamic Score
                this.getGraph().getNeighbourhood(this.uidx2user(iidx), vSel).filter(uNeigh::contains) //Get the common neighbours
                .mapToDouble(w -> 
                {
                    
                    double val = 0.0;
                    if(w != null)
                    {
                        if(this.cache.containsKey(w) && this.cache.get(w) != null)
                        {
                            val = this.cache.get(w);
                        }
                        else
                        {
                            val = this.getGraph().getNeighbourhoodSize(w, wSel);
                            this.cache.put(w, val);
                        }
                    }
                    
                    return 1.0/Math.log(val + 2.0);
                    
                }).sum()*Math.log(2.0)); // Compute the inverse of their number
        });
        
        return scoresMap;
    }    
}
