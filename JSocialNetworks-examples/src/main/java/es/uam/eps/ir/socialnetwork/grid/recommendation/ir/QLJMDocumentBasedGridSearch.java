/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.ir;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.QLJMDOC;
import es.uam.eps.ir.socialnetwork.recommendation.ir.QLJMDocumentBasedRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for Query Likelihood algorithm with Jelinek-Mercer normalization (Document-based version).
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class QLJMDocumentBasedGridSearch<U> implements AlgorithmGridSearch<U> 
{
    /**
     * Identifier for the trade-off between the regularization term and the original term in
     * the Query Likelihood Jelinek-Mercer formula.
     */
    private static final String LAMBDA  = "lambda";
    /**
     * Identifier for the orientation of the target user neighborhood
     */
    private static final String USEL = "uSel";
    /**
     * Identifier for the orientation of the target user neighborhood
     */
    private static final String VSEL = "vSel";

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U,U> prefData) 
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        lambdas.stream().forEach(lambda -> 
        {
            uSels.stream().forEach(uSel -> 
            {
                vSels.stream().forEach(vSel -> 
                {
                    recs.put(QLJMDOC + "_" + uSel + "_" + vSel + "_" + lambda, () -> 
                    {
                       return new QLJMDocumentBasedRecommender<>(graph, uSel, vSel, lambda);
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
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        
        lambdas.stream().forEach(lambda -> 
        {
            uSels.stream().forEach(uSel -> 
            {
                vSels.stream().forEach(vSel -> 
                {
                    recs.put(QLJMDOC + "_" + uSel + "_" + vSel + "_" + lambda, (graph, prefData) -> 
                    {
                       return new QLJMDocumentBasedRecommender<>(graph, uSel, vSel, lambda);
                    });
                });
            });
        });
        return recs;    
    }
    
}
