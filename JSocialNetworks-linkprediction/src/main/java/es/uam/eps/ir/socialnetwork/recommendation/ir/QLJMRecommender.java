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
 * the regularization of Jelinek-Mercer
 * 
 * Ponte, J. M. Croft, W. B. A language modeling approach to information retrieval. 
 * 21st Annual International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR 1998). 
 * Melbourne, Australia, August 1998, pp. 275-281.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class QLJMRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Parameter which controls the trade-off between the regularization term and the original term
     * in the formula.
     */
    private final double lambda;
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
    public QLJMRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, double lambda) 
    {
        super(graph);
        this.lambda = lambda;
        this.uSel = uSel;
        this.vSel = vSel;
        this.pc = new HashMap<>();
        this.size = new HashMap<>();
        
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
            this.getGraph().getAllNodes().forEach(v -> 
            {
                double s = this.size.get(v);
                if((Long) u == 0L && (Long) v == 9L)
                {
                    System.err.println("TEST");
                }
               
                
                double sum = this.getGraph().getNeighbourhood(v, vSel).mapToDouble(w ->
                {
                    if(uUsers.contains(w))
                    {
                        double val = (lambda/(1-lambda))*(this.getFreq(v,w,vSel)/s)/(pc.get(w));
                        
                        if(Double.isNaN(val) || Double.isInfinite(val)) return Double.NEGATIVE_INFINITY;
                        return this.getFreq(u,w,uSel)*Math.log(1.0 + val);
                    }
                    
                    return 0.0;
                }).sum();
                
                if(sum > 0.0)
                    scores.put(iIndex.item2iidx(v), sum);
                else
                    scores.put(iIndex.item2iidx(v), Double.NEGATIVE_INFINITY);
            });
        }
        return scores;
    }

    
    
}
