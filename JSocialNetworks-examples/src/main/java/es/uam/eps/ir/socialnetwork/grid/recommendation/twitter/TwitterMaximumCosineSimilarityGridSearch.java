/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.twitter;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.TWITTERMAXCOS;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.twitter.TwitterMaximumCosineSimilarityRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for Twitter version of the Maximum Cosine Similarity algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class TwitterMaximumCosineSimilarityGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the teleport rate for computing the circle of trust
     */
    private final static String R = "r";
    /**
     * Identifier for the number of users in the circle of trust
     */
    private final static String NEIGH = "neigh";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();

        List<Double> rs = grid.getDoubleValues(R);
        List<Integer> neighs = grid.getIntegerValues(NEIGH);
        
        rs.stream().forEach(r -> 
        {
            neighs.stream().forEach(neigh -> 
            {
                recs.put(TWITTERMAXCOS + "_" + neigh + "_" + r, (graph, prefData) -> 
                {
                   return new TwitterMaximumCosineSimilarityRecommender(graph, neigh, r);
                });
            });
        });
        
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();

        List<Double> rs = grid.getDoubleValues(R);
        List<Integer> neighs = grid.getIntegerValues(NEIGH);
        
        rs.stream().forEach(r -> 
        {
            neighs.stream().forEach(neigh -> {
                recs.put(TWITTERMAXCOS + "_" + neigh + "_" + r, () -> 
                {
                   return new TwitterMaximumCosineSimilarityRecommender(graph, neigh, r);
                });
            });
        });
        
        return recs;
    }
    
}
