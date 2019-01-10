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
import java.util.function.IntPredicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.ranksys.core.Recommendation;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.evaluation.runner.AbstractRecommenderRunner;
import org.ranksys.recommenders.Recommender;
import org.ranksys.recommenders.fast.FastRecommender;

/**
 * Fast filter and inverrter runner. It creates recommendations by using the filter method
 * in the recommenders, and inverts the ranking.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class FastFilterInverterRecommenderRunner<U,I> extends AbstractRecommenderRunner<U,I>
{
    /**
     * User index.
     */
    private final FastUserIndex<U> userIndex;
    /**
     * Item index.
     */
    private final FastItemIndex<I> itemIndex;
    /**
     * User filter.
     */
    private final Function<U, IntPredicate> userFilter;
    /**
     * Maximum length of the recommendation lists. 0 for no limit.
     */
    private final int maxLength;
    
    /**
     * Constructor.
     * 
     * @param userIndex User index.
     * @param itemIndex Item index.
     * @param users target users, those for which recommendations are generated.
     * @param userFilter item filter provided for each user.
     * @param maxLength maximum length of the recommendation lists, 0 for no limit
     */
    public FastFilterInverterRecommenderRunner(FastUserIndex<U> userIndex, FastItemIndex<I> itemIndex, Stream<U> users, Function<U, IntPredicate> userFilter, int maxLength) 
    {
        super(users);
        
        this.userIndex = userIndex;
        this.itemIndex = itemIndex;
        this.userFilter = userFilter;
        this.maxLength = maxLength;
    }

    @Override
    public void run(Recommender<U, I> recommender, Consumer<Recommendation<U, I>> consumer) 
    {
        Function<U, Recommendation<U,I>> funct = user -> 
        {
            FastRecommendation r = ((FastRecommender<U, I>) recommender).getRecommendation(userIndex.user2uidx(user), 0, userFilter.apply(user));
            
            List<Tuple2id> list = new ArrayList<>();
            List<Tuple2id> original = r.getIidxs();
            int origsize = original.size();
            for(int i = 0; i < maxLength && i < origsize; ++i)
            {
                Tuple2id item = original.get(origsize - i - 1);
                list.add(new Tuple2id(item.v1, -item.v2));
            }
             return new Recommendation<>(userIndex.uidx2user(r.getUidx()), list.stream()
                    .map(itemIndex::iidx2item)
                    .collect(toList()));
        };
        
        run(funct, consumer);
                
    }
    
}
