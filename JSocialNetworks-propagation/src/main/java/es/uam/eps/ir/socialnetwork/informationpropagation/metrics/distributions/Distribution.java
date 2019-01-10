/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.metrics.distributions;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import java.io.Serializable;

/**
 * Interface for defining a distribution of elements.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public interface Distribution<U extends Serializable,I extends Serializable,P> 
{    
    /**
     * Initializes the necessary parameters for the distribution
     * @param data the data.
     */
    public void initialize(Data<U,I,P> data);
    
    /**
     * Updates the different values of the distribution
     * @param iteration The current iteration.
     */
    public void update(Iteration<U,I,P> iteration);
    
    /**
     * Prints the distribution into a file
     * @param file The output file.
     */
    public void print(String file);
    
    /**
     * Resets the distribution.
     */
    public void clear();
    
    /**
     * Obtains the name of the distribution
     * @return the name
     */
    public String getName();
    
    /**
     * Checks if the distribution has been initialized.
     * @return true if the distribution has been initialized, false if it has not.
     */
    public boolean isInitialized();
}
