/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.mf;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.HKV;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;
import org.ranksys.recommenders.mf.Factorization;
import org.ranksys.recommenders.mf.Factorizer;
import org.ranksys.recommenders.mf.als.HKVFactorizer;
import org.ranksys.recommenders.mf.rec.MFRecommender;

/**
 * Grid search generator for the Implicit Matrix Factorization algorithm by 
 * Hu, Koren and Volinsky (HKV) algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class HKVGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier fort the parameter that regulates the importance of the error and the norm of the latent vectors.
     */
    private final static String LAMBDA = "lambda";
    /**
     * Identifier for the rate of increase for the confidence
     */
    private final static String ALPHA = "alpha";
    /**
     * Identifier for indicating if teleport always goes to the origin node.
     */
    private final static String K = "k";
    /**
     * Number of iterations for the algorithm
     */
    private final static int NUMITER = 20;

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> ks = grid.getIntegerValues(K);
        
        alphas.stream().forEach(alpha -> 
        {
            DoubleUnaryOperator confidence = (double x) -> 1 + alpha*x;
            ks.stream().forEach(k ->
            {
                lambdas.stream().forEach(lambda -> 
                {
                    recs.put(HKV + "_" + k + "_" + lambda + "_" + alpha, (graph, prefData) -> 
                    {
                       Factorizer<U, U> factorizer = new HKVFactorizer<>(lambda, confidence, NUMITER);
                       Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                       return new MFRecommender<>(prefData, prefData, factorization);
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
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> ks = grid.getIntegerValues(K);
        
        alphas.stream().forEach(alpha -> 
        {
            DoubleUnaryOperator confidence = (double x) -> 1 + alpha*x;
            ks.stream().forEach(k ->
            {
                lambdas.stream().forEach(lambda -> 
                {
                    recs.put(HKV + "_" + k + "_" + lambda + "_" + alpha, () -> 
                    {
                       Factorizer<U, U> factorizer = new HKVFactorizer<>(lambda, confidence, NUMITER);
                       Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                       return new MFRecommender<>(prefData, prefData, factorization);
                    });
                });
            });
        });
        return recs;
    }
    
}
