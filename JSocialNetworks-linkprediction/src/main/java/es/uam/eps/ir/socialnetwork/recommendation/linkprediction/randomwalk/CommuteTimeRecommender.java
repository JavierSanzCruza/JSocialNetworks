/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import no.uib.cipr.matrix.Matrix;

/**
 * Recommends users based on the hitting time.
 * 
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks, CIKM, 2003
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class CommuteTimeRecommender<U> extends HittingTimeRecommender<U>
{
    /**
     * Constructor.
     * @param graph The graph.
     */
    public CommuteTimeRecommender(FastGraph<U> graph) {
        super(graph);
    }
     
    /**
     * Constructor, which uses the hitting time matrix.

     * @param graph the graph.
     * @param hittingTime a matrix containing the hitting times between each pair of users.
     */
    public CommuteTimeRecommender(FastGraph<U> graph, Matrix hittingTime)
    {
        super(graph, hittingTime);
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        this.uIndex.getAllUidx().forEach(uidx -> {
            scores.put(uidx, -this.hittingTime.get(i,uidx) - this.hittingTime.get(uidx,i));
        });
                
        return scores;
    }

}
