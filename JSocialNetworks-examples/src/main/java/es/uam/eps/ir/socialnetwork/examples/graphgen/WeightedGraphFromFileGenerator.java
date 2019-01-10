/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.graphgen;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Javier Sanz-Cruzado Puig
 */
public class WeightedGraphFromFileGenerator 
{
    public static void main(String args[]) throws Exception
    {
        if(args.length < 2)
        {
            System.out.println("Usage: <File> <Destination>");
        }
        
        Graph<String> graph;
        EmptyGraphGenerator<String> graphGenerator = new EmptyGraphGenerator<>();
        graphGenerator.configure(true, true);
        graph = graphGenerator.generate();
        
        Object2IntMap<Pair<String>> map = new Object2IntOpenHashMap<>();
        map.defaultReturnValue(0);
        
        Object2IntMap<String> mapUser = new Object2IntOpenHashMap<>();
        map.defaultReturnValue(0);

        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))))
        {
            br.lines().forEach(line->
            {
                String[] split = line.split("\t");
                Pair<String> pair = new Pair<>(split[0], split[1]);
                
                if(!split[0].equals(split[1]))
                {
                    if(map.containsKey(pair))
                    {
                        map.put(pair, map.get(pair)+1);

                    }
                    else
                    {
                        map.put(pair, 1);
                    }

                    if(mapUser.containsKey(split[0]))
                    {

                        mapUser.put(split[0], mapUser.get(split[0]) + 1);
                    }
                    else
                    {
                        mapUser.put(split[0], 1);
                    }
                }
            });
        }
        
        map.entrySet().stream().forEach((entry) -> 
        {
           Pair<String> pair = entry.getKey();
           String user = pair.v1();
           String followed = pair.v2();
           
           graph.addNode(user);
           graph.addNode(followed);
           
           graph.addEdge(user, followed, (entry.getValue() + 0.0)/(mapUser.get(user) + 0.0));
        });
        
        TextGraphWriter gwriter = new TextGraphWriter("\t");
        if(gwriter.write(graph, args[1], true, false) == false)
        {
            System.err.println("ERROR: Failure while writing in " + args[1]);
        }        
    }
}
