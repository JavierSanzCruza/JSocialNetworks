/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.cf;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.UB;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;
import org.ranksys.recommenders.nn.user.UserNeighborhoodRecommender;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhood;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhoods;
import org.ranksys.recommenders.nn.user.sim.UserSimilarities;
import org.ranksys.recommenders.nn.user.sim.UserSimilarity;

/**
 * Grid search generator for User Based CF algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class UserBasedCFGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the number of neighbors of the algorithm
     */
    private final static String K = "k";
    /**
     * Identifier for the asymmetry of the similarity
     */
    private final static String ALPHA = "alpha";
    /**
     * Identifier for the exponent of the similarity in the final sum
     */
    private final static String Q = "q";

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U,U> prefData) 
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Integer> ks = grid.getIntegerValues(K);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> qs = grid.getIntegerValues(Q);
        
        alphas.stream().forEach(alpha -> 
        {
            UserSimilarity<U> sim = UserSimilarities.vectorCosine(prefData, true);
            ks.stream().forEach(k -> 
            {
                UserNeighborhood<U> neighborhood = UserNeighborhoods.cached(UserNeighborhoods.topK(sim,k));
                qs.stream().forEach(q -> 
                {
                    recs.put(UB + "_" + k + "_" + alpha + "_" + q, () -> 
                    {
                        return new UserNeighborhoodRecommender<>(prefData, neighborhood, q);
                    });
                });
            });
        });
        
        return recs;
    }

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid) 
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Integer> ks = grid.getIntegerValues(K);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> qs = grid.getIntegerValues(Q);
        
        alphas.stream().forEach(alpha -> 
        {
            
            ks.stream().forEach(k -> 
            {
                
                qs.stream().forEach(q -> 
                {
                    recs.put(UB + "_" + k + "_" + alpha + "_" + q, (graph, prefData) -> 
                    {
                        UserSimilarity<U> sim =  UserSimilarities.vectorCosine(prefData, true);
                        UserNeighborhood<U> neighborhood = UserNeighborhoods.cached(UserNeighborhoods.topK(sim,k));
                        return new UserNeighborhoodRecommender<>(prefData, neighborhood, q);
                    });
                });
            });
        });
        
        return recs;
    }
}
