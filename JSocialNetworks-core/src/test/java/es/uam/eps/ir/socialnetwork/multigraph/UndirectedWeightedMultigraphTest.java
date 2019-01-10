package es.uam.eps.ir.socialnetwork.multigraph;

/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


import es.uam.eps.ir.socialnetwork.graph.Weight;
import es.uam.eps.ir.socialnetwork.graph.multigraph.UndirectedMultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.fast.FastUndirectedWeightedMultiGraph;
import java.util.List;
import java.util.TreeMap;
import static java.util.stream.Collectors.toList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Javier
 */
public class UndirectedWeightedMultigraphTest {
    
    public UndirectedWeightedMultigraphTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Tests the addition of nodes to the graph.
     */
    @Test
    public void nodeAddition()
    {
        // Build the graph
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        assertEquals(true,dg.addNode(1));
        assertEquals(true,dg.addNode(2));
        assertEquals(true,dg.addNode(3));
        assertEquals(false,dg.addNode(1));
        
        
        // Check the number of vertices
        assertEquals(3L, dg.getVertexCount());
        
        // Check non-existing nodes
        assertEquals(false, dg.containsVertex(4));
        assertEquals(false, dg.containsVertex(0));
        
        // Check existing nodes
        assertEquals(true, dg.containsVertex(1));
        assertEquals(true, dg.containsVertex(2));
        assertEquals(true, dg.containsVertex(3));
    }
    
    /**
     * Tests the addition of edges to the graph
     */
    @Test
    public void edgeAddition()
    {
        // Build the graph
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        dg.addNode(1);
        dg.addNode(2);
        dg.addNode(3);
        
        // Add some edges with already existing nodes
        assertEquals(true,dg.addEdge(1,2));
        assertEquals(true,dg.addEdge(1,2));
        assertEquals(true,dg.addEdge(1,3));
        assertEquals(true,dg.addEdge(2,3));
        
        // Check the number of edges
        assertEquals(4L, dg.getEdgeCount());
        
        assertEquals(2, dg.getNumEdges(1, 2));
        assertEquals(1, dg.getNumEdges(1, 3));
        assertEquals(1, dg.getNumEdges(2, 3));
        
        // Check non-existent edges
        // With non-existent nodes
        assertEquals(false, dg.containsEdge(4, 5));
        // With an existent node and a non-existent one
        assertEquals(false, dg.containsEdge(3,4));
        assertEquals(false, dg.containsEdge(4,3));
        // With both nodes existent
        assertEquals(true, dg.containsEdge(3,2));
        
        // Check existent edges
        assertEquals(true, dg.containsEdge(1,2));
        assertEquals(true, dg.containsEdge(1,3));
        assertEquals(true, dg.containsEdge(2,3));
        
        // Try to add an edge with a non-existent node, not adding the new node
        assertEquals(false,dg.addEdge(3, 4, false));
        assertEquals(3L, dg.getVertexCount());
        assertEquals(4L, dg.getEdgeCount());
        
        // Add a node and an edge
        assertEquals(true, dg.addEdge(3,4,true));
        assertEquals(4L, dg.getVertexCount());
        assertEquals(5L, dg.getEdgeCount());
        assertEquals(true, dg.containsEdge(3,4));
        
        // Add two nodes and an edge (default behaviour)
        assertEquals(true, dg.addEdge(5,6));
        assertEquals(6L, dg.getVertexCount());
        assertEquals(6L, dg.getEdgeCount());
        assertEquals(true, dg.containsEdge(5, 6));    
        
        // Add a reciprocate edge
        assertEquals(true, dg.addEdge(2,1));
        assertEquals(true, dg.containsEdge(2,1));
        assertEquals(7L, dg.getEdgeCount());
        
        assertEquals(3, dg.getNumEdges(2, 1));

    }
    
    /**
     * Test the weights of the edges
     */
    @Test
    public void weights()
    {
        // Build the graph
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        dg.addNode(1);
        dg.addNode(2);
        dg.addNode(3);
        
        // Add some edges with already existing nodes
        dg.addEdge(1,2,4.0);
        dg.addEdge(1,2,4.0);
        dg.addEdge(1,2,5.0);
        dg.addEdge(1,3,3.0);
        dg.addEdge(2,3);
                
        // Recover each weight and check (adjacent edges)
        List<Weight<Integer,Double>> weights = dg.getAdjacentNodesWeights(1).collect(toList());
        TreeMap<Double,Integer> treeMap = new TreeMap<>();
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);
        TreeMap<Double,Integer> auxTreeMap = new TreeMap<>();
        
        assertEquals(4, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getAdjacentNodesWeights(2).collect(toList());
        assertEquals(4, weights.size());
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
            
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getAdjacentNodesWeights(3).collect(toList());
        assertEquals(2, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
        }
        
        // Recover each weight and check (adjacent edges)
        weights = dg.getIncidentNodesWeights(1).collect(toList());
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);    
        assertEquals(4, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getIncidentNodesWeights(2).collect(toList());
        assertEquals(4, weights.size());
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
            
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getIncidentNodesWeights(3).collect(toList());
        assertEquals(2, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
        }
        
        // Recover each weight and check (all edges)
        weights = dg.getNeighbourNodesWeights(1).collect(toList());
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);
        
        assertEquals(4, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getNeighbourNodesWeights(2).collect(toList());
        assertEquals(4, weights.size());
        treeMap.put(4.0,2);
        treeMap.put(5.0,1);
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(weight.getValue()))
                {
                    auxTreeMap.put(weight.getValue(), auxTreeMap.get(weight.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(weight.getValue(), 1);
                }
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
            
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        weights = dg.getNeighbourNodesWeights(3).collect(toList());
        assertEquals(2, weights.size());
        for(Weight<Integer, Double> weight : weights)
        {
            if(weight.getIdx().equals(1))
            {
                assertEquals(3.0, (double) weight.getValue(), 0.001);
            }
            else
            {
                assertEquals(1.0, (double) weight.getValue(), 0.001);
            }
        }
        
        
        
        
        
    }
    
    /**
     * Test the weights of the edges
     */
    @Test
    public void types()
    {
        // Build the graph
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        dg.addNode(1);
        dg.addNode(2);
        dg.addNode(3);
        
        // Add some edges with already existing nodes
        dg.addEdge(1,2,4.0,1);
        dg.addEdge(1,2,4.0,1);
        dg.addEdge(1,2,4.0,2);
        dg.addEdge(1,3,5.0,2);
        dg.addEdge(2,3,3.0,3);
        
     
        
        // Recover each weight and check (adjacent)
        List<Weight<Integer,Integer>> types = dg.getAdjacentNodesTypes(1).collect(toList());
        assertEquals(4,types.size());
        TreeMap<Integer,Integer> treeMap = new TreeMap<>();
        treeMap.put(1,2);
        treeMap.put(2,1);
        TreeMap<Integer,Integer> auxTreeMap = new TreeMap<>();
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(2, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getAdjacentNodesTypes(2).collect(toList());
        assertEquals(4,types.size());
        treeMap.put(1, 2);
        treeMap.put(2,1);
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getAdjacentNodesTypes(3).collect(toList());
        assertEquals(2,types.size());
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                assertEquals(2, (int) type.getValue(), 0.001);
            }
            else
            {
                assertEquals(3, (int) type.getValue(), 0.001);
            }
        }
        
        
        // Recover each weight and check (incident)
        types = dg.getIncidentNodesTypes(1).collect(toList());
        assertEquals(4,types.size());
        treeMap.put(1,2);
        treeMap.put(2,1);
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(2, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getIncidentNodesTypes(2).collect(toList());
        assertEquals(4,types.size());
        treeMap.put(1,2);
        treeMap.put(2,1);
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getIncidentNodesTypes(3).collect(toList());
        assertEquals(2,types.size());
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                assertEquals(2, (int) type.getValue(), 0.001);
            }
            else
            {
                assertEquals(3, (int) type.getValue(), 0.001);
            }
        }
        
        // Recover each weight and check (all)
        types = dg.getNeighbourNodesTypes(1).collect(toList());
        assertEquals(4,types.size());
        treeMap.put(1,2);
        treeMap.put(2,1);
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(2))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(2, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getNeighbourNodesTypes(2).collect(toList());
        assertEquals(4,types.size());
        treeMap.put(1,2);
        treeMap.put(2,1);
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                if(auxTreeMap.containsKey(type.getValue()))
                {
                    auxTreeMap.put(type.getValue(), auxTreeMap.get(type.getValue())+1);
                }
                else
                {
                    auxTreeMap.put(type.getValue(), 1);
                }
            }
            else
            {
                assertEquals(3, (int) type.getValue());
            }
        }
        
        // Check
        while(!treeMap.isEmpty())
        {
            assertEquals(treeMap.pollFirstEntry(), auxTreeMap.pollFirstEntry());
        }
        
        types = dg.getNeighbourNodesTypes(3).collect(toList());
        assertEquals(2,types.size());
        for(Weight<Integer, Integer> type : types)
        {
            if(type.getIdx().equals(1))
            {
                assertEquals(2, (int) type.getValue(), 0.001);
            }
            else
            {
                assertEquals(3, (int) type.getValue(), 0.001);
            }
        }
        
    }    
    
    /**
     * Test the degrees of the graph
     */
    @Test
    public void degrees()
    {
        // Build the graph
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        dg.addNode(1);
        dg.addNode(2);
        dg.addNode(3);
        dg.addNode(4);
        dg.addEdge(1,2);
        dg.addEdge(1,2);
        dg.addEdge(1,3);
        dg.addEdge(2,3);
        
        // Check the in-degrees of the nodes
        assertEquals(dg.degree(1),3);
        assertEquals(dg.degree(2),3);
        assertEquals(dg.degree(3),2);
        assertEquals(dg.degree(4),0);
        
       
    }
    
    /**
     * Test that checks if the number of nodes and edges which reach a node or start from 
     * it are correct.
     */
    @Test
    public void edgesAndNodesCount()
    {
        UndirectedMultiGraph<Integer> dg = new FastUndirectedWeightedMultiGraph<>();
        dg.addNode(1);
        dg.addNode(2);
        dg.addNode(3);
        dg.addEdge(1,2);
        dg.addEdge(1,2);
        dg.addEdge(1,3);
        dg.addEdge(2,3);
        
        // Check the number of adjacent edges
        assertEquals(3, dg.getAdjacentEdgesCount(1));
        assertEquals(3, dg.getAdjacentEdgesCount(2));
        assertEquals(2, dg.getAdjacentEdgesCount(3));
        
        // Check the number of adjacent nodes
        assertEquals(2, dg.getAdjacentNodes(1).count());
        assertEquals(2, dg.getAdjacentNodes(2).count());
        assertEquals(2, dg.getAdjacentNodes(3).count());
        
        assertEquals(2, dg.getAdjacentNodesCount(1));
        assertEquals(2, dg.getAdjacentNodesCount(2));
        assertEquals(2, dg.getAdjacentNodesCount(3));
        
        // Check the number of incident edges
        assertEquals(3, dg.getIncidentEdgesCount(1));
        assertEquals(3, dg.getIncidentEdgesCount(2));
        assertEquals(2, dg.getIncidentEdgesCount(3));
        
        // Check the number of incident nodes
        assertEquals(2, dg.getIncidentNodes(1).count());
        assertEquals(2, dg.getIncidentNodes(2).count());
        assertEquals(2, dg.getIncidentNodes(3).count());
        
        assertEquals(2, dg.getIncidentNodesCount(1));
        assertEquals(2, dg.getIncidentNodesCount(2));
        assertEquals(2, dg.getIncidentNodesCount(3));
        
        dg.addEdge(2,1);
        
        // Check the number of neighbor edges
        assertEquals(4, dg.getNeighbourEdgesCount(1));
        assertEquals(4, dg.getNeighbourEdgesCount(2));
        assertEquals(2, dg.getNeighbourEdgesCount(3));
        
        // Check the number of neighbor nodes
        assertEquals(2, dg.getNeighbourNodes(1).count());
        assertEquals(2, dg.getNeighbourNodes(2).count());
        assertEquals(2, dg.getNeighbourNodes(3).count());
        
        assertEquals(2, dg.getNeighbourNodesCount(1));
        assertEquals(2, dg.getNeighbourNodesCount(2));
        assertEquals(2, dg.getNeighbourNodesCount(3));
    }
}
