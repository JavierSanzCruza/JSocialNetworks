/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.openide.util.Exceptions;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.index.fast.SimpleFastUserIndex;
import org.ranksys.formats.index.UsersReader;
import static org.ranksys.formats.parsing.Parsers.sp;

/**
 * Given a training and a test graphs, and a user index, changes the original
 * string users into numeric ones.
 * @author Javier Sanz-Cruzado Puig
 */
public class GraphToInteger 
{
    /**
     * Method that changes the original string users into numeric ones
     * @param args Execution arguments <ol>
     * <li><b>Index:</b> Index route</li>
     * <li><b>Training graph:</b> Route to the training graph</li>
     * <li><b>Test graph:</b> Route to the test graph</li>
     * <li><b>Output:</b> Output route for the files</li>
     * </ol>
     * @throws java.io.IOException if something fails while reading or writing files
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 4)
        {
            System.err.println("Usage: <index> <train> <test> <output>");
            return;
        }
        
        String indexRoute = args[0];
        String trainGraph = args[1];
        String testGraph = args[2];
        String outputRoute = args[3];
        
        FastUserIndex<String> userIndex = SimpleFastUserIndex.load(UsersReader.read(indexRoute, sp));
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trainGraph)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute + "-train"))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String split[] = line.split("\t");
                int u = userIndex.user2uidx(split[0]);
                int v = userIndex.user2uidx(split[1]);
                bw.write(u + "\t" + v);
                for(int i = 2; i < split.length; ++i)
                {
                    bw.write("\t" + split[i]);
                }
                bw.write("\n");
            }
        }
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testGraph)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute + "-test"))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String split[] = line.split("\t");
                int u = userIndex.user2uidx(split[0]);
                int v = userIndex.user2uidx(split[1]);
                bw.write(u + "\t" + v);
                for(int i = 2; i < split.length; ++i)
                {
                    bw.write("\t" + split[i]);
                }
                bw.write("\n");
            }
        }
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute + "-index"))))
        {
            userIndex.getAllUidx().forEach(uidx -> 
            {
                try 
                {
                    bw.write(uidx + "\n");
                }
                catch (IOException ex) 
                {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }
}
