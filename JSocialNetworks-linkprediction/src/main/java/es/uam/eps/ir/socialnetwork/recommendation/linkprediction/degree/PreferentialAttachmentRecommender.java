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

/**
 * Preferential Attachment recommender. Recommender based on the Preferential Attachment link prediction method.
 * When the selected neighbourhood is formed by the incoming nodes, then this method is equal to the Popularity
 * recommender method.
 * 
 * Newman, M.E.J. Clustering and Preferential Attachment in Growing Networks. Physical Review Letters E, 64(025102), April 2001.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> Type of the users.
 */
public class PreferentialAttachmentRecommender<U> extends UserFastRankingRecommender<U> 
{

    /**
     * Link orientation for selecting the neighbours of the target node
     */
    private final EdgeOrientation uSel;
    
    /**
     * Link orientation for selecting the neighbours of the candidate node.
     */
    private final EdgeOrientation vSel;
    
    /**
     * Indicates if the mode is link prediction
     */
    private final boolean linkprediction;
    
    /**
     * Constructor for recommendation mode.

     * @param graph Graph.
     * @param vSel Link orientation for selecting the neighbours of the candidate node.
     */
    public PreferentialAttachmentRecommender(FastGraph<U> graph, EdgeOrientation vSel) 
    {
        super(graph);
        this.vSel = vSel;
        this.uSel = EdgeOrientation.UND;
        linkprediction = false;
    }
    
    /**
     * Constructor for link prediction mode.
     * @param graph Graph.
     * @param uSel Link orientation for selecting the neighbours of the target node
     * @param vSel Link orientation for selecting the neighbours of the candidate node
     */
    public PreferentialAttachmentRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
        linkprediction = true;
    }
    
    

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        U u = this.uidx2user(uidx);
        
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(-1.0);
        double uNeigh = this.getGraph().getNeighbourhoodSize(this.uidx2user(uidx), uSel);
        
        this.getAllUsers().forEach(v -> scoresMap.put(this.item2iidx(v), (linkprediction ? uNeigh*this.getGraph().getNeighbourhoodSize(v, vSel)+0.0 : this.getGraph().getNeighbourhoodSize(v, vSel)+0.0)));
        return scoresMap;
    }

    
    
}
