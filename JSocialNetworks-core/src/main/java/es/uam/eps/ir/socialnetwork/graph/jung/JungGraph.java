/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.jung;

import edu.uci.ics.jung.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.UnweightedGraph;
import es.uam.eps.ir.socialnetwork.graph.Weight;
import es.uam.eps.ir.socialnetwork.graph.generator.ComplementaryGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <a href="http://jung.sourceforge.net/">JUNG</a> graph Wrapper.
 * Edges weights and types are not allowed in this types of graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public abstract class JungGraph<U> implements UnweightedGraph<U>
{
    /**
     * The JUNG Graph we are using
     */
    private final Graph<U, Integer> graph;
 
    /**
     * Constructor.
     * @param graph The Jung graph. 
     */
    public JungGraph(Graph<U, Integer> graph)
    {
        this.graph = graph;
    }
    
    @Override
    public boolean addNode(U node)
    {
        return this.graph.addVertex(node);
    }

    @Override
    public boolean addEdge(U nodeA, U nodeB, double weight, int type, boolean insertNodes)
    {
        if(insertNodes)
        {
            this.graph.addVertex(nodeA);
            this.graph.addVertex(nodeB);
        }
        
        if(graph.findEdge(nodeA, nodeB) != null)
        {
            return graph.addEdge(graph.getEdgeCount(), nodeA,nodeB);
        }
        
        return false;
    }

    @Override
    public Stream<U> getAllNodes()
    {
        return this.graph.getVertices().stream().sorted();
    }

    @Override
    public Stream<U> getIncidentNodes(U node)
    {
        return this.graph.getPredecessors(node).stream().sorted();
    }

    @Override
    public Stream<U> getAdjacentNodes(U node)
    {
        return this.graph.getSuccessors(node).stream().sorted();
    }
    


    @Override
    public Stream<U> getNeighbourNodes(U node)
    {
        return this.graph.getNeighbors(node).stream().sorted();
    }
    
    @Override
    public Stream<U> getMutualNodes(U node)
    {
        Set<U> pred = new HashSet<>(this.graph.getPredecessors(node));
        Set<U> succ = new HashSet<>(this.graph.getSuccessors(node));
        pred.retainAll(succ);
        return pred.stream().sorted();
    }

    @Override
    public int degree(U node)
    {
        return graph.degree(node);
    }

    @Override
    public int getIncidentEdgesCount(U node)
    {
        return graph.getPredecessorCount(node);
    }

    @Override
    public int getAdjacentEdgesCount(U node)
    {
        return graph.getSuccessorCount(node);
    }

    @Override
    public int getNeighbourEdgesCount(U node)
    {
        return graph.getNeighborCount(node);
    }
    


    @Override
    public int getMutualEdgesCount(U node)
    {
        return (int) this.getMutualNodes(node).count();
    }

    @Override
    public boolean containsVertex(U node)
    {
        return graph.containsVertex(node);
    }

    @Override
    public boolean containsEdge(U nodeA, U nodeB)
    {
        return graph.findEdge(nodeA, nodeB) != null;
    }
       
    @Override
    public int getEdgeType(U nodeA, U nodeB)
    {
        if(this.containsEdge(nodeA, nodeB))
            return EdgeType.getDefaultValue();
        return EdgeType.getErrorType();
    }

    @Override
    public Stream<Weight<U, Integer>> getIncidentNodesTypes(U node)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<Weight<U, Integer>> getAdjacentNodesTypes(U node)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<Weight<U, Integer>> getNeighbourNodesTypes(U node)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<Weight<U, Integer>> getNeighbourhoodTypes(U node, EdgeOrientation direction)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Stream<Weight<U,Integer>> getAdjacentMutualNodesTypes(U node)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }
    
    @Override
    public Stream<Weight<U,Integer>> getIncidentMutualNodesTypes(U node)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }
    
    @Override
    public long getVertexCount() {
        return this.graph.getVertexCount();
    }

    @Override
    public long getEdgeCount() {
        return this.graph.getEdgeCount();
    }
    
    @Override
    public es.uam.eps.ir.socialnetwork.graph.Graph<U> complement()
    {
        GraphGenerator<U> gg = new ComplementaryGraphGenerator<>();
        gg.configure(this);
        try
        {
            return gg.generate();
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }
    
    @Override
    public int object2idx(U u)
    {
        throw new UnsupportedOperationException("Not available yet.");
    }
    
    @Override
    public U idx2object(int idx)
    {
        throw new UnsupportedOperationException("Not available yet.");
    }
}
