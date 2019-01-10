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
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.SOCIALCOLLAB;
import es.uam.eps.ir.socialnetwork.recommendation.cf.SocialCollabRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;
import org.ranksys.recommenders.nn.item.neighborhood.ItemNeighborhood;
import org.ranksys.recommenders.nn.item.neighborhood.ItemNeighborhoods;
import org.ranksys.recommenders.nn.item.sim.ItemSimilarities;
import org.ranksys.recommenders.nn.item.sim.ItemSimilarity;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhood;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhoods;
import org.ranksys.recommenders.nn.user.sim.UserSimilarities;
import org.ranksys.recommenders.nn.user.sim.UserSimilarity;

/**
 * Grid search for the SocialCollab recommendation algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class SocialCollabGridSearch<U> implements AlgorithmGridSearch<U> 
{
    /**
     * Identifier for the number of similar users to the candidate user according to taste.
     */
    private final static String TASTEK = "tasteK";
    /**
     * Identifier for the number of similar users to the candidate user according to attractiveness.
     */
    private final static String ATTRACTK = "attractivenessK";
    /**
     * Identifier for the exponent of cosine similarity
     */
    private final static String ALPHA = "alpha";

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U,U> prefData) 
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Integer> tasteks = grid.getIntegerValues(TASTEK);
        List<Integer> attractks = grid.getIntegerValues(ATTRACTK);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        
        alphas.stream().forEach(alpha -> 
        {
            UserSimilarity<U> uSim = UserSimilarities.vectorCosine(prefData, true);
            ItemSimilarity<U> iSim = ItemSimilarities.vectorCosine(prefData, true);
            tasteks.stream().forEach(taste -> 
            {
                UserNeighborhood<U> uNeighborhood = UserNeighborhoods.cached(UserNeighborhoods.topK(uSim, taste));
                attractks.stream().forEach(attract -> 
                {
                    ItemNeighborhood<U> iNeighborhood = ItemNeighborhoods.cached(ItemNeighborhoods.topK(iSim, attract));
                    
                    recs.put(SOCIALCOLLAB + "_" + taste + "_" + attract + "_" + alpha, () -> 
                    {
                       return new SocialCollabRecommender<>(graph, uNeighborhood, iNeighborhood);
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
        
        List<Integer> tasteks = grid.getIntegerValues(TASTEK);
        List<Integer> attractks = grid.getIntegerValues(ATTRACTK);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        
        alphas.stream().forEach(alpha -> 
        {
            tasteks.stream().forEach(taste -> 
            {
                attractks.stream().forEach(attract -> 
                {
                    recs.put(SOCIALCOLLAB + "_" + taste + "_" + attract + "_" + alpha, (graph, prefData) -> 
                    {
                        UserSimilarity<U> uSim = UserSimilarities.vectorCosine(prefData, true);
                        ItemSimilarity<U> iSim = ItemSimilarities.vectorCosine(prefData, true);
                        UserNeighborhood<U> uNeighborhood = UserNeighborhoods.cached(UserNeighborhoods.topK(uSim, taste));
                        ItemNeighborhood<U> iNeighborhood = ItemNeighborhoods.cached(ItemNeighborhoods.topK(iSim, attract));

                        return new SocialCollabRecommender<>(graph, uNeighborhood, iNeighborhood);
                    });
                });
            });
        });
        
        return recs;
    }
    
        


    
}
