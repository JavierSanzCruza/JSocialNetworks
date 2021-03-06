/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.stop;

import es.uam.eps.ir.socialnetwork.informationpropagation.stop.NoMoreTimestampsStopCondition;
import es.uam.eps.ir.socialnetwork.informationpropagation.stop.StopCondition;
import java.io.Serializable;

/**
 * Configurator for a No More Timestamps stop condition.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * @param <I> Type of the information pieces
 * @param <P> Type of the features.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.stop.NoMoreTimestampsStopCondition
 */
public class NoMoreTimestampsStopConditionConfigurator<U extends Serializable,I extends Serializable,P> implements StopConditionConfigurator<U,I,P>
{
    @Override
    public StopCondition<U, I, P> getStopCondition(StopConditionParamReader scgr) 
    {
        return new NoMoreTimestampsStopCondition<>();
    }   
}
