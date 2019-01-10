/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.partition.weights;

import es.uam.eps.ir.socialnetwork.grid.Parameters;
import es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmConfigurator;
import static es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmIdentifiers.POPUNBIASEDTRAINWEIGHTS;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.weights.PopUnbiasedTrainWeightsPartition;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;

/**
 * Class for configuring popularity unbiased partitions, selecting a fixed number
 * of train ratings (the ones with smaller weight).
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * @see es.uam.eps.ir.socialnetwork.partition.weights.PopUnbiasedTrainWeightsPartition
 */
public class PopUnbiasedTrainWeightsPartitionConfigurator<U> implements PartitionAlgorithmConfigurator<U> 
{
    /**
     * Identifier for the training set percentage of links
     */
    private final static String PERCENTAGE = "percentage";
    /**
     * Identifier for the minimum ratio of test ratings per item. 
     */
    private final static String EPSILON = "epsilon";
    
    @Override
    public Tuple2oo<String, Partition<U>> grid(Parameters params) 
    {
        double percentage = params.getDoubleValue(PERCENTAGE);
        double epsilon = params.getDoubleValue(EPSILON);
        return new Tuple2oo<>(POPUNBIASEDTRAINWEIGHTS, new PopUnbiasedTrainWeightsPartition<>(epsilon, percentage));
    }
    
}
