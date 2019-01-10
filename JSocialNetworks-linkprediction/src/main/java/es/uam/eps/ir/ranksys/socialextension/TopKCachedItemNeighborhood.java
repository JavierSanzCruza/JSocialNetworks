/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import org.ranksys.recommenders.nn.item.neighborhood.ItemNeighborhood;
import org.ranksys.recommenders.nn.item.sim.ItemSimilarity;
import org.ranksys.recommenders.nn.sim.Similarity;

/**
 * Top k item neighborhood. Caches the neighborhoods for improved performance.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <I> Type of the items
 */
public class TopKCachedItemNeighborhood<I> extends ItemNeighborhood<I> 
{
    /**
     * Constructor.
     * @param similarity the item similarity function.
     * @param k the maximum size of the neighborhood.
     */
    public TopKCachedItemNeighborhood(ItemSimilarity<I> similarity, int k) 
    {
        super(similarity, new TopKCachedNeighborhood((Similarity) similarity,k));
    }
}
