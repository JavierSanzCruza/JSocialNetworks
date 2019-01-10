/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.sim.updateable;

import org.ranksys.core.preferences.fast.updateable.FastUpdateablePointWisePreferenceData;

/**
 * Updateable version of vector cosine similarity.
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 */
public class VectorCosineUpdateableSimilarity extends UpdateableVectorSimilarity
{
    /**
     * Constructor.
     * @param data preference data. It has to be a fast implementation of 
     * updateable preference data, which allows access to the value for each
     * (user, item) pair.
     */
    public VectorCosineUpdateableSimilarity(FastUpdateablePointWisePreferenceData<?,?> data) 
    {
        super(data);
    }

    @Override
    public double sim(double product, double norm2A, double norm2B) 
    {
        if(norm2A == 0 || norm2B == 0) return 0.0;
        return product / Math.sqrt(norm2A*norm2B);
    }
    
}
