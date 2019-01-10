/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import org.ranksys.recommenders.nn.sim.Similarity;
import org.ranksys.recommenders.nn.user.neighborhood.UserNeighborhood;
import org.ranksys.recommenders.nn.user.sim.UserSimilarity;

/**
 * Top k user neighborhood. Caches the neighborhoods for improved performance.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class TopKCachedUserNeighborhood<U> extends UserNeighborhood<U> 
{
    /**
     * Constructor.
     * @param similarity Similarity function between users.
     * @param k The maximum size of the neighborhood of a single user.
     */
    public TopKCachedUserNeighborhood(UserSimilarity<U> similarity, int k) 
    {
        super(similarity, new TopKCachedNeighborhood((Similarity) similarity,k));
    }
}
