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
import java.util.HashMap;
import java.util.Map;

/**
 * Recommender based on Sorensen similarity.
 * 
 * Lü, L., Zhou. T. Link Prediction in Complex Networks: A survey. Physica A: Statistical Mechanics and its Applications, 390(6), March 2011, pp. 1150-1170.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class SorensenDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
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
    public SorensenDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel);
        uSizes = new HashMap<>();
        vSizes = new HashMap<>();
        
        graph.getAllNodes().forEach(u -> uSizes.put(graph.object2idx(u), graph.getNeighbourhoodSize(u, uSel) + 0.0));
        if(uSel.equals(vSel) || !graph.isDirected())
        {
            vSizes.putAll(uSizes);
        }
        else
        {
            graph.getAllNodes().forEach(v -> vSizes.put(graph.object2idx(v), graph.getNeighbourhoodSize(v, vSel)+0.0));           
        }
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        return 2.0;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        double den = uSizes.get(uidx) + vSizes.get(vidx);
        if(den != 0.0)
            return score/den;
        return 0.0;
    }

    
}
