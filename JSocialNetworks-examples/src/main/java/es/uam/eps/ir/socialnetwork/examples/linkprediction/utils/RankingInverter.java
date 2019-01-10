/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.utils;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given some link prediction rankings, prints the inverse rankings
 * @author Javier Sanz-Cruzado Puig
 */
public class RankingInverter 
{
    /**
     * Inverts a link prediction ranking
     * @param args Execution arguments:
     * <ul>
     *  <li><b>Rec. Directory</b> Folder which contains the rankings to invert</li>
     *  <li><b>Output Directory</b> Folder in which to store the inverted rankings</li>
     * </ul>
     */
    public static void main(String[] args) 
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <recDir> <outputDir>");
            return;
        }
        
        String recDir = args[0];
        String outputDir = args[1];
        
        File file = new File(recDir);
        String[] recommenders;
        
        if(file.exists() && file.isDirectory())
        {
            recommenders = file.list();
        }
        else if(file.exists())
        {
            recommenders = new String[]{recDir};
        }
        else
        {
            System.err.println("Invalid route");
            return;
        }    
        
        if(recommenders == null)
        {
            System.err.println("Nothing to invert");
            return;
        }
            
        for(String recomm : recommenders)
        {
            List<Tuple2od<Pair<Long>>> invRanking = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recDir + recomm)));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir + recomm + "-not.txt"))))
            {
                String line;
                while((line = br.readLine()) != null)
                {
                    String[] split = line.split("\t");
                    long u = Parsers.lp.parse(split[0]);
                    long v = Parsers.lp.parse(split[1]);
                    double value = Parsers.dp.parse(split[2]);
                    Tuple2od<Pair<Long>> tuple = new Tuple2od<>(new Pair<>(u,v),value);
                    invRanking.add(0,tuple);
                }

                for(Tuple2od<Pair<Long>> tuple : invRanking)
                {
                    bw.write(tuple.v1.v1() + "\t" + tuple.v1.v2() + "\t" + tuple.v2() + "\n");
                }

                System.out.println("Finished processing file: " + recDir + recomm);
            }
            catch(IOException ioe)
            {
                System.err.println("Error processing file: " + recDir + recomm);
            }
        }    
    }
}
