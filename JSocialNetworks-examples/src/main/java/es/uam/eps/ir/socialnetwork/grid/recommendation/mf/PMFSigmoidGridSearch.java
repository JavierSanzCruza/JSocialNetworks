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
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.PMFBASIC;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.recommendation.mf.PMFFactorizerSigmoid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;
import org.ranksys.recommenders.mf.Factorization;
import org.ranksys.recommenders.mf.Factorizer;
import org.ranksys.recommenders.mf.rec.MFRecommender;

/**
 * Grid search generator for the sigmoidal Probabilistic Matrix Factorization (PMF) algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class PMFSigmoidGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the regularization parameter for the user matrix
     */
    private final static String LAMBDAU = "lambdaU";
    /**
     * Identifier for the regularization parameter for the item matrix
     */
    private final static String LAMBDAV = "lambdaV";
    /**
     * Identifier for indicating if the origin node can be only accessed via teleport 
     */
    private final static String LEARNING = "learningRate";
    /**
     * Identifier for indicating if teleport always goes to the origin node.
     */
    private final static String K = "k";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Double> lambdaUs = grid.getDoubleValues(LAMBDAU);
        List<Double> lambdaVs = grid.getDoubleValues(LAMBDAV);
        List<Double> learningRates = grid.getDoubleValues(LEARNING);
        List<Integer> ks = grid.getIntegerValues(K);
        
        if(lambdaVs == null || lambdaVs.isEmpty())
        {
            ks.stream().forEach(k -> 
            {
                lambdaUs.stream().forEach(lambda ->
                {
                    learningRates.stream().forEach(learningRate -> 
                    {
                        recs.put(PMFBASIC + "_" + k + "_" + lambda + "_" + learningRate, (graph, prefData) -> 
                        {
                           Factorizer<U, U> factorizer = new PMFFactorizerSigmoid<>(lambda, learningRate);
                           Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                           return new MFRecommender<>(prefData, prefData, factorization);
                        });
                    });
                });
            });
        }
        else
        {
            ks.stream().forEach(k -> 
            {
                lambdaUs.stream().forEach(lambdaU ->
                {
                    lambdaVs.stream().forEach(lambdaV ->
                    {
                        learningRates.stream().forEach(learningRate -> 
                        {
                            recs.put(PMFBASIC + "_" + k + "_" + lambdaU + "_" + lambdaV + "_" + learningRate, (graph, prefData) -> 
                            {
                               Factorizer<U, U> factorizer = new PMFFactorizerSigmoid<>(lambdaU, lambdaV, learningRate);
                               Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                               return new MFRecommender<>(prefData, prefData, factorization);
                            });
                        });
                    });
                });
            });
        }
        
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Double> lambdaUs = grid.getDoubleValues(LAMBDAU);
        List<Double> lambdaVs = grid.getDoubleValues(LAMBDAV);
        List<Double> learningRates = grid.getDoubleValues(LEARNING);
        List<Integer> ks = grid.getIntegerValues(K);
        
        if(lambdaVs == null || lambdaVs.isEmpty())
        {
            ks.stream().forEach(k -> 
            {
                lambdaUs.stream().forEach(lambda ->
                {
                    learningRates.stream().forEach(learningRate -> 
                    {
                        recs.put(PMFBASIC + "_" + k + "_" + lambda + "_" + learningRate, () -> 
                        {
                           Factorizer<U, U> factorizer = new PMFFactorizerSigmoid<>(lambda, learningRate);
                           Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                           return new MFRecommender<>(prefData, prefData, factorization);
                        });
                    });
                });
            });
        }
        else
        {
            ks.stream().forEach(k -> 
            {
                lambdaUs.stream().forEach(lambdaU ->
                {
                    lambdaVs.stream().forEach(lambdaV ->
                    {
                        learningRates.stream().forEach(learningRate -> 
                        {
                            recs.put(PMFBASIC + "_" + k + "_" + lambdaU + "_" + lambdaV + "_" + learningRate, () -> 
                            {
                               Factorizer<U, U> factorizer = new PMFFactorizerSigmoid<>(lambdaU, lambdaV, learningRate);
                               Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                               return new MFRecommender<>(prefData, prefData, factorization);
                            });
                        });
                    });
                });
            });
        }
        
        return recs;
    }
    
}
