/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.informationpieces;

import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricParamReader;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.SimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.informationpieces.individual.RealPropagatedRecall;
import java.io.Serializable;

/**
 * Configures a RealPropagatedRecall metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.metrics.informationpieces.individual.RealPropagatedRecall
 */
public class RealPropagatedRecallMetricConfigurator<U extends Serializable,I extends Serializable,P> implements MetricConfigurator<U,I,P> 
{
    @Override
    public SimulationMetric<U, I, P> configure(MetricParamReader params) 
    {
        return new RealPropagatedRecall<>();
    }
    
}
