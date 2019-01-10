/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.communities;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.graph.CompleteCommunityGraphGenerator;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.IntStream;
import org.ranksys.formats.parsing.Parsers;

/**
 *
 * @author Javier
 */
public class CommunityStudy
{
    public static void main(String args[])
    {
        if(args.length < 5)
        {
            System.err.println("usage: <graph> <comms> <directed> <weighted> <output>");
            return;
        }

        String graphRoute = args[0];
        String commsRoute = args[1];
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        String outputRoute = args[4];
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphRoute, weighted, false);
        
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        Communities<Long> comm = creader.read(commsRoute, "\t", Parsers.lp);
        
        CompleteCommunityGraphGenerator<Long> gen = new CompleteCommunityGraphGenerator<>();
        MultiGraph<Integer> commGraph = gen.generate(graph, comm);
        
        Int2LongMap map = new Int2LongOpenHashMap();
        IntStream.range(0, comm.getNumCommunities()).forEach(c -> map.put(c,comm.getUsers(c).count()));
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute))))
        {
            bw.write("comm1\tcomm2\tedges\tsize1\tsize2\n");
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                for(int j = 0; j < comm.getNumCommunities(); ++j)
                {
                    bw.write(i + "\t" + j + "\t" + commGraph.getNumEdges(i, j) + "\t" + map.get(i) + "\t" + map.get(j) + "\n");
                }
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Something failed while writing");
        }
    }
}
