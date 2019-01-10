/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given a link prediction complete ranking, formats it for recommendation.
 * @author Javier Sanz-Cruzado Puig
 */
public class RankingToRecommendationRanking 
{
    /**
     * Program that translates link prediction rankings to recommendation files.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Input folder:</b> Path of the link prediction ranking files</li>
     *  <li><b>Output folder:</b> Path of the directory which will store the recommendation files</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <input folder> <output folder> <topN>");
            return;
        }
        
        String recIn = args[0];
        String recOut = args[1];
        int topN = Parsers.ip.parse(args[2]);
        
        
        File f = new File(recIn);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to evaluate");
        }
        
        // Read and store the ranking into individual recommendations
        for(String rec : recommenders)
        {
            Long a = System.currentTimeMillis();
            Map<Long, List<Tuple2od<Long>>> pair = new HashMap<>();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recIn + rec))))
            {
                String line;
                while((line = br.readLine()) != null)
                {
                    String[] split = line.split("\t");
                    long v1 = Parsers.lp.parse(split[0]);
                    long v2 = Parsers.lp.parse(split[1]);
                    double v3 = Parsers.dp.parse(split[2]);
                    
                    if(!pair.containsKey(v1))
                    {
                       pair.put(v1, new ArrayList<>());
                    }
                    pair.get(v1).add(new Tuple2od<>(v2,v3));
                }
            }
            catch(IOException ioe)
            {
                System.err.println("Problem reading " + recIn + rec);
            }
            
            // Write the recommendation
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recOut + rec))))
            {
                for(Entry<Long, List<Tuple2od<Long>>> entry : pair.entrySet())
                {
                    List<Tuple2od<Long>> list = entry.getValue();
                    for(int i = 0; i < list.size() && i < topN; ++i)
                    {
                        Tuple2od<Long> value = list.get(i);
                        bw.write("" + entry.getKey() + "\tQ0\t" + value.v1 + "\t" +  (entry.getValue().indexOf(value)+1) + "\t" + value.v2 + "\tr\n");
                    }
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
