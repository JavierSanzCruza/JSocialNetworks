/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.updateable;

import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.recommenders.Recommender;

/**
 * Recommender which can be updated.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface UpdateableRecommender<U,I> extends Recommender<U,I>
{
    /**
     * Updates the values of the recommender, considering that the following 
     * preferences have been added.
     * @param tuples the preferences which have been added.
     */
    public void update(Stream<Tuple3<U,I,Double>> tuples);
    
    /**
     * Updates the values of the recommender, considering that the following 
     * preferences have been removed.
     * @param tuples the preferences which have been removed.
     */
    public void updateDelete(Stream<Tuple3<U,I,Double>> tuples);
    
    /**
     * Updates the values of the recommender, considering that a new user has been
     * added.
     * @param u the user which has been added.
     */
    public void updateAddUser(U u);
    
    /**
     * Updates the values of the recommender, considering that a new item has been
     * added.
     * @param i the item which has been added.
     */
    public void updateAddItem(I i);
    
    /**
     * Updates the values of the recommender, considering that a new rating has
     * been added.
     * @param u the user.
     * @param i the item
     * @param val the value.
     */
    public void update(U u, I i, double val);
    
    /**
     * Updates the values of the recommender, considering that a rating has been
     * deleted.
     * @param u the user.
     * @param i the item.
     * @param val the value.
     */
    public void updateDelete(U u, I i, double val);    
}
