/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.CommonNeighborsDocBasedRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Adaptation of the Query Likelihood Information Retrieval method, with
 * the regularization of Jelinek Mercer
 * 
 * Ponte, J. M. Croft, W. B. A language modeling approach to information retrieval. 
 * 21st Annual International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR 1998). 
 * Melbourne, Australia, August 1998, pp. 275-281.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class QLJMDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Parameter which controls the trade-off between the regularization term and the original term
     * in the formula.
     */
    private final double lambda;
    /**
     * For each user, computes the proportion of neighbors it has, in comparison with the sum of all neighborhood sizes.
     */
    private final Int2DoubleMap pc;
    /**
     * Neighborhood sizes
     */
    private final Int2DoubleMap size;
    /**
     * Sum of the neighborhood sizes
     */
    private final double fullSize;
    
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     * @param lambda Parameter which controls the trade-off between the regularization term and the original probability.
     */
    public QLJMDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, double lambda) 
    {
        super(graph, uSel, vSel);
        this.lambda = lambda/(1-lambda);
        this.size = new Int2DoubleOpenHashMap();
        
        EdgeOrientation wSel = vSel.invertSelection();
        if(!graph.isDirected() || vSel.equals(EdgeOrientation.UND)) // vSel == wSel
        {
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double vS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                this.size.put(vidx, vS);
                return vS;
            }).sum();
            this.pc = size;
        }
        else
        {
            this.pc = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double vS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                double wS = graph.getNeighborhoodWeights(vidx, wSel).mapToDouble(w -> w.v2()).sum();
                
                this.size.put(vidx, vS);
                this.pc.put(vidx, wS);
                return vS;
            }).sum();
        }     
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        double s = this.size.getOrDefault(vidx, 0.0);
        double wPc = this.fullSize/pc.get(widx);
        
        double val = this.lambda*wPc*vW/s;
        
        if(Double.isInfinite(val) || Double.isNaN(val))
        {
            return Double.NEGATIVE_INFINITY;
        }
        
        val += 1.0;
        
        return uW*Math.log(val);
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        return score;
    }
}
