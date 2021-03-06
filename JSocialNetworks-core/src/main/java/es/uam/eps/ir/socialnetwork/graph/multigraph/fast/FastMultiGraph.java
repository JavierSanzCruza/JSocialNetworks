/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.multigraph.fast;


import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.Weight;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeWeight;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.Weights;
import es.uam.eps.ir.socialnetwork.graph.multigraph.edges.MultiEdges;
import es.uam.eps.ir.socialnetwork.index.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fast implementation of a multi graph
 * @author Javier Sanz-Cruzado Puig
 * @param <U> User type
 */
public abstract class FastMultiGraph<U> implements MultiGraph<U>, Serializable
{
    /**
     * Index of vertices
     */
    protected final Index<U> vertices;
    /**
     * Edges in the network
     */
    protected final MultiEdges edges;
    
    /**
     * Constructor
     * @param vertices A index for the vertices of the graph
     * @param edges Edges 
     */
    public FastMultiGraph(Index<U> vertices, MultiEdges edges)
    {
        this.vertices = vertices;
        this.edges = edges;
    }
    
    @Override
    public boolean addNode(U node)
    {
        if(vertices.containsObject(node))
            return false;
        int idx = vertices.addObject(node);
        
        if(idx != -1)
        {
            return edges.addUser(idx);
        }
        return false;
    }
        
    @Override
    public boolean addEdge(U nodeA, U nodeB, double weight, int type, boolean insertNodes)
    {
        if(insertNodes)
        {
            if(this.addNode(nodeA)) this.edges.addUser(EdgeType.getDefaultValue());
            if(this.addNode(nodeB)) this.edges.addUser(EdgeType.getDefaultValue());
        }
        
        if(this.containsVertex(nodeA) && this.containsVertex(nodeB))
        {
            return this.edges.addEdge(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB), weight, type);
        }
        return false;
    }

    @Override
    public Stream<U> getAllNodes()
    {
        return this.vertices.getAllObjects();
    }
    
    @Override
    public Stream<U> getIncidentNodes(U node)
    {
        return this.edges.getIncidentNodes(this.vertices.object2idx(node)).map(n -> this.vertices.idx2object(n));
    }

    @Override
    public Stream<U> getAdjacentNodes(U node)
    {
        return this.edges.getAdjacentNodes(this.vertices.object2idx(node)).map(n -> this.vertices.idx2object(n));
    }

    @Override
    public Stream<U> getNeighbourNodes(U node)
    {
        return this.edges.getNeighbourNodes(this.vertices.object2idx(node)).map(n -> this.vertices.idx2object(n));
    }
    
    @Override
    public Stream<U> getNeighbourhood(U node, EdgeOrientation direction)
    {
        switch (direction)
        {
            case IN:
                return this.getIncidentNodes(node);
            case OUT:
                return this.getAdjacentNodes(node);
            default:
                return this.getNeighbourNodes(node);
        }
    }
    
    @Override
    public int getIncidentEdgesCount(U node)
    {
        return this.edges.getIncidentCount(this.vertices.object2idx(node));
    }
    
    @Override
    public int getAdjacentEdgesCount(U node)
    {
        return this.edges.getAdjacentCount(this.vertices.object2idx(node));
    }
    
    @Override
    public int getNeighbourEdgesCount(U node)
    {
        return this.edges.getNeighbourCount(this.vertices.object2idx(node));
    }
    
    
    @Override
    public int getNeighbourhoodSize(U node, EdgeOrientation direction)
    {
        switch (direction)
        {
            case IN:
                return this.getIncidentNodesCount(node);
            case OUT:
                return this.getAdjacentNodesCount(node);
            default:
                return this.getNeighbourNodesCount(node);
        }
    }
    
    @Override
    public boolean containsVertex(U node)
    {
        return this.vertices.containsObject(node);
    }
    
    @Override
    public boolean containsEdge(U nodeA, U nodeB)
    {
        if(this.containsVertex(nodeA) && this.containsVertex(nodeB))
        {
            return this.edges.containsEdge(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
        }
        return false;
    }    

    @Override
    public double getEdgeWeight(U nodeA, U nodeB)
    {
        List<Double> weights = this.edges.getEdgeWeights(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
        if(weights != null && weights.size() > 0)
            return weights.get(0);
        return EdgeWeight.getErrorValue();
    }

    @Override
    public Stream<Weight<U,Double>> getIncidentNodesWeights(U node)
    {
        List<Weight<U,Double>> weights = new ArrayList<>();
        
        this.edges.getIncidentWeight(this.vertices.object2idx(node)).forEach(weight -> {
            weight.getValue().forEach(w -> {
               weights.add(new Weight<>(this.vertices.idx2object(weight.getIdx()),w));
            });
        });
        
        return weights.stream();
    }

    @Override
    public Stream<Weight<U,Double>> getAdjacentNodesWeights(U node)
    {
        List<Weight<U,Double>> weights = new ArrayList<>();
        
        this.edges.getAdjacentWeight(this.vertices.object2idx(node)).forEach(weight -> {
            weight.getValue().forEach(w -> {
               weights.add(new Weight<>(this.vertices.idx2object(weight.getIdx()),w));
            });
        });
        
        return weights.stream();
    }
    
    @Override
    public Stream<Weight<U,Double>> getNeighbourNodesWeights(U node)
    {
        List<Weight<U,Double>> weights = new ArrayList<>();
        
        this.edges.getNeighbourWeight(this.vertices.object2idx(node)).forEach(weight -> {
            weight.getValue().forEach(w -> {
               weights.add(new Weight<>(this.vertices.idx2object(weight.getIdx()),w));
            });
        });
        
        return weights.stream();
    }
    
    @Override
    public Stream<Weight<U,Double>> getNeighbourhoodWeights(U node, EdgeOrientation direction)
    {
        switch (direction)
        {
            case IN:
                return this.getIncidentNodesWeights(node);
            case OUT:
                return this.getAdjacentNodesWeights(node);
            default:
                return this.getNeighbourNodesWeights(node);
        }
    }

    @Override
    public int getEdgeType(U nodeA, U nodeB)
    {
        List<Integer> types = this.edges.getEdgeTypes(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
        if(types != null && types.size() > 0)
            return types.get(0);
        return EdgeType.getErrorType();
    }

    @Override
    public Stream<Weight<U,Integer>> getIncidentNodesTypes(U node)
    {
        List<Weight<U,Integer>> types = new ArrayList<>();
        
        this.edges.getIncidentTypes(this.vertices.object2idx(node)).forEach(type -> {
            type.getValue().forEach(t -> {
               types.add(new Weight<>(this.vertices.idx2object(type.getIdx()),t));
            });
        });
        
        return types.stream();
    }

    @Override
    public Stream<Weight<U,Integer>> getAdjacentNodesTypes(U node)
    {
        List<Weight<U,Integer>> types = new ArrayList<>();
        
        this.edges.getAdjacentTypes(this.vertices.object2idx(node)).forEach(type -> {
            type.getValue().forEach(t -> {
               types.add(new Weight<>(this.vertices.idx2object(type.getIdx()),t));
            });
        });
        
        return types.stream();
    }

    @Override
    public Stream<Weight<U,Integer>> getNeighbourNodesTypes(U node)
    {
        List<Weight<U,Integer>> types = new ArrayList<>();
        
        this.edges.getNeighbourTypes(this.vertices.object2idx(node)).forEach(type -> {
            type.getValue().forEach(t -> {
               types.add(new Weight<>(this.vertices.idx2object(type.getIdx()),t));
            });
        });
        
        return types.stream();
    }

    @Override
    public Stream<Weight<U,Integer>> getNeighbourhoodTypes(U node, EdgeOrientation direction)
    {
        switch (direction)
        {
            case IN:
                return this.getIncidentNodesTypes(node);
            case OUT:
                return this.getAdjacentNodesTypes(node);
            default:
                return this.getNeighbourNodesTypes(node);
        }
    }
    
    @Override
    public long getVertexCount()
    {
        return this.vertices.numObjects();
    }
    
    @Override
    public int getNumEdges(U nodeA, U nodeB) {
        return this.edges.getNumEdges(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
    }

    @Override
    public List<Double> getEdgeWeights(U nodeA, U nodeB) {
        return this.edges.getEdgeWeights(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
    }

    @Override
    public Stream<Weights<U, Double>> getIncidentNodesWeightsLists(U node) {
        return this.edges.getIncidentWeight(this.vertices.object2idx(node))
                .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));             
    }

    @Override
    public Stream<Weights<U, Double>> getAdjacentNodesWeightsLists(U node) {
        return this.edges.getAdjacentWeight(this.vertices.object2idx(node))
                .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));
    }

    @Override
    public Stream<Weights<U, Double>> getNeighbourNodesWeightsLists(U node) {
        return this.edges.getIncidentWeight(this.vertices.object2idx(node))
            .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue())); 
    }

    @Override
    public Stream<Weights<U, Double>> getNeighbourhoodWeightsLists(U node, EdgeOrientation orientation) {
        return this.edges.getNeighbourWeight(this.vertices.object2idx(node))
            .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));
    }

    @Override
    public List<Integer> getEdgeTypes(U nodeA, U nodeB) {
        return this.edges.getEdgeTypes(this.vertices.object2idx(nodeA), this.vertices.object2idx(nodeB));
    }

    @Override
    public Stream<Weights<U, Integer>> getIncidentNodesTypesLists(U node) {
        return this.edges.getIncidentTypes(this.vertices.object2idx(node))
            .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));
    }

    @Override
    public Stream<Weights<U, Integer>> getAdjacentNodesTypesLists(U node) {
        return this.edges.getAdjacentTypes(this.vertices.object2idx(node))
            .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));
    }

    @Override
    public Stream<Weights<U, Integer>> getNeighbourNodesTypesLists(U node) {
        return this.edges.getNeighbourTypes(this.vertices.object2idx(node))
            .map((weight) -> new Weights<>(this.vertices.idx2object(weight.getIdx()), weight.getValue()));
    }
    
    @Override
    public long getEdgeCount() 
    {
        return this.edges.getNumEdges();
    }
    
    @Override
    public Graph<U> complement()
    {
        throw new UnsupportedOperationException("The multigraph cannot be complemented");
    }
    
    @Override
    public boolean updateEdgeWeight(U orig, U dest, double weight)
    {
        throw new UnsupportedOperationException("Edges weights cannot be updated in multigraphs");
    }
    
    @Override
    public int object2idx(U u)
    {
        return this.vertices.object2idx(u);
    }
    
    @Override
    public U idx2object(int idx)
    {
        return this.vertices.idx2object(idx);
    }
}
