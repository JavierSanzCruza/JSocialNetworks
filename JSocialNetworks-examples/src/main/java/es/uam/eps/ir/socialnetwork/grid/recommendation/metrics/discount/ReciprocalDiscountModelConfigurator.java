/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount;

import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rank.ReciprocalDiscountModel;

/**
 * Configures an ranking discount model with linear decay
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see es.uam.eps.ir.ranksys.metrics.rank.ReciprocalDiscountModel
 */
public class ReciprocalDiscountModelConfigurator<U,I> implements DiscountConfigurator<U,I> 
{
    @Override
    public RankingDiscountModel configure(DiscountParamReader params) 
    {
        return new ReciprocalDiscountModel();
    }
    
}
