/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.detection.connectedness.StronglyConnectedComponents;
import es.uam.eps.ir.socialnetwork.graph.DirectedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Recommender system that uses SALSA Algorithm
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class SALSARecommender<U> extends UserFastRankingRecommender<U>
{

    /**
     * Strongly connected components of the graph.
     */
    private final Communities<U> comm;
    /**
     * Scores for each user in the network.
     */
    private Int2DoubleMap scores;
    /**
     * Weight for each component
     */
    private Int2DoubleMap commWeight;
    /**
     * Number of authorities in each component
     */
    private Int2DoubleMap commAuth;
    /**
     * Number of hubs in each component
     */
    private Int2DoubleMap commHub;
    /**
     * Number of components
     */
    private Int2DoubleMap commCount;

    /**
     * Constructor.
     * @param graph The graph.
     * @param mode true if we want to use the Authorities score, false if we want to use the Hubs score.
     */
    public SALSARecommender(FastGraph<U> graph, boolean mode) {
        super(graph);
        
        StronglyConnectedComponents<U> scc = new StronglyConnectedComponents<>();
        this.comm = scc.detectCommunities(graph);
        
        this.scores = new Int2DoubleOpenHashMap();
        this.commAuth = new Int2DoubleOpenHashMap();
        this.commHub = new Int2DoubleOpenHashMap();
        this.commCount = new Int2DoubleOpenHashMap();
        this.commWeight = new Int2DoubleOpenHashMap();
        
        this.comm.getCommunities().forEach(c -> 
        {
            commAuth.put(c, 0.0);
            double weight = this.comm.getUsers(c).mapToDouble(u -> 
            {
                if(this.getGraph().isDirected())
                {
                    DirectedGraph<U> dgraph = (DirectedGraph<U>) this.getGraph();
                    
                    if(mode) // Authorities
                    {
                        double outDeg = dgraph.outDegree(u) + 0.0;
                        if(outDeg > 0)
                            commAuth.put(c, commAuth.get(c) + 1.0);
                        return outDeg;
                    }
                    else // Hubs
                    {
                        double inDeg = dgraph.inDegree(u) + 0.0;
                        if(inDeg > 0)
                            commHub.put(c, commHub.get(c) + 1.0);
                        return inDeg;
                    }
                }
                else
                {
                    double outDeg = this.getGraph().degree(u) + 0.0;
                    if(outDeg > 0)
                        commCount.put(c, commCount.get(c) + 1.0);
                    return outDeg;
                }
            }).sum();
            
            this.commWeight.put(c, weight);
        });
        
        
        
        uIndex.getAllUsers().forEach(u -> 
        {
            int uComm = this.comm.getCommunity(u);
            
            double value;
            if(this.getGraph().isDirected())
            {
                DirectedGraph<U> dgraph = (DirectedGraph<U>) this.getGraph();
                if(mode == true) // Authorities
                {
                    value = this.commAuth.get(uComm)*dgraph.outDegree(u)/(this.commWeight.get(uComm)+1.0);
                }
                else
                {
                    value = this.commAuth.get(uComm)*dgraph.outDegree(u)/(this.commWeight.get(uComm)+1.0);
                }
            }
            else
            {
                value = this.commCount.get(uComm)*this.getGraph().degree(u)/(this.commWeight.get(uComm)+1.0);
            }
            
            this.scores.put(uIndex.user2uidx(u), value);
        });
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        return this.scores;
    }

}
