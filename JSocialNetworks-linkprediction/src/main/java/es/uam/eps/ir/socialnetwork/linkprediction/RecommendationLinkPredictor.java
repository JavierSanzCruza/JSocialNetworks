/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.linkprediction;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.ranksys.core.util.tuples.Tuple2od;
import java.util.SortedSet;
import java.util.TreeSet;
import org.ranksys.core.Recommendation;
import org.ranksys.recommenders.Recommender;

/**
 * Unsupervised link prediction algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class RecommendationLinkPredictor<U> extends AbstractLinkPredictor<U>
{
    /**
     * The unsupervised method.
     */
    private final Recommender<U,U> recommender;
    /**
     * The graph we are going to perform link prediction in.
     */
    
    
    /**
     * Constructor.
     * @param graph The graph we are going to perform link prediction in.
     * @param comparator The comparator for ordering the pairs
     * @param recommender The unsupervised method.
     */
    public RecommendationLinkPredictor(Graph<U> graph, Comparator<Tuple2od<Pair<U>>> comparator, Recommender<U,U> recommender)
    {
        super(graph, comparator);
        this.recommender = recommender;
    }

    @Override
    public List<Tuple2od<Pair<U>>> getPrediction(int maxLength, Predicate<Pair<U>> filter) 
    {  
        SortedSet<Tuple2od<Pair<U>>> auxSet = new TreeSet<>(this.getComparator());
        
        // Add the scores for each pair of users which pass the filter.
        this.getGraph().getAllNodes().forEach(u -> 
        {
            Set<U> vFilter = this.getGraph().getAllNodes().filter(v -> filter.test(new Pair<>(u,v))).collect(Collectors.toCollection(HashSet::new));
            int size = vFilter.size();
            Recommendation<U,U> rec = this.recommender.getRecommendation(u, vFilter.stream());
            rec.getItems().stream().forEach(score -> 
            {
                auxSet.add(new Tuple2od<>(new Pair<>(u,score.v1),score.v2));
                vFilter.remove(score.v1);
            });
            if(vFilter.isEmpty() == false)            
                vFilter.forEach(v -> auxSet.add(new Tuple2od<>(new Pair<>(u,v),Double.NEGATIVE_INFINITY))); // If some link does not appear in the recommendation.
        });
        
        List<Tuple2od<Pair<U>>> prediction = auxSet.stream().limit(maxLength).collect(Collectors.toCollection(ArrayList::new));
        return prediction;
    }    
}
