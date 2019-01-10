/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.reranking;

import java.io.*;
import java.util.*;

/**
 *
 * @author Javier
 */
public class RewiredLinks {
    public static void main(String[] args)
    {
        if(args.length < 2)
            return;
        
        HashMap<String, HashSet<String>> original = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))))
        {
            String line="";
            while((line = br.readLine() )!= null)
            {
                String[] split = line.split("\t");
                if(original.containsKey(split[0]))
                {
                    original.get(split[0]).add(split[2]);
                }
                else
                {
                    HashSet<String> out = new HashSet<>();
                    out.add(split[2]);
                    original.put(split[0], out);
                }
            }
        }
        catch(IOException ioe)
        {
            
        }
        
        File f = new File(args[1]);
        for(File g : f.listFiles())
        {
            System.out.println(g.getName());
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(g.getAbsoluteFile()))))
            {
                String line="";
                Long l = 0L;
                while((line = br.readLine() )!= null)
                {
                    String[] split = line.split("\t");
                    if(!original.get(split[0]).contains(split[2]))
                            l++;
                }
                System.out.println(l);
            }
            catch(IOException ioe)
            {
                
            }
        }
        
    }
    
}
