/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.selection;

import es.uam.eps.ir.socialnetwork.informationpropagation.selections.RecommenderSelectionMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.selections.SelectionMechanism;
import java.io.Serializable;

/**
 * Configures a Recommender selection mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public class RecommenderSelectionConfigurator<U extends Serializable,I extends Serializable,P> implements SelectionConfigurator<U,I,P>
{
    /**
     * Identifier for the number of own pieces of information to propagate.
     */
    private final static String NUMOWN = "numOwn";
    /**
     * Identifier for the number of received pieces to propagate.
     */
    private final static String NUMREC = "numRec";
    /**
     * Identifier for the probability of selecting a piece received by a recommended user.
     */
    private final static String PROB = "prob";
    
    @Override
    public SelectionMechanism<U,I,P> configure(SelectionParamReader params) 
    {
        int numOwn = params.getParams().getIntegerValue(NUMOWN);
        int numRec = params.getParams().getIntegerValue(NUMREC);
        double prob = params.getParams().getDoubleValue(PROB);
        
        return new RecommenderSelectionMechanism(numOwn, numRec, prob);
    }
    
}
