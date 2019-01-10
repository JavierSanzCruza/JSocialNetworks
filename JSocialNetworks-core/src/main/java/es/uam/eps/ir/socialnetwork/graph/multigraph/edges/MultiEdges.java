/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.multigraph.edges;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface that represents the edges of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public interface MultiEdges
{
    /**
     * Checks if an edge exists.
     * @param orig The origin endpoint
     * @param dest The destiny endpoint
     * @return true if the edge exists, false if not.
     */
    public boolean containsEdge(int orig, int dest);
    
    /**
     * Gets the number of existing edges between two destinies
     * @param orig The origin endpoint
     * @param dest The destiny endpoint
     * @return the number of edges
     */
    public int getNumEdges(int orig, int dest);
    
    /**
     * Gets the weight of an edge.
     * @param orig The origin endpoint.
     * @param dest The destiny endpoint.
     * @return the value if the edge exists, the default error value if not.
     */
    public List<Double> getEdgeWeights(int orig, int dest);
    
    /**
     * Gets the type of an edge.
     * @param orig The origin endpoint.
     * @param dest The destiny endpoint.
     * @return the type if the edge exists, the default error value if not.
     */
    public List<Integer> getEdgeTypes(int orig, int dest);
    
    /**
     * Gets the incoming neighbourhood of a node.
     * @param node The node.
     * @return a stream of all the ids of nodes.
     */
    public Stream<Integer> getIncidentNodes(int node);
    
    /**
     * Gets the outgoing neighbourhood of a node.
     * @param node The node.
     * @return a stream containing all the ids of the nodes.
     */
    public Stream<Integer> getAdjacentNodes(int node);
    
    /**
     * Gets the full neighbourhood of a node.
     * @param node The node.
     * @return a stream containing all the ids of the nodes.
     */
    public Stream<Integer> getNeighbourNodes(int node);
    
    /**
     * Gets the types of the incident edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<MultiEdgeTypes> getIncidentTypes(int node);
    
    /**
     * Gets the types of the adjacent edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<MultiEdgeTypes> getAdjacentTypes(int node);
    
    /**
     * Gets the types of the neighbourhood edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<MultiEdgeTypes> getNeighbourTypes(int node);
    
    /**
     * Gets the weights of the incident edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<MultiEdgeWeights> getIncidentWeight(int node);
    
    /**
     * Gets the weights of the adjacent edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<MultiEdgeWeights> getAdjacentWeight(int node);
    
    /**
     * Gets the weights of the neighbour edges of a node.
     * @param node The node.
     * @return a sream containing all the edge types.
     */
    public Stream<MultiEdgeWeights> getNeighbourWeight(int node);

    /**
     * Adds a user to the edges.
     * @param idx Identifier of the user
     * @return the user. 
     */
    public boolean addUser(int idx);
    
    /**
     * Adds an edge to the set.
     * @param orig Origin node
     * @param dest Destiny node
     * @param weight Weight of the edge
     * @param type Type of the edge
     * @return true if everything went OK, false if not.
     */
    public boolean addEdge(int orig, int dest, double weight, int type);
    
    /**
     * Gets the number of incident edges to a node
     * @param dest Destiny node
     * @return the number of incident edges
     */
    public int getIncidentCount(int dest);
    
    /**
     * Gets the number of adjacent edges to a node
     * @param orig Origin node
     * @return the number of adjacent edges
     */
    public int getAdjacentCount(int orig);
    
    /**
     * Gets the number of edges which reach or start in a certain node
     * @param node the node
     * @return the number of edges which reach or start in that node
     */
    public int getNeighbourCount(int node);
    /**
     * Obtains the number of edges
     * @return the number of edges
     */
    public long getNumEdges();

    
}
