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
import java.util.stream.Stream;

/**
 * Adaptation of the BM-25 Information Retrieval Algorithm for user recommendation. Uses a term-based implementation.
 * 
 * Sparck Jones, K., Walker, S., Roberton S.E. A Probabilistic Model of Information Retrieval: Development and Comparative Experiments. 
 * Information Processing and Management 36. February 2000, pp. 779-808 (part 1), pp. 809-840 (part 2).
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class BM25DocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{
    /**
     * Parameter that tunes the effect of the neighborhood size. Between 0 and 1
     */
    private final double b;
    /**
     * Parameter that tunes the effect of the term frequency on the formula.
     */
    private final double k;
    /**
     * Neighbour selection for the document length
     */
    private final EdgeOrientation dlSel;
    /**
     * Average size of the neighborhood of the candidate nodes.
     */
    private final double avgSize;
    /**
     * Number of users in the network.
     */
    private final long numUsers;
    /**
     * Robertson-Sparck-Jones formula values for each user.
     */
    private final Int2DoubleMap rsj;
    /**
     * Neighborhood sizes for each user.
     */
    private final Int2DoubleMap size;
    
    /**
     * Constructor.
     * @param graph Graph
     * @param uSel Selection of the neighbours of the target user
     * @param vSel Selection of the neighbours of the candidate user
     * @param dlSel Selection of the neighbours for the document length
     * @param b Tunes the effect of the neighborhood size. Between 0 and 1.
     * @param k parameter of the algorithm.
     */
    public BM25DocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation dlSel, double b, double k) 
    {
        super(graph, uSel, vSel);
        
        this.dlSel = dlSel;
        this.b = b;
        this.k = k;
        this.rsj = new Int2DoubleOpenHashMap();
        this.size = new Int2DoubleOpenHashMap();
        this.numUsers = graph.getVertexCount();
        

        this.avgSize = this.getAllUidx().mapToDouble(vidx -> 
        {
            // Compute RSJ
            double rsjV = graph.getNeighborhood(vidx, vSel.invertSelection()).count();
            rsjV = Math.log((numUsers - rsjV + 0.5)/(rsjV + 0.5));
            this.rsj.put(vidx, rsjV);
            
            // Compute size
            double val = graph.getNeighborhoodWeights(vidx, dlSel).mapToDouble(widx -> widx.v2).sum();
            this.size.put(vidx, val);
            return val;
        }).average().getAsDouble();
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        double s = this.size.getOrDefault(vidx, 0.0);
        
        
        double num;
        double den;
        if(Double.isFinite(this.k)) // Usual BM-25 version
        {
            num = (k + 1.0)*vW*rsj.get(widx);
            den = k*(1-b + (b*s / avgSize)) + vW;
        }
        else // Extreme BM-25 version
        {
            num = vW * rsj.get(widx);
            den = (1-b + (b*s / avgSize));
        }
        if(num == 0 || den == 0)
            return 0.0;
        
        return num/den;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        return score;
    }

    
}
