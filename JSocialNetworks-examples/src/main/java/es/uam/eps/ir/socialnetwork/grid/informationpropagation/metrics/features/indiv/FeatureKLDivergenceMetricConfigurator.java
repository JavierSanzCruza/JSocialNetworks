/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.features.indiv;

import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.informationpropagation.metrics.MetricParamReader;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.SimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.indiv.FeatureKLDivergence;
import java.io.Serializable;

/**
 * Configures a Feature KL Divergence metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.indiv.FeatureKLDivergence
 */
public class FeatureKLDivergenceMetricConfigurator<U extends Serializable,I extends Serializable,P> implements MetricConfigurator<U,I,P> 
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
     * Identifier for considering unique user-item pairs or not.
     */
    private final static String UNIQUE = "unique";
    
    @Override
    public SimulationMetric<U, I, P> configure(MetricParamReader params) 
    {
        String parameter = params.getParams().getStringValue(PARAMETER);
        Boolean userParam = params.getParams().getBooleanValue(USERPARAM);
        Boolean unique = params.getParams().getBooleanValue(UNIQUE);
        
        if(parameter == null || userParam == null || unique == null)
            return null;
        
        return new FeatureKLDivergence<>(parameter, userParam, unique);
    }
    
}
