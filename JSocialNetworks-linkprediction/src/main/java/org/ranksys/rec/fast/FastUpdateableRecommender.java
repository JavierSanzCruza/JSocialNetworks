/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.fast;

import org.ranksys.rec.updateable.UpdateableRecommender;
import org.ranksys.recommenders.fast.FastRecommender;

/**
 * Fast recommender which can be updated.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface FastUpdateableRecommender<U,I> extends FastRecommender<U,I>, UpdateableRecommender<U,I>
{
    
}
