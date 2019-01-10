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
import es.uam.eps.ir.socialnetwork.utils.generator.Generator;

/**
 * Multigraph generator.
 * @author Javier Sanz-Cruzado Puig
 */
@Deprecated
interface MultiGraphGenerator<V> 
{
    /**
     * Generates an empty multigraph.
     * @param directed if the multigraph is directed.
     * @param weighted if the multigraph is weighted.
     * @return the graph.
     */
    public Graph<V> generateEmptyGraph(boolean directed, boolean weighted);
    
    /**
     * Generates a random multigraph, using the Erdös-Renyi Model
     * @param directed if the multigraph is directed.
     * @param numNodes the number of nodes.
     * @param prob the probability that an edge is created.
     * @param generator a node generator.
     * @return the graph.
     */
    public Graph<V> generateRandomGraph(boolean directed, int numNodes, double prob, Generator<V> generator); 
    
    /**
     * Generates a random multigraph, using the Preferential Attachment model.
     * @param directed if the multigraph is directed.
     * @param initialNodes Initial number of nodes.
     * @param numIter Number of iterations.
     * @param numEdgesIter Number of edges created each iteration.
     * @param generator a node generator.
     * @return the graph. 
     */
    public Graph<V> generatePreferentialAttachmentGraph(boolean directed, int initialNodes, int numIter, int numEdgesIter, Generator<V> generator); 
    
}
