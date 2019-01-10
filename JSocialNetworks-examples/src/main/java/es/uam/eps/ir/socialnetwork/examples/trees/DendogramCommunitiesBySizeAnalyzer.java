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
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.index.fast.FastIndex;
import es.uam.eps.ir.socialnetwork.utils.indexes.GiniIndex;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.lambda.tuple.Tuple3;
import org.openide.util.Exceptions;
import org.ranksys.formats.parsing.Parsers;

/**
 * Class that processes a dendogram, and analyzes balanced communities from it.
 * @author Javier Sanz-Cruzado Puig
 */
public class DendogramCommunitiesBySizeAnalyzer
{
    /**
     * Method that analyzes the different possible community selections (by community size).
     * @param args Execution arguments:
     * <ul>
     *  <li><b>Graph:</b> The graph from which the communities have been extracted</li>
     *  <li><b>Dendogram:</b> A relation of the triples that form the dendogram. <br/>
     *      <u>Format of the file:</u> childA \t childB \t parent
     *  </li>
     *  <li><b>Output:</b> Name of the output file</li>
     *  <li><b>Relation:</b> Relation between the nodes in the network, and their representation
     *  in the dendogram triplets</li>
     * </ul>
     */
    public static void main(String args[])
    {
        if(args.length < 4)
        {
            System.err.println("Error: Invalid arguments");
            System.err.println("Usage: <graph> <dendogram> <output> <relation>");
            return;
        }
        
        String graphroute = args[0];
        String dendogram = args[1];
        String output = args[2];
        String relation = args[3];
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, true, false, "\t", Parsers.lp);
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
        Map<Integer, Communities<Long>> commMap = dendo.getCommunitiesBySize();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("size\tnumComms\texpected\tavg.size\tGini\n");
            for(int i = 1; i <= commMap.size(); ++i)
            {
                Communities<Long> comm = commMap.get(i);
                int numComms = comm.getNumCommunities();
                List<Double> commSizes = comm.getCommunities().mapToDouble(c -> comm.getUsers(c).count()).boxed().collect(Collectors.toCollection(ArrayList::new));
                double avgSize = comm.getCommunities().mapToDouble(c -> comm.getUsers(c).count() + 0.0).sum();
                double expected = (graph.getVertexCount() + 0.0) / (i + 0.0);
                GiniIndex gini = new GiniIndex();
                double ginival = gini.compute(commSizes, true, numComms, avgSize);
                avgSize /= (numComms + 0.0);
                bw.write(i + "\t" + numComms + "\t" + expected + "\t" + avgSize + "\t" + ginival + "\n");
            }
        } 
        catch (IOException ex) 
        {
            Exceptions.printStackTrace(ex);
        }

        
        
    }
}
