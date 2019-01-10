/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.distances;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.LPI;
import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.distance.LocalPathIndexRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for the Local Path Index algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class LocalPathIndexGridSearch<U> implements AlgorithmGridSearch<U> 
{
    /**
     * Identifier for the orientation of the paths from the origin to the source
     */
    private static final String USEL = "orientation";
    
    /**
     * Identifier for the beta parameter.
     */
    private static final String BETA = "beta";
    
    /**
     * Identifier for the maximum lenght of the paths
     */
    private static final String LENGTH = "maxLength";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();

        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<Double> betas = grid.getDoubleValues(BETA);
        List<Integer> lengths = grid.getIntegerValues(LENGTH);

        betas.stream().forEach(beta -> 
        {
            lengths.stream().forEach(length -> 
            {
                uSels.stream().forEach(orient -> 
                {
                    recs.put(LPI + "_" + orient + "_" + beta + "_" + length, (graph, prefData) -> 
                    {
                        return new LocalPathIndexRecommender<>(graph, beta, orient, length);
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

        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<Double> betas = grid.getDoubleValues(BETA);
        List<Integer> lengths = grid.getIntegerValues(LENGTH);

        betas.stream().forEach(beta -> 
        {
            lengths.stream().forEach(length -> 
            {
                uSels.stream().forEach(orient -> 
                {
                    recs.put(LPI + "_" + orient + "_" + beta + "_" + length, () -> 
                    {
                        return new LocalPathIndexRecommender<>(graph, beta, orient, length);
                    });
                });
            });
        });

        return recs;
    }
    
}
