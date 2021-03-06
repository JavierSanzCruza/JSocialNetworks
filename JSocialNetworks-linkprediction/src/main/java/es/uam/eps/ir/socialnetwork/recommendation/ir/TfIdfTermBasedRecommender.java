/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Adaptation of the TF-IDF method of Information Retrieval for user recommendation
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class TfIdfTermBasedRecommender<U> extends UserFastRankingRecommender<U> 
{   
    /**
     * Target users' inverse document frequency
     */
    private final Int2DoubleMap uIdf;
    /**
     * Candidate users' inverse document frequency
     */
    private final Int2DoubleMap vIdf;
    /**
     * tf-idf vector modules for each user
     */
    private final Int2DoubleMap mod;
    /**
     * Neighborhood selection for the target users.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighborhood selection for the candidate users.
     */
    private final EdgeOrientation vSel;
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     */
    public TfIdfTermBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph);
              
        this.uSel = uSel;
        this.vSel = vSel.invertSelection();
        this.uIdf = new Int2DoubleOpenHashMap();
        this.mod = new Int2DoubleOpenHashMap();
        EdgeOrientation uAuxOrient = uSel.invertSelection();
        EdgeOrientation vAuxOrient = vSel.invertSelection();
        
        if(!graph.isDirected() || uSel.equals(vSel))
        {
            this.getAllUidx().forEach(uidx -> 
            {
                uIdf.put(uidx, this.calculateIdf(uidx, uAuxOrient));
            });
            this.vIdf = uIdf;
        }
        else
        {
            this.vIdf = new Int2DoubleOpenHashMap();
            this.getAllUidx().forEach(uidx -> 
            {
                uIdf.put(uidx, this.calculateIdf(uidx, uAuxOrient));
                vIdf.put(uidx, this.calculateIdf(uidx, vAuxOrient));
            });
        }

        this.getAllUidx().forEach(vidx -> 
        {
            double module = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(widx -> 
            {
                double val = this.calculateTf(widx.v2)*this.vIdf.get(widx.v1);
                return val*val;
            }).sum();
            this.mod.put(vidx, module);
        });
    }


    private double calculateTf(double weight)
    {
        return 1.0 + Math.log(weight)/Math.log(2.0);
    }
    
    /**
     * Compute the inverse document frequency of a node
     * @param u the node
     * @param s the orientation of the neighbors
     * @return the value of the idf
     */
    private double calculateIdf(int uidx, EdgeOrientation s)
    {
        double num = this.getGraph().getNeighborhood(uidx, s).count() + 0.0;
        double val = Math.log(1.0 + ((double) this.numUsers()) / (num + 1.0))/Math.log(2.0);
        return val;
    }
        
    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);

        
        graph.getNeighborhoodWeights(uidx, uSel).forEach(w -> 
        {            
            int widx = w.v1;
            double uW = this.calculateTf(w.v2)*this.uIdf.get(widx);
            double vW = this.vIdf.get(widx);
            
            graph.getNeighborhoodWeights(widx, vSel).forEach(v -> 
            {
                double val = uW*this.calculateTf(v.v2)*vW;
                scoresMap.addTo(v.v1, val);
            });
        });
        
        scoresMap.replaceAll((vidx, val) -> val/Math.sqrt(this.mod.get((int) vidx)));
        
        return scoresMap;
    }
}
