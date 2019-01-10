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
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;

/**
 * Class for performing the grid search for a given partition algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public interface PartitionAlgorithmConfigurator<U>
{
    /**
     * Obtains the different recommendation partition algorithms to execute in a grid.
     * @param params The parameters of the partition algorithm
     * @return the configuration parameters.
     */
    public Tuple2oo<String, Partition<U>> grid(Parameters params);
}
