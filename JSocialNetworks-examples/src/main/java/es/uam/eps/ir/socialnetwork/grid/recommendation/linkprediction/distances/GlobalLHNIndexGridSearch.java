/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.linkprediction.distances;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSearch;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmIdentifiers.GLOBALLHN;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.distance.GlobalLHNIndexRecommender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.Recommender;

/**
 * Grid search generator for the global Leicht-Holme-Newman index.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class GlobalLHNIndexGridSearch<U> implements AlgorithmGridSearch<U> 
{
    /**
     * Identifier for the orientation of the target user neighborhood
     */
    private static final String USEL = "uSel";
    /**
     * Identifier for the orientation of the candidate user neighborhood
     */
    private static final String VSEL = "vSel";
    /**
     * Identifier for the beta parameter.
     */
    private static final String PHI = "phi";

    @Override
    public Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> grid(Grid grid)
    {
        Map<String, BiFunction<FastGraph<U>, FastPreferenceData<U, U>, Recommender<U, U>>> recs = new HashMap<>();

        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        List<Double> phis = grid.getDoubleValues(PHI);
        
        phis.stream().forEach(phi -> 
        {
            uSels.stream().forEach(uSel -> 
            {
                vSels.stream().forEach(vSel -> 
                {
                    recs.put(GLOBALLHN + "_" + uSel + "_" + vSel + "_" + phi, (graph, prefData) -> 
                    {
                        try 
                        {
                            return new GlobalLHNIndexRecommender<>(graph, phi, uSel, vSel);
                        } catch (Exception ex) {
                            return null;
                        }
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
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        List<Double> phis = grid.getDoubleValues(PHI);
        
        phis.stream().forEach(phi -> 
        {
            uSels.stream().forEach(uSel -> 
            {
                vSels.stream().forEach(vSel -> 
                {
                    recs.put(GLOBALLHN + "_" + uSel + "_" + vSel + "_" + phi, () -> 
                    {
                        try 
                        {
                            return new GlobalLHNIndexRecommender<>(graph, phi, uSel, vSel);
                        } catch (Exception ex) {
                            return null;
                        }
                    });
                });
            });
        });

        return recs;    
    }
    
}
