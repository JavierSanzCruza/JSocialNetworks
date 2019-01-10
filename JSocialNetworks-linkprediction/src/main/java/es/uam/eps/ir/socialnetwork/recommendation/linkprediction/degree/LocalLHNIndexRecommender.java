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
 * Recommender based on the first Leicht-Holme-Newman Index.
 * 
 * Leicht, E.A., Holme, P., Newman, M.E.J. Vertex similarity in networks. Physical Review E 73(2), 026120, February 2002.
 * Lü, L., Zhou. T. Link Prediction in Complex Networks: A survey. Physica A: Statistical Mechanics and its Applications, 390(6), March 2011, pp. 1150-1170.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users
 */
public class LocalLHNIndexRecommender<U> extends UserFastRankingRecommender<U> 
{

    /**
     * Link orientation for selecting the target user neighbours
     */
    private final EdgeOrientation uSel;
    /**
     * Link orientation for selecting the candidate user neighbours
     */
    private final EdgeOrientation vSel;
    
    /**
     * Constructor.
     * @param graph Social network graph
     * @param uSel Link orientation for selecting the target user neighbours.
     * @param vSel Link orientation for selecting the candidate user neighbours.
     */
    public LocalLHNIndexRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(-1.0);
        Set<U> uNeigh = this.getGraph().getNeighbourhood(this.uidx2user(uidx),uSel).collect(Collectors.toCollection(HashSet::new));
        this.uIndex.getAllUsers().forEach(v -> {
            long intersect = this.getGraph().getNeighbourhood(v, vSel).filter(uNeigh::contains).count();
            double denominator = uNeigh.stream().count()*this.getGraph().getNeighbourhood(v, vSel).count()+0.0;
            scoresMap.put(this.user2uidx(v), (intersect + 0.0) / (denominator + 1.0));
        });
        
        return scoresMap;
    }    

    
    
}
