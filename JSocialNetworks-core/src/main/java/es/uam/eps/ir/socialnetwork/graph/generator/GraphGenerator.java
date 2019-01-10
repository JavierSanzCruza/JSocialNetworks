/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.generator;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;

/**
 * Generates different graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> Type of the vertices.
 */
public interface GraphGenerator<V> 
{
    /**
     * Configures the generator.
     * @param configuration An array containing the configuration parameters.
     */
    public void configure(Object...configuration);
    /**
     * Generates a graph.
     * @return The generated graph.
     * @throws es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException The generator is not configured.
     * @throws es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException The generator parameters are incorretct.
     */
    public Graph<V> generate() throws GeneratorNotConfiguredException, GeneratorBadConfiguredException;
}
