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
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.bipartite.BipartiteRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Hiperlink-Induced Topic Search (HITS) Recommender
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class HITSRecommender<U> extends BipartiteRecommender<U> 
{
    /**
     * Scores for each user.
     */
    private final Int2DoubleMap scores;
    /**
     * Number of iterations.
     */
    private static final int NUMITER = 25;
    /**
     * Minimum threshold.
     */
    private static final double THRESHOLD = 0.001;

    /**
     * Constructor.
     * @param graph the graph.
     * @param mode true if we want to recommend authorities, false if we want to recommend hubs.
     */
    public HITSRecommender(FastGraph<U> graph, boolean mode) {
        super(graph, mode);
        this.scores = this.computeHITS();
    }

    /**
     * Computes HITS algorithm
     * @return The authorities scores or the hubs scores, depending on the algorithm configuration.
     */
    private Int2DoubleMap computeHITS()
    {
        Int2DoubleMap hubScore = new Int2DoubleOpenHashMap();
        Int2DoubleMap authScore = new Int2DoubleOpenHashMap();
        hubScore.defaultReturnValue(0.0);
        authScore.defaultReturnValue(0.0);
                
        this.uIndex.getAllUidx().forEach(uIdx -> {
            hubScore.put(uIdx, 0.0);
            authScore.put(uIdx, 0.0);
        });
        // Initialize
        this.hubs.entrySet().forEach(entry -> 
        {
           int uIdx = this.user2uidx(entry.getValue());
           hubScore.put(uIdx, 1.0);
        });
        
        this.authorities.entrySet().forEach(entry -> {
            int uIdx = this.user2uidx(entry.getValue());
            authScore.put(uIdx, 1.0);
        });
        
        double threshold = Double.POSITIVE_INFINITY;
        //Iteratively
        for(int i = 0; i < NUMITER && threshold > THRESHOLD; ++i)
        {
            
            // Compute new Hubs scores
            double hubsSum =this.hubs.entrySet().stream().mapToDouble(entry -> 
            {
                Long hub = entry.getKey();
                U u = entry.getValue();
                int uIdx = this.user2uidx(u);
                double score = this.bipartiteGraph.getAdjacentNodes(hub).mapToDouble(auth -> 
                {
                    U v = authorities.get(auth);
                    int vIdx = this.user2uidx(v);
                    return authScore.get(vIdx);
                }).sum();
                
                hubScore.put(uIdx, score);
                return score*score;
            }).sum();
            
            this.uIndex.getAllUidx().forEach(uIdx -> 
            {
                hubScore.put(uIdx, hubScore.get(uIdx)/Math.sqrt(hubsSum));
            });
            
            // Compute new Authorities scores
            double authSum = this.hubs.entrySet().stream().mapToDouble(entry -> 
            {
                Long auth = entry.getKey();
                U u = entry.getValue();
                
                double score = this.bipartiteGraph.getIncidentNodes(auth).mapToDouble(hub -> 
                {
                    U v = authorities.get(hub);
                    int vIdx = this.user2uidx(v);
                    return hubScore.get(vIdx);
                }).sum();
                
                return score*score;
            }).sum();
            
            this.uIndex.getAllUidx().forEach(uIdx -> 
            {
                authScore.put(uIdx, authScore.get(uIdx)/Math.sqrt(authSum));
            });
        }
        
        if(mode)
            return authScore;
        else
            return hubScore;
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        return this.scores;
    }

    

    
    
}
