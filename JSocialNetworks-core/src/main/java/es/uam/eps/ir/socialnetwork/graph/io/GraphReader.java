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
import es.uam.eps.ir.socialnetwork.index.Index;
import java.io.InputStream;

/**
 * Interface for graph readers.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 */
public interface GraphReader<U>
{
    /**
     * Given a file, reads a graph. 
     * @param file the file containing the nodes.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(String file);
    
    /**
     * Given a file, reads a graph.
     * @param file The file containing the graph.
     * @param readWeights true if the file contains weights, false if not.
     * @param readTypes true if the file contains types, false if not.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(String file, boolean readWeights, boolean readTypes);
    
    /**
     * Given an file, reads a graph.
     * @param file The file containing the graph.
     * @param readWeights true if the file contains weights, false if not.
     * @param readTypes true if the file contains graph types.
     * @param users an index containing the users in the network.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(String file, boolean readWeights, boolean readTypes, Index<U> users);
    
    /**
     * Given an input stream, reads a file from it (for reading embedded graphs in greater files)
     * By default, assumes the graph contains information about weights, but not about types.
     * @param stream the input stream we read the graph from.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(InputStream stream);
    
    /**
     * Given an input stream, reads a file from it (for reading embedded graphs in greater files)
     * @param stream the input stream we read the graph from.
     * @param readWeights true if the file contains weights, false if not.
     * @param readTypes true if the file contains graph types.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(InputStream stream, boolean readWeights, boolean readTypes);
    
    /**
     * Given an input stream, reads a file from it (for reading embedded graphs in greater files)
     * @param stream the input stream we read the graph from.
     * @param readWeights true if the file contains weights, false if not.
     * @param readTypes true if the file contains graph types.
     * @param users an index containing the users in the network.
     * @return the graph if everything goes OK, null if not.
     */
    public Graph<U> read(InputStream stream, boolean readWeights, boolean readTypes, Index<U> users);
}
