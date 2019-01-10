/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Class for performing the grid search for a given algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public interface AlgorithmGridSearch<U>
{
    /**
     * Obtains the different recommendation algorithms to execute in a grid.
     * @param grid The grid for the algorithm.
     * @return a map containing the different recommendations.
     */
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U,U>, Recommender<U,U>>> grid(Grid grid);
    
    /**
     * Obtains the different recommendation algorithms to execute in a grid.
     * @param grid The grid for the algorithm.
     * @param graph The training graph.
     * @param prefData The preference training data.
     * @return a map containing the different recommendations.
     */
    public Map<String, Supplier<Recommender<U,U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U,U> prefData);
}
