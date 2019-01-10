/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recommender which uses the PropFlow algorithm.
 * 
 * Lichtenwalter, R., Lussier, J., Chawla, N. New perspectives and methods in link prediction.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class PropFlowRecommender<U> extends UserFastRankingRecommender<U> 
{

    /**
     * Maximum distance from the target node (maximum length of the random walk).
     */
    private final int maxLength;
    /**
     * Orientation of the edges.
     */
    private final EdgeOrientation orientation;
    
    /**
     * Constructor.
     * @param graph the original graph.
     * @param maxLength maximum distance from the target node (maximum length of the random walk).
     * @param orientation the orientation of the edges.
     */
    public PropFlowRecommender(FastGraph<U> graph, int maxLength, EdgeOrientation orientation) 
    {
        super(graph);
        this.maxLength = maxLength;
        this.orientation = orientation;
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        
        U u = uidx2user(i);
        Set<Integer> found = new HashSet<>();
        LinkedList<Integer> newSearch = new LinkedList<>();
        Int2DoubleMap propFlow = new Int2DoubleOpenHashMap();
        this.iIndex.getAllIidx().forEach(iidx -> propFlow.put(iidx, 0.0));

        
        found.add(i);
        newSearch.push(i);
        propFlow.put(i, 1.0);
        for(int j = 0; j < this.maxLength; ++j)
        {
            LinkedList<Integer> oldSearch = (LinkedList<Integer>) newSearch.clone();
            newSearch.clear();
            
            while(!oldSearch.isEmpty())
            {
                int userId = oldSearch.pop();
                U user = this.uidx2user(userId);
                Double nodeInput = propFlow.get(userId);
                
                List<U> neighbourhood = this.getGraph().getNeighbourhood(user, orientation).collect(Collectors.toCollection(ArrayList::new));
                double sumOutput = neighbourhood.stream().mapToDouble(neigh -> 
                {
                   switch(orientation)
                   {
                        case IN:
                           return this.getGraph().getEdgeWeight(neigh, user);
                        case OUT:
                           return this.getGraph().getEdgeWeight(user, neigh);
                        default:
                            return Math.max(this.getGraph().getEdgeWeight(neigh, user),this.getGraph().getEdgeWeight(user, neigh));
                   }
                }).sum();
                
                
                double flow = 0.0;
                for(U neigh : neighbourhood)
                {
                    int nIdx = this.user2uidx(neigh);
                    double weight;
                    switch(orientation)
                    {
                         case IN:
                            weight = this.getGraph().getEdgeWeight(neigh, user);
                            break;
                         case OUT:
                            weight = this.getGraph().getEdgeWeight(user, neigh);
                            break;
                         default:
                            weight = Math.max(this.getGraph().getEdgeWeight(neigh, user),this.getGraph().getEdgeWeight(user, neigh));
                    }
                    flow = nodeInput*weight/sumOutput;
                    
                    if(propFlow.containsKey(nIdx))
                    {
                        propFlow.put(nIdx, propFlow.get(nIdx)+flow);
                    }
                    else
                    {
                        propFlow.put(nIdx, flow);
                    }
                    
                    if(!found.contains(nIdx))
                    {
                        found.add(nIdx);
                        newSearch.push(nIdx);
                    }
                }              
            }            
        }
        return propFlow;
    }  
}
