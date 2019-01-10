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
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Recommender that uses the Jaccard coefficient of the neighbours.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 */
public class DistanceTwoPopularityDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Map containing the popularity of the candidate users.
     */
    private final Int2DoubleMap vSizes;
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel the neighborhood selection for the target user.
     * @param vSel the neighborhood selection for the candidate user.
     */
    public DistanceTwoPopularityDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel);
        vSizes = new Int2DoubleOpenHashMap();
        
        this.getAllUidx().forEach(vidx -> vSizes.put(vidx, graph.getNeighborhood(vidx, EdgeOrientation.IN).count() + 0.0));
    }
    
    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        return 1.0;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score)
    {
        return vSizes.get(vidx);
    }

}
