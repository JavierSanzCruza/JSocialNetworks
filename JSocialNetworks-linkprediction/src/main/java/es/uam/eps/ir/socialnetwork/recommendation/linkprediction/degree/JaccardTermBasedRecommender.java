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
 * Recommender that uses the Jaccard coefficient of the neighbours.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 */
public class JaccardTermBasedRecommender<U> extends UserFastRankingRecommender<U>
{
    /**
     * Map containing the length of the neighborhoods of the target users.
     */
    private final Int2DoubleMap uSizes;
    /**
     * Map containing the length of the neighborhoods of the candidate users.
     */
    private final Int2DoubleMap vSizes;
    
    
    private final EdgeOrientation uSel;
    private final EdgeOrientation vSel;
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel the neighborhood selection for the target user.
     * @param vSel the neighborhood selection for the candidate user.
     */
    public JaccardTermBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph);
        uSizes = new Int2DoubleOpenHashMap();
        this.uSel = uSel;
        this.vSel = vSel.invertSelection();
        
        if(uSel.equals(vSel) || !graph.isDirected())
        {
            this.getAllUidx().forEach(uidx -> 
            {
                uSizes.put(uidx, graph.getNeighborhood(uidx, uSel).count()+0.0);
            });
            vSizes = uSizes;
        }
        else
        {
            vSizes = new Int2DoubleOpenHashMap();
            this.getAllUidx().forEach(uidx -> 
            {
               uSizes.put(uidx, graph.getNeighborhood(uidx, uSel).count()+0.0);
               vSizes.put(uidx, graph.getNeighborhood(uidx, vSel).count()+0.0);
            });
        }
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);

        double uSize = this.uSizes.get(uidx);
        graph.getNeighborhood(uidx, uSel).forEach(widx -> 
        {
            graph.getNeighborhood(widx, vSel).forEach(vidx -> 
            {
                scoresMap.addTo(vidx, 1.0);
            });
        });
       
        scoresMap.replaceAll((vidx, sim) -> sim/(uSize + this.vSizes.get((int)vidx) - sim));
        return scoresMap;
    }
}
