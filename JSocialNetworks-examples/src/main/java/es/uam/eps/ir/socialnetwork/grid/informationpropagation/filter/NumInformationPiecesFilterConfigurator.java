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
import es.uam.eps.ir.socialnetwork.informationpropagation.data.filter.NumInformationPiecesFilter;
import java.io.Serializable;

/**
 * Configures a NumTweets Filter.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the information pieces.
 * @param <P> type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.data.filter.NumInformationPiecesFilter
 */
public class NumInformationPiecesFilterConfigurator<U extends Serializable,I extends Serializable,P> implements FilterConfigurator<U,I,P>
{
    /**
     * Identifier for the name of the tag field to consider.
     */
    private final static String NUMTWEETS = "numTweets";
    
    @Override
    public DataFilter<U, I, P> getFilter(FilterParamReader fgr) 
    {
        Integer numTweets = fgr.getParams().getIntegerValue(NUMTWEETS);
        return new NumInformationPiecesFilter<>(numTweets);
    }
}
