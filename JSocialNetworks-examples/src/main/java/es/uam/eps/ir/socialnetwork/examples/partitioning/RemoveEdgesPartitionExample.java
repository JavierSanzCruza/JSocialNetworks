/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.GraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.GraphWriter;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.SubstractEdgesPartition;
import org.ranksys.formats.parsing.Parsers;

/**
 *
 * @author Javier
 */
public class RemoveEdgesPartitionExample 
{
    public static void main(String args[])
    {
        String train = args[0];
        String test = args[1];
        
        GraphReader<Long> greader = new TextGraphReader<>(false, true, false, false, "\t", Parsers.lp);
        Graph<Long> trainGraph = greader.read(train);
        Graph<Long> testGraph = greader.read(test);
        
        
        Partition<Long> partition = new SubstractEdgesPartition(trainGraph);
        
        partition.doPartition(testGraph);
        Graph<Long> defTest  = partition.cleanAndRemoveTestReciprocal();
        GraphWriter<Long> gwriter = new TextGraphWriter<>("\t");
        gwriter.write(defTest, args[2], true, false);
        
    }
}
