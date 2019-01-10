/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;


import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Triplet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.IntPredicate;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.preference.fast.IdxPref;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 * Recommender that uses an implementation similar to a document-based query processing task from search. This recommenders just take users at
 * two steps away from the target user.
 * 
 * Büttcher, S., Clarke, C.L.A. and Cormack, G.V. Information retrieval: implementing and evaluating search engines. The MIT Press, 2010.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class CommonNeighborsDocBasedRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Neighbour selection for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighbour selection for the candidate user.
     */
    private final EdgeOrientation vSel;
    
    /**
     * Triplet comparator
     */
    private final Comparator<Triplet<IdxPref, Iterator<IdxPref>, IdxPref>> comparator = (Triplet<IdxPref, Iterator<IdxPref>, IdxPref> x, Triplet<IdxPref, Iterator<IdxPref>, IdxPref> y) ->
    {
        return (int) (x.v1().v1 - y.v1().v1);
    };
    
    private final Comparator<Tuple2id> rankingComp = (Tuple2id x, Tuple2id y) ->
    {
        int c = Double.compare(x.v2(), y.v2());
            if(c != 0)
                return c;
            else
               c = Integer.compare(x.v1(), y.v1());
            return c;  
    };
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel neighbour selection for the target user
     * @param vSel neighbour selection for the candidate user
     */
    public CommonNeighborsDocBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
    }
    
    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter)
    {
        EdgeOrientation auxSel = vSel.invertSelection();
        List<Tuple2id> recommendations = new ArrayList<>();
        if(uidx == -1)
        {
            return new FastRecommendation(uidx, recommendations);
        }
       
        int numNeighs = this.graph.getNeighborhood(uidx, uSel).reduce(0, (x,y)->x+1);
        if(numNeighs == 0) return new FastRecommendation(uidx, recommendations);
        
        // Document heap
        PriorityQueue<Triplet<IdxPref,Iterator<IdxPref>,IdxPref>> queue = new PriorityQueue<>(numNeighs, comparator);
        // Ranking heap
        PriorityQueue<Tuple2id> ranking = new PriorityQueue<>(Math.min(maxLength,this.numItems()), rankingComp);
        
        // Obtain the iterators for each "query element", i.e. for the neighbors of the target user.
        
        this.getGraph().getNeighborhoodWeights(uidx, uSel).forEach(w -> 
        {
            Iterator<IdxPref> iterator = this.graph.getNeighborhoodWeights(w.v1, auxSel).iterator();
            if(iterator.hasNext())
            {
                queue.add(new Triplet<>(iterator.next(), iterator, w));
            }
        });
       
        int currentId = -1;
        double currentValue = 0;
       
        // While there are elements in the document heap
        while(!queue.isEmpty())
        {
            Triplet<IdxPref, Iterator<IdxPref>, IdxPref> tuple = queue.poll();

            if(currentId != tuple.v1().v1)
            {
                if(currentId != -1)
                {
                    // Check if the element must be added to the ranking.
                    // If it is, add the element to the ranking hea
                    if(filter.test(currentId))
                    {
                        double norm = this.normalization(uidx, currentId, currentValue);

                        Tuple2id rankingtuple = new Tuple2id(currentId, norm);
                        ranking.add(rankingtuple);
                        
                        if(ranking.size() > maxLength)
                        {
                            ranking.poll();
                        }
                    }
                }

                currentValue = 0;
                currentId = tuple.v1().v1;
            }

            if(tuple.v2().hasNext())
            {
                IdxPref next = tuple.v2().next();
                queue.add(new Triplet<>(next, tuple.v2(), tuple.v3()));
            }

            // Update the value of each element.
            currentValue += this.getValue(uidx, tuple.v1().v1, tuple.v3().v1, tuple.v3().v2, tuple.v1().v2);
        }
        
        // The remaining element, which was not previously stored
        if(currentId != -1 && filter.test(currentId))
        {
            double norm = this.normalization(uidx, currentId, currentValue);
                        
            Tuple2id rankingtuple = new Tuple2id(currentId, norm);
            ranking.add(rankingtuple);
            
            if(ranking.size() > maxLength)
            {
                ranking.poll();
            }
        }
        
        // Reorder the ranking, and obtain the recommendation ranking
        while(!ranking.isEmpty())
        {
            recommendations.add(0, ranking.poll());
        }
        
        return new FastRecommendation(uidx, recommendations);
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int uidx)
    {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);

        int numNeighs = this.getGraph().getNeighborhood(uidx, uSel).reduce(0, (x,y) -> x+1);
        if(numNeighs == 0) return scoresMap;
        
        PriorityQueue<Triplet<IdxPref, Iterator<IdxPref>, IdxPref>> queue = new PriorityQueue<>(numNeighs, comparator);
        EdgeOrientation auxSel = vSel.invertSelection();

        Set<U> set = new HashSet<>();
        this.getGraph().getNeighborhoodWeights(uidx, uSel).forEach(w -> 
        {
            Iterator<IdxPref> iterator = this.graph.getNeighborhoodWeights(w.v1, auxSel).iterator();
            if(iterator.hasNext())
            {
                queue.add(new Triplet<>(iterator.next(), iterator, w));
            }
        });
        
        int currentId = -1;
        double currentValue = 0;
        while(!queue.isEmpty())
        {
            Triplet<IdxPref, Iterator<IdxPref>, IdxPref> tuple = queue.poll();
            
            if(currentId != tuple.v1().v1)
            {
                if(currentId != -1)
                {
                    scoresMap.put(currentId, normalization(uidx, currentId, currentValue));
                }
              
                
                currentValue = 0;
                currentId = tuple.v1().v1;
            }  
                
            if(tuple.v2().hasNext())
            {
                IdxPref next = tuple.v2().next();

                queue.add(new Triplet<>(next, tuple.v2(), tuple.v3()));
            }
            
            currentValue += this.getValue(uidx, tuple.v1().v1, tuple.v3().v1,tuple.v3().v2, tuple.v1().v2);
        }

        scoresMap.put(currentId, normalization(uidx, currentId, currentValue));
        return scoresMap;
    }

    
    /**
     * Obtains the neighbour selection for the target user
     * @return the neighbour selection for the target user
     */
    protected EdgeOrientation getTargetOrientation()
    {
        return this.uSel;
    }
    
    /**
     * Obtains the neighbour selection for the candidate user
     * @return the neighbour selection for the candidate user
     */
    protected EdgeOrientation getCandidateOrientation()
    {
        return this.vSel;
    }
    
    /**
     * Obtains the recommendation value for a pair of target/candidate users.
     * @param uidx identifier for the target user.
     * @param vidx identifier for the candidate user.
     * @param widx the identifier for intermediate user between the target and the candidate.
     * @param uW (u,w) weight
     * @param vW (v,w) weight
     * @return the corresponding value.
     */
    protected abstract double getValue(int uidx, int vidx, int widx, double uW, double vW);
    
    /**
     * Applies a normalization for the final recommendation score.
     * @param uidx identifier for the target user.
     * @param vidx identifier for the candidate user.
     * @param score the raw recommendation score
     * @return the normalized value
     */
    protected abstract double normalization(int uidx, int vidx, double score);
}
