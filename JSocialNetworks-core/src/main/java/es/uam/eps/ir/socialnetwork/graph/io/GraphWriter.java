/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.io;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import java.io.OutputStream;

/**
 * Interface for graph writers.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 */
public interface GraphWriter<U>
{
    /**
     * Writes a graph into a file. It writes the weights, but not the types
     * @param graph The graph we want to write.
     * @param file The file.
     * @return true if everything went OK, false if not.
     */
    public boolean write(Graph<U> graph, String file);
    
    /**
     * Writes a graph into an output stream. It writes the weights, but not the types
     * @param graph The graph we want to write.
     * @param file The output stream.
     * @return true if everything went OK, false if not.
     */
    public boolean write(Graph<U> graph, OutputStream file);
    
    /**
     * Writes a graph into a file. Simple graphs types are written, while multigraph
     * types are not.
     * @param graph The graph we want to write.
     * @param file The file.
     * @param writeWeights indicates if weights have to be written.
     * @param writeTypes indicates if types have to be written.
     * @return true if everything is ok, false if not
     */
    public boolean write(Graph<U> graph, String file, boolean writeWeights, boolean writeTypes);
    
    /**
     * Writes a graph into a output stream. Simple graphs types are written, while multigraph
     * types are not.
     * @param graph The graph we want to write.
     * @param file The file.
     * @param writeWeights indicates if weights have to be written.
     * @param writeTypes indicates if types have to be written.
     * @return true if everything is ok, false if not
     */
    public boolean write(Graph<U> graph, OutputStream file, boolean writeWeights, boolean writeTypes);
    
    
}
