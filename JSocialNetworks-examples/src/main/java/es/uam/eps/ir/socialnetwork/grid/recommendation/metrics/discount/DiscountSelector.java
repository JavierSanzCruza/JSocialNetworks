/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount.DiscountIdentifiers.*;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import org.ranksys.metrics.rank.RankingDiscountModel;

/**
 * Class that selects an individual ranking discount model.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class DiscountSelector<U,I> 
{  
    /**
     * Selects and configures a ranking discount model.
     * @param spr Parameters for the ranking discount model.
     * @return A pair containing the name and the selected ranking discount model.
     */
    public Tuple2oo<String, RankingDiscountModel> select(DiscountParamReader spr)
    {
        String name = spr.getName();
        DiscountConfigurator<U,I> conf;
        switch(name)
        {
            case EXPONENTIAL:
                conf = new ExponentialDiscountModelConfigurator<>();
                break;
            case LOGARITHMIC:
                conf = new LogarithmicDiscountModelConfigurator<>();
                break;
            case NODISCOUNT:
                conf = new NoDiscountModelConfigurator<>();
                break;
            case RECIPROCAL:
                conf = new ReciprocalDiscountModelConfigurator<>();
                break;
            default:
                return null;
        }
        
        RankingDiscountModel discount = conf.configure(spr);
        return new Tuple2oo<>(name, discount);
    }
}
