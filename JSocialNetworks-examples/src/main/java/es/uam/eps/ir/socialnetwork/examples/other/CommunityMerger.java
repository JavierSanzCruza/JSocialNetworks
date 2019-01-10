/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.other;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given several community partitions, this class merges them all in a single feature document.
 * @author Javier Sanz-Cruzado Puig
 */
public class CommunityMerger 
{
    /**
     * Constructor.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Output file:</b> File in which to store the output</li>
     *  <li><b>Comm. folder:</b> Folder which contains the communities files</li>
     *  <li><b>Comm. file 1:</b> Name of the first community file to combine</li>
     *  <li><b>Comm. file 2:</b> Name of the second community file to combine</li>
     *  <li>...</li>
     *  <li><b>Comm. file n:</b> Name of the n-th community file to combine</li>
     * </ul>
     * @throws java.io.IOException If something fails while writing the communities.
     */
    public static void main(String args[]) throws IOException
    {
        if(args.length < 3)
        {
            System.err.println("Invalid arguments");
            System.err.println("Usage:");
            System.err.println("\tOutput file: File in which to store the output");
            System.err.println("\tComm. folder:Folder which contains the communities files");
            System.err.println("\tComm. file 1: Name of the first community file to combine");
            System.err.println("\tComm. file 2: Name of the second community file to combine");
            System.err.println("\t...");
            System.err.println("\tComm. file n: Name of the n-th community file to combine");
            return;
        }
        
        String output = args[0];
        String commFolder = args[1];
        List<String> commFiles = new ArrayList<>();
        for(int i = 2; i < args.length; ++i)
        {
            commFiles.add(args[i]);
        }
        
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            int numComms = 0;
            for(String commFile : commFiles)
            {
                int currComms = numComms;
                Communities<Long> comms = creader.read(commFolder + commFile, "\t", Parsers.lp);
                if(comms != null)
                {
                    comms.getCommunities().forEach(c -> 
                    {
                        comms.getUsers(c).forEach(u -> 
                        {
                            try 
                            {
                                bw.write(u + "\t" + (c+currComms) + "\t" + 1.0 + "\n");
                            } 
                            catch (IOException ex) 
                            {
                                System.err.println("Something failed while writing");
                            }
                        });
                    });
                    numComms += comms.getNumCommunities();
                }
                else
                {
                    System.err.println("Error while reading communities file " + commFolder + commFile);
                }
            }
        }
    }
}
