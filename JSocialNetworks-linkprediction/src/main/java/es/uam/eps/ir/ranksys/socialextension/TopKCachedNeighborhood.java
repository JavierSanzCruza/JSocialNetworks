/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.ranksys.core.util.topn.IntDoubleTopN;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.recommenders.nn.neighborhood.Neighborhood;
import org.ranksys.recommenders.nn.sim.Similarity;

/**
 * Top-K neighborhood. It keeps the k most similar users/items as neighbors.
 * Caches the neighborhoods for improved performance.
 *
 * @author Javier Sanz-Cruzado Puig
 */
public class TopKCachedNeighborhood implements Neighborhood 
{

    /**
     * The similarity function
     */
    private final Similarity sim;
    /**
     * The size of the neighborhoods.
     */
    private final int k;
    /**
     * The cache.
     */
    Map<Integer, IntDoubleTopN> cache = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param sim similarity
     * @param k maximum size of neighborhood
     */
    public TopKCachedNeighborhood(Similarity sim, int k) {
        this.sim = sim;
        this.k = k;
        
        
    }

    /**
     * Returns the neighborhood of a user/index.
     *
     * @param idx user/index whose neighborhood is calculated
     * @return stream of user/item-similarity pairs.
     */
    @Override
    public Stream<Tuple2id> getNeighbors(int idx) {

        if(!this.cache.containsKey(idx))
        {   
            IntDoubleTopN topN = new IntDoubleTopN(k);
            sim.similarElems(idx).forEach(topN::add);
            cache.put(idx, topN);
        }
        
        return this.cache.get(idx).stream();
    }
}
