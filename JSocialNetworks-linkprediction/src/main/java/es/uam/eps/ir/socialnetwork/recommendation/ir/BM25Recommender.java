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
 * Adaptation of the BM-25 Information Retrieval Algorithm for user recommendation
 * 
 * Sparck Jones, K., Walker, S., Roberton S.E. A Probabilistic Model of Information Retrieval: Development and Comparative Experiments. 
 * Information Processing and Management 36. February 2000, pp. 779-808 (part 1), pp. 809-840 (part 2).
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class BM25Recommender<U> extends UserFastRankingRecommender<U> 
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
     * Neighbour selection for the target user
     */
    private final EdgeOrientation uSel;
    /**
     * Neighbour selection for the candidate users.
     */
    private final EdgeOrientation vSel;
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
    private final Map<U, Double> rsj;
    /**
     * Neighborhood sizes for each user.
     */
    private final Map<U, Double> size;

    /**
     * Constructor
     * @param graph Graph
     * @param uSel Selection of the neighbours of the target user
     * @param vSel Selection of the neighbours of the candidate user
     * @param dlSel Selection of the neighbours for the document length
     * @param b Tunes the effect of the neighborhood size. Between 0 and 1
     * @param k parameter of the algorithm
     */
    public BM25Recommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation dlSel, double b, double k) 
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
        this.dlSel = dlSel;
        this.b = b;
        this.k = k;
        this.rsj = new HashMap<>();
        this.size = new HashMap<>();
        this.numUsers  = graph.getVertexCount();
        this.avgSize = this.getGraph().getAllNodes().mapToDouble(v -> 
        {
            double val = this.getGraph().getNeighbourhood(v, this.dlSel).mapToDouble(w -> 
            {
                double aux = this.getFreq(v, w, this.dlSel);
                return aux;
            }).sum();
            this.size.put(v, val);
            return val;
        }).average().getAsDouble();
        
        this.getGraph().getAllNodes().forEach(w -> 
        {
            double val = this.getGraph().getNeighbourhood(w, this.vSel.invertSelection()).count();
            this.rsj.put(w, Math.log((numUsers - val + 0.5)/(val + 0.5)));
        });
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap map = new Int2DoubleOpenHashMap();
        map.defaultReturnValue(Double.NEGATIVE_INFINITY);
        U u = uIndex.uidx2user(i);
        
        Set<U> uUsers = this.getGraph().getNeighbourhood(u, uSel).collect(Collectors.toCollection(HashSet::new));
        iIndex.getAllItems().forEach(v -> 
        {
            double value = this.getGraph().getNeighbourhood(v, vSel).mapToDouble(w -> 
            {
                if(uUsers.contains(w))
                {
                    double s = size.getOrDefault(v, 0.0);

                    // Compute freq(u,v)
                    double weight = this.getFreq(v, w, vSel);

                    double num;
                    double den;
                    if(Double.isFinite(k)) // Usual BM 25 version
                    {
                        num = (k + 1.0) * weight * rsj.get(w);
                        den = k * (1 - b + (b * s / (double) avgSize)) + weight;
                    }
                    else // Extreme BM 25 version
                    {
                        num = weight * rsj.get(w);
                        den = (1 - b + (b*s / (double) avgSize));
                    }

                    if(num == 0.0 || den == 0.0)
                        return 0.0;
                    return num/den;
                }
                
                return 0.0;
            }).sum();
            
            map.put(iIndex.item2iidx(v), value);
        });
        
        return map;
    }
}
