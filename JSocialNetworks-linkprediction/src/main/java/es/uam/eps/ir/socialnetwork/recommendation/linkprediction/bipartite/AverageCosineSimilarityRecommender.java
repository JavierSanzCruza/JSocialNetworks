/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.bipartite;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Recommender. This method computes all the similarities between the authorities,
 * and scores recommended contacts by the average similarity over the authorities
 * that the target user is currently following.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class AverageCosineSimilarityRecommender<U> extends BipartiteRecommender<U> 
{

    /**
     * Vectorial representation of the authorities
     */
    private final Map<U, DoubleMatrix1D> authVectors;
    /**
     * Norm of the vectors which represent the authorities
     */
    private final Map<U, Double> normVectors;
    /**
     * Identifiers of the hubs
     */
    private final Map<Long, Integer> hubIdx;
    /**
     * Similarities between pairs of users.
     */
    private final Object2DoubleMap<Pair<U>> similarities;
    
    /**
     * Constructor
     * @param graph the graph.
     */
    public AverageCosineSimilarityRecommender(FastGraph<U> graph) 
    {
        super(graph, true);
        this.authVectors = new HashMap<>();
        this.normVectors = new HashMap<>();
        this.hubIdx = new HashMap<>();
        int i = 0;
        for(Long hub : hubs.keySet())
        {
            hubIdx.put(hub, i);
            ++i;
        }
        
        this.authorities.entrySet().stream().forEach(entry -> 
        {
           DoubleMatrix1D vector = new SparseDoubleMatrix1D(this.hubs.size());
           double norm = this.computeVector(entry.getKey(), vector);
           this.authVectors.put(entry.getValue(), vector);
           this.normVectors.put(entry.getValue(), norm);
        });
        
        this.similarities = new Object2DoubleOpenHashMap<>();
        this.similarities.defaultReturnValue(-1.0);
        Algebra alg = new Algebra();
        authVectors.entrySet().forEach(first -> 
        {
            U u = first.getKey();
            DoubleMatrix1D uVec = first.getValue();
            double uNorm = this.normVectors.get(u);
           authVectors.entrySet().forEach(second -> 
           {
               U v = second.getKey();
               DoubleMatrix1D vVec = second.getValue();
               double vNorm = this.normVectors.get(v);
               double cos = alg.mult(uVec, vVec);
               cos = cos / (uNorm*vNorm);
               this.similarities.put(new Pair<>(u,v), cos);
           });
        });
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        scores.defaultReturnValue(-1.0);

        U u = this.uIndex.uidx2user(i);
        
        // Get the identity in the graph.     
        long bIdx = this.hubs.entrySet().stream().filter(entry -> entry.getValue().equals(u)).findFirst().get().getKey();

        this.authorities.entrySet().stream().forEach(auth -> 
        {
            U v = auth.getValue();
            int vIdx = uIndex.user2uidx(v);
            
            // obtain the average similarity score for this authority
            
            double score = this.bipartiteGraph.getAdjacentNodes(bIdx).mapToDouble(uAuth -> 
            {
                if(!Objects.equals(uAuth, auth.getKey()))
                {
                    U w = this.authorities.get(uAuth);
                    Pair<U> pair = new Pair<>(v,w);
                    return this.similarities.getDouble(pair);
                }
                return 0.0;
            }).sum();
            
            int count = this.bipartiteGraph.getAdjacentNodesCount(bIdx);
            if(count > 0)
                score /= (count + 0.0);
            
            scores.put(vIdx, score);
        });
        
        return scores;
    }

    
    /**
     * Computes the vector for an authority
     * @param auth The identifier of the authority
     * @param vector The vector
     * @return The norm of the vector (L2 norm)
     */
    private double computeVector(long auth, DoubleMatrix1D vector)
    {
        double module;
        
        module = this.hubs.keySet().stream().mapToDouble(hub -> 
        {
            if(this.bipartiteGraph.containsEdge(hub, auth))
            {
                double w = this.bipartiteGraph.getEdgeWeight(hub, auth);
                vector.setQuick(hubIdx.get(hub), w);
                return w*w;
            }
            return 0.0;
        }).sum();
        
        return Math.sqrt(module);
    }    
}
