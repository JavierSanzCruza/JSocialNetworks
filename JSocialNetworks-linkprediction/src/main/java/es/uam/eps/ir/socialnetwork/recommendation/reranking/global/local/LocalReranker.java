/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local;

import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ranksys.core.Recommendation;

/**
 * Generalization of the local rerankers for processing several of them in a row.
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public abstract class LocalReranker<U,I> implements GlobalReranker<U,I> 
{

    /**
     * Random number generator seed.
     */
    private final long seed;
    
    /**
     * Constructor.
     */
    public LocalReranker()
    {
        seed = 0;
    }
    
    /**
     * Constructor.
     * @param seed Random number generator seed.
     */
    public LocalReranker(long seed)
    {
        this.seed = seed;
    }
    
    @Override
    public Stream<Recommendation<U, I>> rerankRecommendations(Stream<Recommendation<U, I>> recommendation, int maxLength) 
    {
        List<Recommendation<U,I>> recommendations = recommendation.collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(recommendations, new Random(seed));
        
        List<Recommendation<U,I>> output = new ArrayList<>();
        for(Recommendation<U,I> rec : recommendations)
        {
            Recommendation<U,I> reranked = this.rerankRecommendation(rec, maxLength);
            output.add(reranked);
            this.update(reranked);
        }
        return output.stream();
    }

    /**
     * Updates the values, given a new recomemendation.
     * @param reranked The reranked recommendation.
     */
    protected abstract void update(Recommendation<U, I> reranked);

    /**
     * Given a recommendation, reranks it.
     * @param rec The recommendation to rerank
     * @param maxLength Maximum length of the recommendation
     * @return The updated recommendation.
     */
    protected abstract Recommendation<U, I> rerankRecommendation(Recommendation<U, I> rec, int maxLength);
    
}
