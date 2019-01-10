/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.basic;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Random;

/**
 * Recommends contacts using the random algorithm.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class RandomRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Random number generator.
     */
    private final Random random;

    /**
     * Constructor.
     * @param graph Graph
     */
    public RandomRecommender(FastGraph<U> graph) 
    {
        this(graph, System.currentTimeMillis());        
    }
    
    /**
     * Constructor.
     * @param graph The graph representing the social networks.
     * @param seed The seed for the random number generator.
     */
    public RandomRecommender(FastGraph<U> graph, long seed)
    {
        super(graph);
        this.random = new Random(seed);
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap map = new Int2DoubleOpenHashMap();
        map.defaultReturnValue(Double.NEGATIVE_INFINITY);
        U u = uIndex.uidx2user(i);
        
        iIndex.getAllIidx().forEach(iidx -> map.put(iidx,random.nextDouble()));
                
        return map;
    }

}
