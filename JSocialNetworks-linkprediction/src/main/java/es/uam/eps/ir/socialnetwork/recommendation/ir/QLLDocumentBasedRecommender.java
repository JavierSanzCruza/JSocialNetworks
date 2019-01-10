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
 * the regularization of Laplace
 * 
 * Ponte, J. M. Croft, W. B. A language modeling approach to information retrieval. 
 * 21st Annual International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR 1998). 
 * Melbourne, Australia, August 1998, pp. 275-281.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class QLLDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Parameter which controls the trade-off between the regularization term and the original term
     * in the formula.
     */
    private final double gamma;
    /**
     * Target users neighborhood sizes
     */
    private final Int2DoubleMap uSize;
    /**
     * Candidate users neighborhood sizes
     */
    private final Int2DoubleMap vSize;
    private final double fullSize;
    
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     * @param gamma Regularization parameter
     */
    public QLLDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, double gamma) 
    {
        super(graph, uSel, vSel);
           this.gamma = gamma;
        
        this.uSize = new Int2DoubleOpenHashMap();
        
        if(!graph.isDirected() || uSel.equals(vSel))
        {
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double s = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                this.uSize.put(vidx, s);
                return s;
            }).sum();
            vSize = uSize;
        }
        else
        {
            this.vSize = new Int2DoubleOpenHashMap();
            this.fullSize = this.getAllUidx().mapToDouble(vidx -> 
            {
                double uS = graph.getNeighborhoodWeights(vidx, uSel).mapToDouble(w -> w.v2()).sum();
                double vS = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(w -> w.v2()).sum();
                
                this.uSize.put(vidx, uS);
                this.vSize.put(vidx, vS);
                return vS;
            }).sum();
        }
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {        
        return uW*Math.log((vW+ this.gamma)/gamma);
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        return score + this.uSize.get(uidx)*Math.log(gamma/(vSize.get(vidx) + gamma*this.numUsers()));
    }
}
