/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.adapters.GraphCondenser;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.GraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.GraphWriter;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmGridReader;
import es.uam.eps.ir.socialnetwork.grid.partition.PartitionAlgorithmGridSelector;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import java.io.File;
import java.util.Set;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given a graph, applies a split, according to a grid.
 * @author Javier Sanz-Cruzado Puig
 */
public class PartitionExample 
{
    /**
     * Makes the split of a graph in train and test.
     * @param args Execution arguments.
     * <ul>
     *  <li><b>Data:</b> Route containing the whole set of links.</li>
     *  <li><b>Output folder:</b> Folder to store the splits.</li>
     *  <li><b>Grid:</b> XML file containing the configuration of the partitioning algorithms.</li>
     *  <li><b>Multigraph:</b> Indicates if the network you are trying to partition is a multigraph or not</li>
     *  <li><b>Directed:</b> Indicates if the graph is directed or undirected.</li>
     *  <li><b>Clean Test:</b> Indicates if the test graph has to be cleaned.</li>
     *  <li><b>Clean Reciprocals:</b> Indicates if reciprocal edges have to be removed from test.</li>
     *  <li><b>Condense:</b> Indicates if the graph has to be converted to a simple graph after the split.</li>
     *  <li><b>Condense weights:</b> If the graph has to be condensed, indicates if the weights will be sum, or only counted</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 8)
        {
            System.err.println("Error: Invalid arguments");
            System.err.println("Usage: <data> <outputFolder> <grid> <multigraph> <directed> <cleantest> <cleanreciprocals> <condense> <condenseweights>");
            return;
        }
        
        // Read the parameters.
        String data = args[0];
        String outputFolder = args[1];
        String grid = args[2];
        boolean multigraph = args[3].equalsIgnoreCase("true");
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean cleantest = args[5].equalsIgnoreCase("true");
        boolean cleanreciprocals = args[6].equalsIgnoreCase("true");
        boolean condense = args[7].equalsIgnoreCase("true");
        boolean condenseweights = args[8].equalsIgnoreCase("true");
        boolean useWeights = args[9].equalsIgnoreCase("true");
        
        // Read the graph data.
        GraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, true, false,"\t", Parsers.lp);
        Graph<Long> dataGraph = greader.read(data, true, false);
        
        // Reads the grid
        PartitionAlgorithmGridReader gridreader = new PartitionAlgorithmGridReader(grid);
        gridreader.readDocument();
        Set<String> partitions = gridreader.getPartitionAlgorithms();
        
        GraphWriter<Long> gwriter = new TextGraphWriter<>("\t");
        
        // Executes the partition algorithms for the graph data.
        for(String name : partitions)
        {
            File f = new File(outputFolder + name);
            if(!f.exists())
            {
                f.mkdir();
            }
            
            PartitionAlgorithmGridSelector<Long> selector = new PartitionAlgorithmGridSelector<>(name, gridreader.getParameters(name));
            Partition<Long> partition = selector.getPartitionAlgorithms().v2();
            partition.doPartition(dataGraph);
            
            Graph<Long> trainGraph = partition.getTrainGraph();
            Graph<Long> testGraph;
            if(cleantest && cleanreciprocals)
                testGraph = partition.cleanAndRemoveTestReciprocal();
            else if(cleantest)
                testGraph = partition.cleanTest();
            else if(cleanreciprocals)
                testGraph = partition.removeTestReciprocal();
            else
                testGraph = partition.getTestGraph();
            
            if(condense)
            {
                trainGraph = GraphCondenser.condense(trainGraph, condenseweights, (useWeights) ? x -> x.mapToDouble(y -> y).sum() : x -> 1.0);
                testGraph = GraphCondenser.condense(testGraph, condenseweights, (useWeights) ? x -> x.mapToDouble(y -> y).sum() : x -> 1.0);
            }
            gwriter.write(trainGraph, outputFolder + name + "\\" + "train.txt", true, false);
            gwriter.write(testGraph, outputFolder + name + "\\" + "test.txt", true, false);
        }
    }
}
