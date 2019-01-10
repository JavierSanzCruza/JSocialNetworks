/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.trees;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.Dendogram;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesWriter;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.index.fast.FastIndex;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.formats.parsing.Parsers;

/**
 * Class that processes a dendogram, and computes balanced communities from it.
 * @author Javier Sanz-Cruzado Puig
 */
public class DendogramCommunitiesBySizeFinder
{
    /**
     * Method that finds a single community selection from a dendogram (by community size).
     * @param args Execution arguments:
     * <ul>
     *  <li><b>Graph:</b> The graph from which the communities have been extracted</li>
     *  <li><b>Dendogram:</b> A relation of the triples that form the dendogram. <br/>
     *      <u>Format of the file:</u> childA \t childB \t parent
     *  </li>
     *  <li><b>Output:</b> Name of the output file</li>
     *  <li><b>Relation:</b> Relation between the nodes in the network, and their representation
     *  in the dendogram triplets</li>
     *  <li><b>Size:</b> The maximum size of the communities</li>
     * </ul>
     */
    public static void main(String args[])
    {
        if(args.length < 5)
        {
            System.err.println("Error: Invalid arguments");
            System.err.println("Usage: <graph> <dendogram> <output> <relation> <size>");
            return;
        }
        
        String graphroute = args[0];
        String dendogram = args[1];
        String output = args[2];
        String relation = args[3];
        int size = new Integer(args[4]);
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, true, true, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphroute, true, false); 
        
        Map<Integer, Long> auxMap = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relation))))
        {
            br.lines().forEach(line -> {
                String[] split = line.split("\t");
                auxMap.put(new Integer(split[0]), Parsers.lp.parse(split[1]));
            });
        }
        catch(IOException ioe)
        {
            System.err.println("Something failed while reading the dendogram");
            return;
        }
        
        FastIndex<Long> index = new FastIndex<>();
        for(int i = 0; i < graph.getVertexCount(); ++i)
        {
            index.addObject(auxMap.get(i));
        }
        
        List<Tuple3<Integer, Integer, Integer>> merges = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dendogram))))
        {        
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                int first = Parsers.ip.parse(split[0]);
                int second = Parsers.ip.parse(split[1]);
                int third = Parsers.ip.parse(split[2]);
                
                merges.add(0, new Tuple3<>(first,second, third));
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Something failed while reading the dendogram");
            return;
        }
        
        
        Dendogram<Long> dendo = new Dendogram(index, graph, merges.stream());
        Communities<Long> comm = dendo.getCommunitiesBySize(size);
        CommunitiesWriter cwriter = new CommunitiesWriter();
        cwriter.write(comm, output, "\t");
    }
}
