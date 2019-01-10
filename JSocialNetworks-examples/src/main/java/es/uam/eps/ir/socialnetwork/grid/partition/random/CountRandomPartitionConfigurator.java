/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.partition.random;

import es.uam.eps.ir.socialnetwork.grid.Parameters;
import es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmConfigurator;
import static es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmIdentifiers.COUNTRANDOM;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.random.RandomCountedPartition;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;

/**
 * Class for configuring Random partition algorithms.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * @see es.uam.eps.ir.socialnetwork.partition.random.RandomCountedPartition
 */
public class CountRandomPartitionConfigurator<U> implements PartitionAlgorithmConfigurator<U> 
{
    /**
     * Identifier for the training set percentage of links
     */
    private final static String COUNT = "count";
    
    @Override
    public Tuple2oo<String, Partition<U>> grid(Parameters params) 
    {
        int count = params.getIntegerValue(COUNT);
        
        return new Tuple2oo<>(COUNTRANDOM, new RandomCountedPartition<>(count));
    }
    
}
