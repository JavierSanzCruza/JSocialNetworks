/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.randomwalk;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.PROPFLOW;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk.PropFlowRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for PropFlow algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class PropFlowGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the teleport parameter
     */
    private final static String MAXLENGTH = "maxLength";
    /**
     * Identifier for the edge orientation
     */
    private final static String ORIENTATION = "orientation";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();
        
        List<Integer> maxLengths = grid.getIntegerValues(MAXLENGTH);
        List<EdgeOrientation> orientations = grid.getOrientationValues(ORIENTATION);
        maxLengths.stream().forEach(maxLength -> 
        {
            orientations.stream().forEach(orientation -> 
            {
                recs.put(PROPFLOW + "_" + orientation + "_" + maxLength, (graph, prefData) -> 
                {
                   return new PropFlowRecommender<>(graph, maxLength, orientation);
                });
            });
        });
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Integer> maxLengths = grid.getIntegerValues(MAXLENGTH);
        List<EdgeOrientation> orientations = grid.getOrientationValues(ORIENTATION);
        maxLengths.stream().forEach(maxLength -> 
        {
            orientations.stream().forEach(orientation -> 
            {
                recs.put(PROPFLOW + "_" + orientation + "_" + maxLength, () -> 
                {
                   return new PropFlowRecommender<>(graph, maxLength, orientation);
                });
            });
        });
        return recs;
    }
    
}
