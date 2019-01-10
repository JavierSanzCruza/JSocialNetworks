/* 
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import static java.util.Collections.sort;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import static java.util.stream.Collectors.toList;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 * Contact recommender based in the value of a certain user metric (Not personalized recommender).
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users.
 */
public class MetricRecommender<U> extends UserFastRankingRecommender<U>
{
    
    /**
     * The values of the metric for each user
     */
    private Map<U,Double> metric;
    
    /**
     * The ranking of the metric (from largest value to smallest).
     */
    private final List<Tuple2id> rankingList;
    
    /**
     * Constructor of the user metric based recommender.
     * @param graph Graph user graph.
     * @param metric the user metric
     */
    public MetricRecommender(FastGraph<U> graph, VertexMetric<U> metric) 
    {
        super(graph);
        
        this.metric = metric.compute(graph);

        rankingList = iIndex.getAllItems().map(user -> new Tuple2id(iIndex.item2iidx(user),this.metric.get(user))).collect(toList());
        sort(rankingList, (p1,p2) -> Double.compare(p2.v2, p1.v2));
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) {
        
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(Double.NEGATIVE_INFINITY);
        
        this.iIndex.getAllIidx().forEach(iidx -> {
            scoresMap.put((int) iidx, (double) this.metric.get(this.iidx2item(iidx)));
        });
        
        return scoresMap;
    }
    
    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter)
    {
        if(maxLength == 0)
        {
            maxLength = rankingList.size();
        }
       
        List<Tuple2id> items = rankingList.stream()
                .filter(is -> filter.test(is.v1))
                .limit(maxLength)
                .collect(toList());
        
        return new FastRecommendation(uidx, items);
    }
}
