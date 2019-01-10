/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.filter;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.filter.DataFilter;
import java.io.Serializable;

/**
 * Configures a data filter from a set of parameters.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.data.filter.DataFilter
 */
public interface FilterConfigurator<U extends Serializable,I extends Serializable,P> 
{
    /**
     * Configures a data filter.
     * @param fgs filter configuration.
     * @return The configured filter.
     */
    public DataFilter<U,I,P> getFilter(FilterParamReader fgs);
}
