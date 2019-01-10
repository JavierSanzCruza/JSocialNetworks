/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import java.util.stream.Stream;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;

/**
 * Class that represents both user and item indexes for a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public interface GraphIndex<U> extends FastItemIndex<U>, FastUserIndex<U>
{
    @Override
    public default int item2iidx(U i) 
    {
        return this.user2uidx(i);
    }

    @Override
    public default U iidx2item(int i) 
    {
        return this.uidx2user(i);
    }

    @Override
    public default boolean containsItem(U i) 
    {
        return this.containsUser(i);
    }

    @Override
    public default int numItems() 
    {
        return this.numUsers();
    }

    @Override
    public default Stream<U> getAllItems() 
    {
        return this.getAllUsers();
    }
    
   
}
