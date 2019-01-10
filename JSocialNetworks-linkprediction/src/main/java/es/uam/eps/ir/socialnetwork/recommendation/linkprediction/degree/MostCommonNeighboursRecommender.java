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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Most Common Neighbours (MCN) recommender. Recommends the users that share the maximum number of neighbours with the target user.
 * 
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks. Journal of the American Society for Information Science and Technology 58(7), May 2007.
 * Newman, M.E.J. Clustering and Preferential Attachment in Growing Networks. Physical Review Letters E, 64(025102), April 2001.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users.
 */
public class MostCommonNeighboursRecommender<U> extends UserFastRankingRecommender<U> 
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
     * Constructor.
     * @param graph User graph.
     * @param uSel Link orientation for the target users.
     * @param vSel Link orientation for the candidate users.
     */
    public MostCommonNeighboursRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) {
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
        this.uIndex.getAllUidx().forEach(iidx -> 
        {
            scoresMap.put(iidx, 
                    this.getGraph().getNeighbourhood(this.uidx2user(iidx), vSel).filter(uNeigh::contains).count() + 0.0);
        });
        
        return scoresMap;
    }

    
    
}
