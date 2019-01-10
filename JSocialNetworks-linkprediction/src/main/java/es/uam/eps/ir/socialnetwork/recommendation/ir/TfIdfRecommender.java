/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptation of the TF-IDF method of Information Retrieval for user recommendation
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class TfIdfRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Neighborhood selection for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighborhood selection for the candidate user.
     */
    private final EdgeOrientation vSel;
    /**
     * Inverse Document Frequency (IDF) for each user
     */
    private final Map<U, Double> idf;
    /**
     * IDF for each user using their incoming neighbors.
     */
    private final Map<U, Double> idfIn;
    /**
     * IDF for each user using their outgoing neighbors.
     */
    private final Map<U, Double> idfOut;
    /**
     * TF-IDF vector modules for each user.
     */
    private final Map<U, Double> mod;
    /**
     * Number of users.
     */
    private final int numUsers;
    
    /**
     * Constructor
     * @param graph Graph
     * @param uSel Neighbour selection for the target user
     * @param vSel Neighbour selection for the candidate user
     */
    public TfIdfRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph);
        
        this.uSel = uSel;
        this.vSel = vSel;
        this.idf = new HashMap<>();
        this.idfIn = new HashMap<>();
        this.idfOut = new HashMap<>();
        this.numUsers = uIndex.numUsers();
        this.mod = new HashMap<>();
        
        // Compute the Inverse Document Frequencies (IDF) for each node
        this.calculateIdf(this.getGraph(), uSel.invertSelection());
        if(!uSel.equals(vSel))
        {
            this.calculateIdf(this.getGraph(), vSel.invertSelection());
        }
        
        this.computeNorms(this.getGraph(), vSel);       
    }
    
    /**
     * Computes the IDF for each user in the network
     * @param graph The graph
     * @param orientation The orientation
     */
    private void calculateIdf(Graph<U> graph, EdgeOrientation orientation)
    {
        graph.getAllNodes().forEach(u -> 
        {
            double uIdf = this.calculateIdf(u, orientation);
            if(orientation.equals(EdgeOrientation.IN))
            {
                idfIn.put(u, uIdf);
            }
            else if(orientation.equals(EdgeOrientation.OUT))
            {
                idfOut.put(u, uIdf);
            }
            else
            {
                idf.put(u, uIdf);
            }
        });
    }
    
    /**
     * Compute the IDF of a node
     * @param w The node
     * @param s The orientation of the neighbours
     * @return The value of IDF
     */
    private double calculateIdf(U w, EdgeOrientation s)
    {
        switch(s)
        {
            case OUT:
                if(!idfOut.containsKey(w))
                {
                    long wNeigh = this.getGraph().getNeighbourhood(w, s).count();
                    double val = Math.log(1.0 + ((double) numUsers / (wNeigh + 1.0)))/Math.log(2.0);
                    idfOut.put(w,val);
                }
                return idfOut.get(w);
            case IN:
                if(!idfIn.containsKey(w))
                {
                    long wNeigh = this.getGraph().getNeighbourhood(w, s).count();
                    double val = Math.log(1.0 + ((double) numUsers / (wNeigh + 1.0)))/Math.log(2.0);
                    idfIn.put(w,val);
                }
                return idfIn.get(w);
            case UND:
                if(!idf.containsKey(w))
                {
                    long wNeigh = this.getGraph().getNeighbourhood(w, s).count();
                    double val = Math.log(1.0 + ((double) numUsers / (wNeigh + 1.0)))/Math.log(2.0);
                    idf.put(w,val);
                }
                return idf.get(w);
            default:
                return 0.0;
        }
    }
    
    /**
     * Compute the TF of a node
     * @param u The document node
     * @param w The term node
     * @param orientation The orientation of the neighbours
     * @return The value of TF
     */
    private double calculateTf(U u, U w, EdgeOrientation orientation)
    {
        return 1.0 + Math.log(this.getFreq(u, w, orientation))/Math.log(2.0);
    }
    
    /**
     * Computes the vector norm for each user in the network
     * @param graph The graph
     * @param orientation The orientation
     */
    private void computeNorms(Graph<U> graph, EdgeOrientation orientation)
    {
        graph.getAllNodes().forEach(u -> 
        {
            double norm = graph.getNeighbourhood(u, orientation).mapToDouble(w -> 
            {
                double wTf;
                double wIdf;
                
                wIdf = this.calculateIdf(w, orientation.invertSelection());
                wTf = this.calculateTf(u,w,orientation);
               
                return wTf*wTf*wIdf*wIdf;
            }).sum();
            
            norm = Math.sqrt(norm);
            this.mod.put(u, norm);
        });
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        U u = this.uidx2user(i);
        
        Set<U> uUsers  = this.getGraph().getNeighbourhood(u, uSel).collect(Collectors.toCollection(HashSet::new));
        this.getGraph().getAllNodes().forEach(v -> 
        {
            Set<U> vUsers  = this.getGraph().getNeighbourhood(v, vSel).collect(Collectors.toCollection(HashSet::new));
            if(uUsers == null || vUsers == null || uUsers.isEmpty() || vUsers.isEmpty() || this.mod.get(v) == 0.0)
            {
                scores.put(iIndex.item2iidx(v), 0.0);
            }
            else
            {
                double value = vUsers.stream().filter(w -> uUsers.contains(w)).mapToDouble(w -> 
                {
                    double uTf;
                    double uIdf;
                    double vTf;
                    double vIdf;

                    uTf = this.calculateTf(u, w, uSel);
                    vTf = this.calculateTf(v, w, vSel);
                    uIdf = this.calculateIdf(w, uSel.invertSelection());
                    vIdf = this.calculateIdf(w, vSel.invertSelection());
                    
                    return uTf*uIdf*vTf*vIdf;
                    
                }).sum();
                
                value /= this.mod.get(v);  
                scores.put(iIndex.item2iidx(v), value);
            }
        });
        
        return scores;
    }
    
    
    
    
}
