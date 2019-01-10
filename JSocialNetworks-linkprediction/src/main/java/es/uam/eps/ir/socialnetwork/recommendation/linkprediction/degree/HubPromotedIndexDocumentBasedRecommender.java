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
import es.uam.eps.ir.socialnetwork.recommendation.CommonNeighborsDocBasedRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Map;

/**
 * Recommender that uses the Jaccard coefficient of the neighbours.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 */
public class HubPromotedIndexDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Map containing the length of the neighborhoods of the target users.
     */
    private final Map<Integer, Double> uSizes;
    /**
     * Map containing the length of the neighborhoods of the candidate users.
     */
    private final Map<Integer, Double> vSizes;
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel the neighborhood selection for the target user.
     * @param vSel the neighborhood selection for the candidate user.
     */
    public HubPromotedIndexDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel);
        uSizes = new Int2DoubleOpenHashMap();
        
        if(uSel.equals(vSel) || !graph.isDirected())
        {
            graph.getAllNodesIds().forEach(uidx -> 
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
               uSizes.put(uidx, graph.getNeighborhood(uidx, vSel).count()+0.0);
            });
        }
    }
    
    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        return 1.0;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score)
    {
        double den = Math.min(uSizes.get(uidx),vSizes.get(vidx));
        if(den == 0.0) return 0.0;
        return score/den;
    }

}
