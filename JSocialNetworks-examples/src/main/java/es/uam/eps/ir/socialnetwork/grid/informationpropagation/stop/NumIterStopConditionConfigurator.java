/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.stop;

import es.uam.eps.ir.socialnetwork.informationpropagation.stop.NumIterStopCondition;
import es.uam.eps.ir.socialnetwork.informationpropagation.stop.StopCondition;
import java.io.Serializable;

/**
 * Configures a stop condition based on the number of iterations of the simulation.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * @param <I> Type of the items
 * @param <P> Type of the parameters
 */
public class NumIterStopConditionConfigurator<U extends Serializable,I extends Serializable,P> implements StopConditionConfigurator<U,I,P>
{
    /**
     * Identifier for the number of iterations
     */
    private final static String NUMITER = "numIter";
    
    @Override
    public StopCondition<U,I,P> getStopCondition(StopConditionParamReader scgr)
    {
        int numIter = scgr.getParams().getIntegerValue(NUMITER);
        return new NumIterStopCondition(numIter);
    }
}
