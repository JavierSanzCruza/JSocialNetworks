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
public class AdamicDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Map containing the length of the common neighborhoods between target and candidate users.
     */
    private final Int2DoubleMap wSizes;
    /**
     * Neighborhood selection for the intermediate users
     */
    private final EdgeOrientation wSel;
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel the neighborhood selection for the target user.
     * @param vSel the neighborhood selection for the candidate user.
     * @param wSel the neighborhood selection for the users in the intersection
     */
    public AdamicDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation wSel) 
    {
        super(graph, uSel, vSel);
        wSizes = new Int2DoubleOpenHashMap();
        this.wSel = wSel;
        this.getAllUidx().forEach(widx -> wSizes.put(widx, graph.getNeighborhood(widx, wSel).count()+0.0));
    }
    
    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        return 1.0/Math.log(wSizes.get(widx) + 2.0);
    }

    @Override
    protected double normalization(int uidx, int vidx, double score)
    {
        return score*Math.log(2.0);
    }


}
