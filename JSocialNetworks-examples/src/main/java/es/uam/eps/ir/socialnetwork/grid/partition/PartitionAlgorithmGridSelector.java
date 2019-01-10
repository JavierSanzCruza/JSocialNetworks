/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.partition;

import es.uam.eps.ir.socialnetwork.grid.Parameters;
import static es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmIdentifiers.*;
import es.uam.eps.ir.socialnetwork.grid.partition.random.CountRandomPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.random.PopUnbiasedTestRandomPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.random.PopUnbiasedTrainRandomPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.random.RandomPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.weights.PercentageWeightsPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.weights.PopUnbiasedTestWeightsPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.weights.PopUnbiasedTrainWeightsPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.partition.weights.ThresholdWeightsPartitionConfigurator;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;


/**
 * Class that translates from a grid to the different train/test partition algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class PartitionAlgorithmGridSelector<U>
{
    /**
     * Algorithm name
     */
    private final String name;
    /**
     * Grid that contains the different possible parameters for the algorithm.
     */
    private final Parameters parameters;
    
    /**
     * Constructor
     * @param name Partition algorithm name
     * @param parameters Configuration parameters for the algorithm.
     */
    public PartitionAlgorithmGridSelector(String name, Parameters parameters)
    {
        this.name = name;
        this.parameters = parameters;

    }
    
    /**
     * Obtains the configured suppliers of partition algorithms using the 
     * parameters in the grid.
     * @return a map containing the different algorithm suppliers.
     */
    public Tuple2oo<String, Partition<U>> getPartitionAlgorithms()
    {
        PartitionAlgorithmConfigurator<U> gridsearch;
        switch(this.name)
        {
            // IR algorithms
            case RANDOM:
                gridsearch = new RandomPartitionConfigurator<>();
                break;
            case POPUNBIASEDTRAINRANDOM:
                gridsearch = new PopUnbiasedTrainRandomPartitionConfigurator<>();
                break;
            case POPUNBIASEDTESTRANDOM:
                gridsearch = new PopUnbiasedTestRandomPartitionConfigurator<>();
                break;
            case COUNTRANDOM:
                gridsearch = new CountRandomPartitionConfigurator<>();
                break;
            case PERCENTAGEWEIGHTS:
                gridsearch = new PercentageWeightsPartitionConfigurator<>();
                break;
            case THRESHOLDWEIGHTS:
                gridsearch = new ThresholdWeightsPartitionConfigurator<>();
                break;
            case POPUNBIASEDTRAINWEIGHTS:
                gridsearch = new PopUnbiasedTrainWeightsPartitionConfigurator<>();
                break;
            case POPUNBIASEDTESTWEIGHTS:
                gridsearch = new PopUnbiasedTestWeightsPartitionConfigurator<>();
                break;                
            // Default behavior
            default:
                gridsearch = null;
        }
        
        if(gridsearch != null)
            return gridsearch.grid(this.parameters);
        return null;
    }
}
