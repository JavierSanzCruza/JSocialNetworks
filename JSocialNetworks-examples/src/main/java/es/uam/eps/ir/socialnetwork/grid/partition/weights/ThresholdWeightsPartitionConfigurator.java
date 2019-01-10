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
import static es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmIdentifiers.THRESHOLDWEIGHTS;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.weights.ThresholdWeightsPartition;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;

/**
 * Class for configuring a partitioning algorithm that puts the top weighted links
 * in test, and the rest in training. The weights are classified acording to a threshold.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * @see es.uam.eps.ir.socialnetwork.partition.weights.ThresholdWeightsPartition
 */
public class ThresholdWeightsPartitionConfigurator<U> implements PartitionAlgorithmConfigurator<U> 
{
    /**
     * Identifier for the training set percentage of links
     */
    private final static String THRESHOLD = "threshold";
    
    @Override
    public Tuple2oo<String, Partition<U>> grid(Parameters params) 
    {
        double threshold = params.getDoubleValue(THRESHOLD);
        
        return new Tuple2oo<>(THRESHOLDWEIGHTS, new ThresholdWeightsPartition<>(threshold));
    }
    
}
