/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.features.global;

import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricParamReader;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.SimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.global.MonteCarloFeatureGlobalUserGini;
import java.io.Serializable;

/**
 * Configures a global feature user Gini metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.global.FeatureGlobalUserGini
 */
public class MonteCarloFeatureGlobalUserGiniMetricConfigurator<U extends Serializable,I extends Serializable,P> implements MetricConfigurator<U,I,P> 
{
    /**
     * Identifier for the parameter name
     */
    private final static String PARAMETER = "parameter";
    /**
     * Identifier for the param which identifies if the studied parameter is an user or an information piece feature.
     */
    private final static String USERPARAM = "userFeature";
    /**
     * Number of MonteCarlo samples
     */
    private final static String NUMMC = "numMC";

    @Override
    public SimulationMetric<U, I, P> configure(MetricParamReader params) 
    {
        String parameter = params.getParams().getStringValue(PARAMETER);
        Boolean userParam = params.getParams().getBooleanValue(USERPARAM);
        Integer numMC = params.getParams().getIntegerValue(NUMMC);
        if(parameter == null || userParam == null || numMC == null)
            return null;
        
        return new MonteCarloFeatureGlobalUserGini<>(parameter, userParam, numMC);
    }
    
}
