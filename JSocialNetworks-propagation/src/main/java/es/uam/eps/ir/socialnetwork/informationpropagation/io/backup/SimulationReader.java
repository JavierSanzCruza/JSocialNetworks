/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.io.backup;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Simulation;
import java.io.Serializable;

/**
 * Interface for writing a simulation into a file.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public interface SimulationReader<U extends Serializable, I extends Serializable, P> 
{
    /**
     * Initializes the simulation writing
     * @param file the name of the file where we want to store the simulation.
     * @return true if everything went OK, false if something failed while configuring the writer.
     */
    public boolean initialize(String file);
    
    /**
     * Reads a simulation from a file
     * @param data the simulation data
     * @return the simulation if everything went OK, null if not.
     */
    public Simulation<U,I,P> readSimulation(Data<U,I,P> data);
    
    /**
     * Writes a single iteration in a file.
     * @param data the simulation data
     * @return true if everything went OK, false if something failed while writing
     */
    public Iteration<U,I,P> readIteration(Data<U,I,P> data);
    
    /**
     * Closes the writing objects.
     * @return true if everything went OK, false if something failed while deconfiguring the writer.
     */
    public boolean close();
}
