/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.randomwalk;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.SALSA;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk.SALSARecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for SALSA algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class SALSAGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the mode of the algorithm
     */
    private final static String MODE = "mode";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Boolean> modes = grid.getBooleanValues(MODE);
        modes.stream().forEach(mode -> 
        {
            recs.put(SALSA + "_" + (mode ? "auth" : "hubs"), (graph, prefData) -> 
            {
               return new SALSARecommender<>(graph, mode);
            });
        });
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Boolean> modes = grid.getBooleanValues(MODE);
        modes.stream().forEach(mode -> 
        {
            recs.put(SALSA + "_" + (mode ? "auth" : "hubs"), () -> 
            {
               return new SALSARecommender<>(graph, mode);
            });
        });
        return recs;
    }
    
}
