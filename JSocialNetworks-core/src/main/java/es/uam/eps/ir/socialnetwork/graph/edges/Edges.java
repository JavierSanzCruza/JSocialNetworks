/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.edges;

import java.util.stream.Stream;
import org.ranksys.core.preference.fast.IdxPref;

/**
 * Interface that represents the edges of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public interface Edges
{
    /**
     * Checks if an edge exists.
     * @param orig The origin endpoint
     * @param dest The destiny endpoint
     * @return true if the edge exists, false if not.
     */
    public boolean containsEdge(int orig, int dest);

    /**
     * Gets the weight of an edge.
     * @param orig The origin endpoint.
     * @param dest The destiny endpoint.
     * @return the value if the edge exists, the default error value if not.
     */
    public double getEdgeWeight(int orig, int dest);
    
    /**
     * Gets the type of an edge.
     * @param orig The origin endpoint.
     * @param dest The destiny endpoint.
     * @return the type if the edge exists, the default error value if not.
     */
    public int getEdgeType(int orig, int dest);
    
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
     * Gets those neighbors of a node which are, at the same time, 
     * incident and adjacent.
     * @param node The node.
     * @return a stream containing all the ids of the nodes.
     */
    public Stream<Integer> getMutualNodes(int node);
    
    /**
     * Gets the types of the incident edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getIncidentTypes(int node);
    
    /**
     * Gets the types of the adjacent edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getAdjacentTypes(int node);
    
    /**
     * Gets the types of the neighbourhood edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getNeighbourTypes(int node);
    
    /**
     * Gets the types of the adjacent edges of a node, such that there is a reciprocal
     * connection in the graph.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getMutualAdjacentTypes(int node);
    
    /**
     * Gets the types of the incident edges of a node, such that there is a reciprocal
     * connection in the graph.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getMutualIncidentTypes(int node);
    
    /**
     * Gets the types of the neighbourhood edges of a node, such that there is a reciprocal
     * connection in the graph.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<EdgeType> getMutualTypes(int node);   
    
    /**
     * Gets the weights of the incident edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<IdxPref> getIncidentWeights(int node);
    
    /**
     * Gets the weights of the adjacent edges of a node.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<IdxPref> getAdjacentWeights(int node);
    
    /**
     * Gets the weights of the neighbour edges of a node.
     * @param node The node.
     * @return a sream containing all the edge types.
     */
    public Stream<IdxPref> getNeighbourWeights(int node);

    /**
     * Gets the weights of the adjacent edges of a node which are reciprocated.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<IdxPref> getMutualAdjacentWeights(int node);
    /**
     * Gets the weights of the incident edges of a node which are reciprocated.
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<IdxPref> getMutualIncidentWeights(int node);
    /**
     * For each mutual connection (node to u and u to node exist in the graph),
     * gets the average values of the incident and adjacent edge between u's and nod
     * @param node The node.
     * @return a stream containing all the edge types.
     */
    public Stream<IdxPref> getMutualWeights(int node);
    
    /**
     * Gets the number of adjacent nodes
     * @param node The node
     * @return the number of adjacent nodes.
     */
    public long getAdjacentCount(int node);
    /**
     * Gets the number of incident nodes
     * @param node The node
     * @return the number of incident nodes.
     */
    public long getIncidentCount(int node);
    /**
     * Gets the number of neighbour nodes
     * @param node The node
     * @return the number of neighbour nodes.
     */
    public long getNeighbourCount(int node);

    /**
     * Gets the number of nodes which are, at the same time,
     * adjacent and incident.
     * @param node The node.
     * @return the number of mutual neighbors.
     */
    
    public long getMutualCount(int node);
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
     * Removes an edge
     * @param orig origin node.
     * @param dest Destiny node.
     * @return true if everything went OK, false if not.
     */
    public boolean removeEdge(int orig, int dest);
    
    /**
     * Removes a node from the edge list.
     * @param idx the identifier of the node.
     * @return true if everything went OK, false otherwise.
     */
    public boolean removeNode(int idx);
    
    /**
     * Modifies the weight of an edge.
     * @param orig Origin node.
     * @param dest Destiny node.
     * @param weight The new weight of the edge.
     * @return true if everything went OK, false if not, or the edge does not exist.
     */
    public boolean updateEdgeWeight(int orig, int dest, double weight);
    
    /**
     * Obtains the number of edges in the graph.
     * @return The number of edges in the graph.
     */
    public long getNumEdges();
}
