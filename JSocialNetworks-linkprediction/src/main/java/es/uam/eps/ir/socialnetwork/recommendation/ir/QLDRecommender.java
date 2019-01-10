/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class QLDRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Parameter which controls the trade-off between the regularization term and the original term
     * in the formula.
     */
    private final double mu;
    /**
     * Neighborhood selection for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighborhood selection for the candidate user.
     */
    private final EdgeOrientation vSel;
    /**
     * For each user, computes the proportion of neighbors it has, in comparison with the sum of all neighborhood sizes.
     */
    private final Map<U, Double> pc;
    /**
     * Neighborhood sizes
     */
    private final Map<U, Double> size;
    /**
     * Sum of the neighborhood sizes
     */
    private final double fullSize;
    
    /**
     * Neighborhood sizes for the target users
     */
    private final Map<U,Double> uSize;
    /**
     * Flag for indicating if the score is correctly computed.
     */
    private boolean flag;
    
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     * @param lambda Parameter which controls the trade-off between the regularization term and the original probability.
     */
    public QLDRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, double lambda) 
    {
        super(graph);
        this.mu = lambda;
        this.uSel = uSel;
        this.vSel = vSel;
        this.pc = new HashMap<>();
        this.size = new HashMap<>();
        this.uSize = new HashMap<>();
        
        // Compute the sizes.
        this.fullSize = this.iIndex.getAllItems().mapToDouble(v-> 
        {
            double s = this.getGraph().getNeighbourhood(v, vSel).mapToDouble(w -> 
            {
                return this.getFreq(v, w, vSel);
            }).sum();
            
            this.size.put(v, s);
            return s;
            
        }).sum();
        
        // Compute the p_c probability.
        this.iIndex.getAllItems().forEach(w -> 
        {
            double uVal = this.getGraph().getNeighbourhood(w, uSel).mapToDouble(x ->
            {
                return this.getFreq(w,x,uSel);
            }).sum();
            
            this.uSize.put(w, uVal);
            double val = this.getGraph().getNeighbourhood(w, vSel.invertSelection()).mapToDouble(x -> 
            {
                return this.getFreq(x,w,vSel);
            }).sum();
           
            this.pc.put(w, val / fullSize);
        });
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        U u = uIndex.uidx2user(i);
        
        Set<U> uUsers = this.getGraph().getNeighbourhood(u, uSel).collect(Collectors.toCollection(HashSet::new));
        if(uUsers == null || uUsers.isEmpty())
        {
            iIndex.getAllIidx().forEach(iidx -> scores.put(iidx, Double.NEGATIVE_INFINITY));
        }
        else
        {
            double uS = this.uSize.get(u);
            this.getGraph().getAllNodes().forEach(v -> 
            {
                double vS = size.get(v);

                double sum = this.getGraph().getNeighbourhood(v, vSel).mapToDouble(w -> 
                {
                    double s = size.get(v);
                    double val = 0.0;
                    if(uUsers.contains(w))
                    {
                        double uW = this.getFreq(u, w, uSel);
                        double vW = this.getFreq(v, w, vSel);
                        
                        val = (vW/mu)*(1/pc.get(w))+1.0;
                        val = Math.log(val)*uW;
                    }
                    
                    if(Double.isInfinite(val) || Double.isNaN(val)) return Double.NEGATIVE_INFINITY;
                    return val;
                }).sum();
                
                if(sum != 0)
                    scores.put(iIndex.item2iidx(v), sum - uS*Math.log(1.0 + vS/mu));
                else
                    scores.put(iIndex.item2iidx(v), Double.NEGATIVE_INFINITY);
            });
        }
        return scores;
    }

    
    
}
