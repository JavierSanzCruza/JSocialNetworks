/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SimRank recommender.
 * 
 * 
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class SimRankRecommender<U> extends UserFastRankingRecommender<U> {

    /**
     * free parameter.
     */
    private final double c;
    /**
     * Matrix containing the values.
     */
    private final DoubleMatrix2D matrix;
    /**
     * Convergence threshold
     */
    private final static double THRESHOLD = 0.01;
    /**
     * Maximum number of iterations
     */
    private final int numIter;
    
    /**
     * Constructor
     * @param graph the graph.
     * @param c the free parameter. Takes values between 0 and 1.
     * @param numIter maximum number of iterations.
     */
    public SimRankRecommender(FastGraph<U> graph, double c, int numIter) {
        super(graph);
        this.c = c;
        this.numIter = numIter;
        matrix = this.computeSimRank();
    }

    /**
     * Constructor
     * @param graph the original graph.
     * @param c the free parameter. Takes values between 0 and 1
     */
    public SimRankRecommender(FastGraph<U> graph, double c)
    {
        this(graph, c, 5);
    }
    
    /**
     * Constructor. Uses default values: c = 0.8, 20 iterations
     * @param graph The original graph.
     */
    public SimRankRecommender(FastGraph<U> graph)
    {
        this(graph, 0.8, 5);
    }
    
    /**
     * Computes the SimRank values of a matrix.
     * @return a matrix containing the SimRank values for each pair of users.
     */
    private DoubleMatrix2D computeSimRank() 
    {
        DoubleMatrix2D newSimRank = DoubleFactory2D.sparse.identity(this.numUsers());
        
        double threshold = Double.POSITIVE_INFINITY;
        for(int i = 0; i < this.numIter && threshold > THRESHOLD; ++i)
        {
            DoubleMatrix2D oldSimRank = newSimRank;
            DoubleMatrix2D aux = new SparseDoubleMatrix2D(this.numUsers(),this.numUsers());
            
            DoubleMatrix2D partials = new SparseDoubleMatrix2D(this.numUsers(), this.numUsers());
            this.uIndex.getAllUsers().parallel().forEach(u -> 
            {
                List<U> incident = this.getGraph().getIncidentNodes(u).collect(Collectors.toCollection(ArrayList::new));
                int uIdx = this.user2uidx(u);
                this.uIndex.getAllUidx().forEach(vIdx -> 
                {
                    double partial = incident.stream().mapToDouble(w -> 
                    {
                        int wIdx = this.uIndex.user2uidx(w);
                        return oldSimRank.get(vIdx,wIdx);
                    }).sum();
                    
                    partials.setQuick(uIdx, vIdx, partial);
                });
            });
            
            threshold = this.uIndex.getAllUsers().mapToDouble(u -> 
            {
                int uIdx = this.uIndex.user2uidx(u);
                double uI = this.getGraph().getIncidentNodesCount(u) + 0.0;
                double acc = this.uIndex.getAllUsers().mapToDouble(v -> 
                {
                    if(u.equals(v))
                    {
                        aux.setQuick(uIdx,uIdx,1.0);
                        return 0.0;
                    }
                    else
                    {
                        int vIdx = this.uIndex.user2uidx(v);
                        double vI = this.getGraph().getIncidentNodesCount(v) + 0.0;
                        double value = this.getGraph().getIncidentNodes(v).mapToDouble(w -> 
                        {
                            int wIdx = this.uIndex.user2uidx(w);
                            return partials.getQuick(uIdx,wIdx);
                        }).sum();

                        value *= this.c/(uI*vI);
                        aux.setQuick(uIdx, vIdx, value);
                        return value - oldSimRank.getQuick(uIdx, vIdx);
                    }
                }).sum();
                
                return acc;
                
            }).sum();
            
            newSimRank = aux;
        }
        
        return newSimRank;   
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        this.uIndex.getAllUidx().forEach(vIdx -> {
            scores.put(vIdx, this.matrix.get(i, vIdx));
        });
        return scores;
    }   
}
