/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.random.RandomCountedPartition;
import es.uam.eps.ir.socialnetwork.partition.random.RandomPartition;
import org.ranksys.formats.parsing.Parsers;

/**
 * Shows how the partitioning module works. To do that, this module generates 
 * a random subsample of a given graph, and stores the resulting partition in 
 * two separate files. This method also cleans the graph so that there is not node
 * in test that does not previously appear in train.
 * @author Javier Sanz-Cruzado Puig
 */
public class RandomCountedPartitionExample 
{
    /**
     * Starting from a graph, this method generates eleven subsamples by gradually
     * varying the probability of selecting a node from uniform probability to
     * degree proportional probability.
     * @param args Execution arguments. All of them are compulsory: 
     * <ol>
     *  <li><b>Original Graph:</b> A file containing the original graph</li>
     *  <li><b>Folder:</b> A folder in which to store the subsamples</li>
     *  <li><b>Directed:</b> true if the graph is directed, false if not</li>
     *  <li><b>Weighted:</b> true if the graph is weighted, false if not</li>
     *  <li><b>Percentage:</b> Percentage of nodes in the subsample</li>
     * </ol>
     */
    public static void main(String[] args)
    {
        if(args.length < 5)
        {
            System.out.println("Usage: <Original Graph> <folder> <directed> <weighted> <Train Graph>");
            return;
        }
        
        String graphReader = args[0];
        String graphFolder = args[1];
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        int count = new Integer(args[4]);
        
        
        // Step 1: Read the graph
        TextGraphReader<String> greader = new TextGraphReader<>(false, directed, weighted, false, args[1], Parsers.sp);
        Graph<String> graph = greader.read(graphReader, weighted, false);
        
        if(graph == null)
        {
            System.err.println("ERROR: Something failed while reading the graph " + graphReader);
            return;
        }
        // Step 2: Generate the partition
        Partition<String> part = new RandomCountedPartition<>(count);
        if(part.doPartition(graph))
        {
            TextGraphWriter<String> gwriter = new TextGraphWriter<>("\t");
            
            if(!gwriter.write(part.getTrainGraph(), graphFolder + "train.txt", true, false))
            {
                System.err.println("ERROR: Something failed while writing the graph " + graphFolder + "train.txt");
                return;
            }
            if(!gwriter.write(part.cleanTest(), graphFolder + "test.txt", true, false))
            {
                System.err.println("ERROR: Something failed while writing the graph " + graphFolder + "train.txt");
            }
        }
        else
        {
            System.err.println("ERROR: Partition could not be done");
        }
    }
}
