/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.evaluation.runner.AbstractRecommenderRunner;
import org.ranksys.recommenders.Recommender;

/**
 * Filter and inverrter runner. It creates recommendations by using the filter method
 * in the recommenders, and inverts the ranking.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class FilterInverterRecommenderRunner<U,I> extends AbstractRecommenderRunner<U,I>
{
    /**
     * User filter.
     */
    private final Function<U, Predicate<I>> userFilter;
    /**
     * Maximum length of the recommendation lists. 0 for no limit.
     */
    private final int maxLength;
    
    /**
     * Constructor.
     * 
     * @param users target users, those for which recommendations are generated.
     * @param userFilter item filter provided for each user.
     * @param maxLength maximum length of the recommendation lists, 0 for no limit
     */
    public FilterInverterRecommenderRunner(Stream<U> users, Function<U, Predicate<I>> userFilter, int maxLength) 
    {
        super(users);
        
        this.userFilter = userFilter;
        this.maxLength = maxLength;
    }

    @Override
    public void run(Recommender<U, I> recommender, Consumer<Recommendation<U, I>> consumer) 
    {
        Function<U, Recommendation<U,I>> funct = user -> 
        {
            Recommendation<U,I> rec = recommender.getRecommendation(user, 0, userFilter.apply(user));
            List<Tuple2od<I>> list = new ArrayList<>();
            
            List<Tuple2od<I>> original = rec.getItems();
            int length = (maxLength == 0) ? original.size() : maxLength;
            int origSize = original.size();

            for(int i = 0; i < length && i < origSize; ++i)
            {
                Tuple2od<I> item = original.get(origSize - i - 1);
                list.add(new Tuple2od<>(item.v1, -item.v2));
            }
            
            return new Recommendation<>(user, list);
        };
        
        run(funct, consumer);
                
    }
    
}
