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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Personalized HITS Recommender.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class PersonalizedHITSRecommender<U> extends BipartiteRecommender<U> 
{

    /**
     * Teleport rate.
     */
    private final double alpha;
    /**
     * Convergence threshold.
     */
    private static final double THRESHOLD = 0.01;
    /**
     * Constructor
     * @param graph original graph.
     * @param mode true to recommend authorities, false to recommend hubs.
     * @param alpha the teleport rate.
     */
    public PersonalizedHITSRecommender(FastGraph<U> graph, boolean mode, double alpha) 
    {
        super(graph, mode);
        this.alpha = alpha;
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        U u = uIndex.uidx2user(i);
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        Map<U, Double> hubsMap = new HashMap<>();
        Map<U, Double> authMap = new HashMap<>();
        
        this.hubs.entrySet().forEach(hub -> 
        {
            if(hub.getValue().equals(u))
                hubsMap.put(hub.getValue(), 1.0);
            else
                hubsMap.put(hub.getValue(), 0.0);
        });
        
        this.authorities.entrySet().forEach(auth -> 
        {
            authMap.put(auth.getValue(), 1.0/(this.authorities.size()));
        });
        
        
        double diff;
        do // Compute Personalized HITS
        {
            Map<U, Double> auxAuth = new HashMap<>();
            Map<U, Double> auxHubs = new HashMap<>();
            this.authorities.entrySet().stream().forEach(entry -> 
            {
                long bIdx = entry.getKey();
                U user = entry.getValue();
                
                double newAuthScore = this.bipartiteGraph.getIncidentNodes(bIdx)
                        .mapToDouble(wIdx -> hubsMap.get(this.hubs.get(wIdx)))
                        .sum();
                auxAuth.put(user, newAuthScore);
            });
            
            double sumAuth = Math.sqrt(auxAuth.values().stream().mapToDouble(score -> score*score).sum());
            
            diff = this.authorities.entrySet().stream().mapToDouble(entry -> 
            {
                double old = authMap.get(entry.getValue());
                double newAux = auxAuth.get(entry.getValue())/sumAuth;
                authMap.put(entry.getValue(), newAux);
                return Math.abs(old-newAux);
            }).sum();
            
            this.hubs.entrySet().stream().forEach(entry -> 
            {
                long bIdx = entry.getKey();
                U user = entry.getValue();
                
                double newHubScore = this.bipartiteGraph.getAdjacentNodes(bIdx)
                        .mapToDouble(wIdx -> authMap.get(this.authorities.get(wIdx)))
                        .sum();
                newHubScore *= (1-this.alpha);
                
                if(user.equals(u))
                {
                    newHubScore += this.alpha;
                }

                auxHubs.put(user, newHubScore);
                
            });
            
            double sumHubs = Math.sqrt(auxHubs.values().stream().mapToDouble(score -> score*score).sum());
            
            diff += this.hubs.entrySet().stream().mapToDouble(entry -> 
            {
                double old = hubsMap.get(entry.getValue());
                double newAux = auxHubs.get(entry.getValue())/sumHubs;
                hubsMap.put(entry.getValue(), newAux);
                return Math.abs(old-newAux);
            }).sum();
        }
        while(diff > THRESHOLD);
        
        if(this.mode == true) //Authorities
        {
            this.uIndex.getAllUsers().forEach(v -> 
            {
                int vIdx = this.uIndex.user2uidx(v);
                if(authMap.containsKey(v))
                {
                    scores.put(vIdx, authMap.get(v).doubleValue());
                }
                else
                {
                    scores.put(vIdx,0.0);
                }
            });
        }
        else // Hubs
        {
            this.uIndex.getAllUsers().forEach(v -> 
            {
                int vIdx = this.uIndex.user2uidx(v);
                if(hubsMap.containsKey(v))
                {
                    scores.put(vIdx, hubsMap.get(v).doubleValue());
                }
                else
                {
                    scores.put(vIdx,0.0);
                }
            });
        }
            
            
        return scores;
    }

    
    
}
