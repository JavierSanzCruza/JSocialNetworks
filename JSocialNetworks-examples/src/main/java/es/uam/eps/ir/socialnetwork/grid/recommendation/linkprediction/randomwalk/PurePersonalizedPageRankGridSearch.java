/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.randomwalk;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.PUREPERSPAGERANK;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk.PurePersonalizedPageRankRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for Pure Personalized PageRank algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class PurePersonalizedPageRankGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the teleport parameter
     */
    private final static String LAMBDA = "lambda";
    /**
     * Identifier for indicating if the origin node can be only accessed via teleport 
     */
    private final static String SIMPLE = "simple";
    /**
     * Identifier for indicating if teleport always goes to the origin node.
     */
    private final static String S2U = "S2U";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<Boolean> simples = grid.getBooleanValues(SIMPLE);
        List<Boolean> s2us = grid.getBooleanValues(S2U);
        lambdas.stream().forEach(lambda -> 
        {
            simples.stream().forEach(simple ->
            {
                s2us.stream().forEach(s2u -> 
                {
                    recs.put(PUREPERSPAGERANK + "_" + lambda + "_" + (simple ? "simple" : "general") + "_" + (s2u ? "S2U" : "S2O"), (graph, prefData) -> 
                    {
                       return new PurePersonalizedPageRankRecommender<>(graph, lambda, simple, s2u);
                    });
                });
            });
        });
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<Boolean> simples = grid.getBooleanValues(SIMPLE);
        List<Boolean> s2us = grid.getBooleanValues(S2U);
        lambdas.stream().forEach(lambda -> 
        {
            simples.stream().forEach(simple ->
            {
                s2us.stream().forEach(s2u -> 
                {
                    recs.put(PUREPERSPAGERANK + "_" + lambda + "_" + (simple ? "simple" : "general") + "_" + (s2u ? "S2U" : "S2O"), () -> 
                    {
                       return new PurePersonalizedPageRankRecommender<>(graph, lambda, simple, s2u);
                    });
                });
            });
        });
        return recs;
    }
    
}
