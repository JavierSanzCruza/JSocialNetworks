/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.hashtags;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ranksys.formats.parsing.Parsers;

/**
 * Analyzes the hashtags of a given dataset
 * @author Javier Sanz-Cruzado Puig
 */
public class HashtagByComms
{
    /**
     * Analyzes the hashtags of a given dataset.
     * @param args Execution arguments: <ol>
     * <li>File containing a tag list (id/tag), comma separated</li>
     * <li>File containing a relation between users and information pieces ids, tag separated</li>
     * <li>File containing a relation between information pieces ids and tag ids, tag separated</li>
     * <li>Communities file</li>
     * <li>Output directory where to store the analysis</li>
     * </ol>
     * @throws java.io.IOException if something fails while reading or writing files
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Invalid arguments:");
            System.err.println("1: File containing a tag list (id/tag)");
            System.err.println("2: Tweets / Users");
            System.err.println("3: Tweets / Hashtags Id");
            System.err.println("4: Communities file");
            System.err.println("5: Output directory");
            return;
        }
        
        String tagFile = args[0];
        String infoFile = args[1];
        String tagInfoFile = args[2];
        String commFile = args[3];
        String output = args[4];
        
        
        
        CommunitiesReader<Long> commReader = new CommunitiesReader<>();
        Communities<Long> comm = commReader.read(commFile, "\t", Parsers.lp);
        
        /*
         * Reads the different tags (relation between an identifier and a string) 
         */
        Map<Long, String> tags = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile))))
        {
            String line = br.readLine(); // Read the header
            while((line = br.readLine()) != null)
            {
                String[] split = line.split(",");
                Long tagId = Parsers.lp.parse(split[0]);
                tags.put(tagId, split[1]);
            }
        }
        
        /**
         * Reads the relation between tweets and users.
         */
        Map<Long, Long> userInfo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile))))
        {
            String line = br.readLine();
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                Long infoId = Parsers.lp.parse(split[0]);
                Long userId = Parsers.lp.parse(split[1]);
                
                userInfo.put(infoId, userId);
            }
        }
        
        /**
         * Reads the relation between tags and tweets.
         */
        Map<Long, Set<Long>> tagInfo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagInfoFile))))
        {
            String line = br.readLine();
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                Long infoId = Parsers.lp.parse(split[0]);
                Long tagId = Parsers.lp.parse(split[1]);
                
                if(userInfo.containsKey(infoId))
                {
                    if(!tagInfo.containsKey(tagId))
                        tagInfo.put(tagId, new HashSet<>());

                    tagInfo.get(tagId).add(infoId);
                }
            }
        }
        
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("tag\ttotal");
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                bw.write("\tcomm" + i);
            }
            
            bw.write("\n");
            
            for(long tag : tagInfo.keySet())
            {
                bw.write(tags.get(tag));
                
                
                
                Int2IntMap map = new Int2IntOpenHashMap();
                for(int i = 0; i < comm.getNumCommunities(); ++i)
                {
                    map.put(i, 0);
                }
                map.defaultReturnValue(0);
                
                for(long infoId : tagInfo.get(tag))
                {
                    long userId = userInfo.get(infoId);
                    map.put(comm.getCommunity(userId), map.get(comm.getCommunity(userId)) + 1);
                }
                
                double sum = 0;
                for(int i = 0; i < comm.getNumCommunities(); ++i)
                {
                    sum += map.get(i);
                }
                
                bw.write("\t" + sum);
                for(int i = 0; i < comm.getNumCommunities(); ++i)
                {
                    bw.write("\t" + map.get(i));
                }
                bw.write("\n");
            }  
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR");
        }
    }
}
