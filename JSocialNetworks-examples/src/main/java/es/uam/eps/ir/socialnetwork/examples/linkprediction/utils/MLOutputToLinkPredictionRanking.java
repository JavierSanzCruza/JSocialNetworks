/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.utils;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiPredicate;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given a the output of a link prediction algorithm (not necessarily ordered), formats it for recommendation.
 * @author Javier Sanz-Cruzado Puig
 */
public class MLOutputToLinkPredictionRanking 
{
    /**
     * Program that translates link prediction rankings to recommendation files.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Train graph:</b> Route to the location of the training graph</li>
     *  <li><b>Input folder:</b> Path of the link prediction ranking files</li>
     *  <li><b>Output folder:</b> Path of the directory which will store the recommendation files</li>
     *  <li><b>Top N:</b> Number of edges to recommend to each target user</li>
     *  <li><b>Directed:</b> Indicates if the graph is directed (true) or not</li>
     *  <li><b>Weighted:</b> Indicates if the graph is weighted (true) or not</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <trainGraph> <testGraph> <input folder> <output folder> <topN> <directed> <weighted>");
            return;
        }
        
        String trainRoute = args[0];
        String testRoute = args[1];
        String recIn = args[2];
        String recOut = args[3];
        int topN = Parsers.ip.parse(args[4]);
        boolean directed = args[5].equalsIgnoreCase("true");
        boolean weighted = args[6].equalsIgnoreCase("true");
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> trainGraph = greader.read(trainRoute, true, false);
        Graph<Long> testGraph = greader.read(testRoute, true, false);
        
        // Predicted edges must be in training graph
        BiPredicate<Long, Long> inTrain = (u,v) -> trainGraph.containsEdge(u, v);
        // Predicted edges must not be autoloops
        BiPredicate<Long, Long> notSelf = (u,v) -> !u.equals(v);
        // Predicted edges must have both users in test
        BiPredicate<Long, Long> linksInTest = (u,v) -> testGraph.getNeighbourNodesCount(u)*testGraph.getNeighbourNodesCount(v) > 0;
        /*
        // Filters.
        // Predicted edges must not be in training graph
        BiPredicate<Long,Long> inTrain = (u,v) -> !graph.containsEdge(u,v);
        // Predicted edges must not be reciprocal to the existing ones
        BiPredicate<Long,Long> notReciprocal = (u,v) -> !graph.containsEdge(v,u);
        // Predicted edges must be autoloops
        BiPredicate<Long,Long> notSelf = (u,v) -> !u.equals(v);
        // Final predicate must follow all the previous ones.
        BiPredicate<Long,Long> filter = inTrain.and(notReciprocal).and(notSelf);
        */
        
        BiPredicate<Long,Long> filter = inTrain.and(notSelf).and(linksInTest);
        File f = new File(recIn);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to evaluate");
        }
        
        // Read and store the ranking into individual recommendations
        for(String rec : recommenders)
        {
            Map<Long, Queue<Tuple2od<Long>>> pairs = new HashMap<>();
            Long a = System.currentTimeMillis();
            Map<Long, List<Tuple2od<Long>>> pair = new HashMap<>();
            int k = 0;

            List<Tuple3<Long, Long, Double>> list = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recIn + rec))))
            {
                String line;
                while((line = br.readLine()) != null)
                {
                    String[] split = line.split("\t");
                    long v1 = Parsers.lp.parse(split[0]);
                    long v2 = Parsers.lp.parse(split[1]);
                    double v3 = Parsers.dp.parse(split[2]);
                    
                    
                    if(filter.test(v1, v2)) // Check that the candidate users follow the properties we desire
                    {
                        list.add(new Tuple3<>(v1,v2,v3));
                    }
                }
            }
            catch(IOException ioe)
            {
                System.err.println("Problem reading " + recIn + rec);
            }
            catch(NullPointerException npe)
            {
                System.err.println("Problem while reading line " + k);
            }
            
            Collections.sort(list, (x,y) -> 
            {
                if(x.v3 < y.v3) return 1;
                else if(x.v3 > y.v3) return -1;
                else return 0;
            });
            
            // Write the recommendation
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recOut + rec))))
            {
                for(Tuple3<Long,Long,Double> val : list)
                {
                    bw.write(val.v1 + "\t" + val.v2 + "\t" + val.v3 + "\n");
                }
            }
            catch(IOException ioe)
            {
                System.err.println("Problem writing " + recOut + rec);
            }
            Long b = System.currentTimeMillis();
            System.out.println("Finished " + rec + " (" + (b-a) + " ms.)");
        }
        
    }
}
