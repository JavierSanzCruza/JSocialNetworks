/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.other;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.openide.util.Exceptions;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given two graphs, stores a new graph only containing the nodes in the one of them.
 * @author Javier Sanz-Cruzado Puig
 */
public class GraphCleaner 
{
    /**
     * Given two graphs, stores a new graph only containing the nodes in the one of them.
     * @param args Execution arguments:
     * <ol>
     * <li>The graph which contains the remaining nodes</li>
     * <li>Path to the graph to clean.</li>
     * <li>Path to the cleaned graph.</li>
     * </ol>
     */
    public static void main(String[] args)
    {
        TextGraphReader<String> greader = new TextGraphReader<>(false, true, true, false, "\t", Parsers.sp);
        Graph<String> g = greader.read(args[0], true, false);
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]))))
        {
            br.lines().forEach(line -> 
            {
                String[] split = line.split("\t");
                String A = split[0];
                String B = split[1];
                
                if(g.containsVertex(A) && g.containsVertex(B))
                    try {
                        bw.write(A+"\t"+B+"\t"+split[2]+"\n");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
