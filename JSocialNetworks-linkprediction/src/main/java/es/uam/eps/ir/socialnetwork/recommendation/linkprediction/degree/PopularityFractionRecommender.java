/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 *
 * @author Javier
 */
public class PopularityFractionRecommender<U> extends UserFastRankingRecommender<U> {

    private final Int2DoubleMap pop;
    public PopularityFractionRecommender(FastGraph<U> graph)
    {
        super(graph);
        
        this.pop = new Int2DoubleOpenHashMap();
        
        this.getAllUidx().forEach(uidx -> 
                {
                    double first = graph.getNeighborhood(uidx, EdgeOrientation.OUT).count()+1.0;
                    double second = graph.getNeighborhood(uidx, EdgeOrientation.IN).count();
                    pop.put(uidx, second/first);
                });
        
    }
    
    
    @Override
    public Int2DoubleMap getScoresMap(int i) {
        return pop;
    }

    
    
}
