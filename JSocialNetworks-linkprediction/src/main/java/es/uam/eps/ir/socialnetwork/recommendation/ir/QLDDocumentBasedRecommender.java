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
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptation of the Query Likelihood Information Retrieval method, with
 * the regularization of Dirichlet
 * 
 * Ponte, J. M. Croft, W. B. A language modeling approach to information retrieval. 
 * 21st Annual International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR 1998). 
 * Melbourne, Australia, August 1998, pp. 275-281.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class QLDDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Parameter which controls the trade-off between the regularization term and the original term
     * in the formula.
     */
    private final double mu;
    /**
     * For each user, computes the proportion of neighbors it has, in comparison with the sum of all neighborhood sizes.
     */
    private final Int2DoubleMap pc;
    /**
     * Neighborhood sizes
     */
    private final Int2DoubleMap uSize;
        /**
     * Neighborhood sizes
     */
    private final Int2DoubleMap vSize;
    /**
     * Sum of the neighborhood sizes
     */
    private final double fullSize;
    
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     * @param mu Parameter which controls the trade-off between the regularization term and the original probability.
     */
    public QLDDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, double mu)
    {
        super(graph, uSel, vSel);

        this.mu = mu;
        this.uSize = new Int2DoubleOpenHashMap();
        
        EdgeOrientation wSel = vSel.invertSelection();
        if(!graph.isDirected() || (uSel.equals(vSel) && uSel.equals(EdgeOrientation.UND))) // Cases UND-UND
        {
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                this.uSize.put(vidx, uS);
                return uS;
            }).sum();
            this.vSize = uSize;
            this.pc = uSize;
        }
        else if(uSel.equals(vSel)) //CASES IN-IN,OUT-OUT
        {
            this.pc = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                double wS = graph.getNeighborhoodWeights(vidx, wSel).mapToDouble(w -> w.v2()).sum();
                this.uSize.put(vidx, uS);
                this.pc.put(vidx, wS);
                return uS;
            }).sum();
            this.vSize = uSize;
        }
        else if(uSel.equals(vSel.invertSelection())) // CASES IN-OUT,OUT-IN
        {
            this.vSize = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                double wS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                this.uSize.put(vidx, uS);
                this.vSize.put(vidx, wS);
                return uS;
            }).sum();
            this.pc = uSize;
        }
        else if(vSel.equals(EdgeOrientation.UND)) // CASES IN-UND, OUT-UND
        {
            this.vSize = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                double vS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                this.uSize.put(vidx, uS);
                this.vSize.put(vidx, vS);
                return uS;
            }).sum();
            this.pc = vSize;
        }
        else // CASES UND-IN, UND-OUT
        {
            this.vSize = new Int2DoubleOpenHashMap();
            this.pc = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                double vS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                double wS = uS - vS; // Considering that weight(UND,x,y) = weight(x,y) + weight(y,x)
                this.uSize.put(vidx, uS);
                this.vSize.put(vidx, vS);
                this.pc.put(vidx, wS);
                return uS;
            }).sum();
        }
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        double value = vW/this.mu;
        value *= this.fullSize/this.pc.get(widx);
        value += 1.0;
        
        if(Double.isInfinite(value) || Double.isNaN(value))
        {
            return Double.NEGATIVE_INFINITY;
        }
        
        return uW*Math.log(value);
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        double norm = this.uSize.get(uidx);
        norm *= Math.log(1 + this.vSize.get(vidx)/this.mu);
        return score - norm;
    }
}
