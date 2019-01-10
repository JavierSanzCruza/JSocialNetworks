/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.util.ArrayList;
import org.openide.util.Exceptions;
/**
 *
 * @author Javier
 */
public class ReciprocalCleaningExample 
{
    public static void main(String[] args) throws Exception
    {
        if(args.length < 3)
        {
            System.err.println("Usage: <trainfile> <testfile> <outputtest>");
            return;
        }
        
        Map<String, List<String>> train = new HashMap<>();
        Map<String, List<String>> test = new HashMap<>();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))))
        {
            br.lines().forEach(line->
            {
               String[] split = line.split("\t");
               if(!train.containsKey(split[0]))
                  train.put(split[0], new ArrayList<>());
               train.get(split[0]).add(split[1]);
            });
        }
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]))))
        {
            br.lines().forEach(line ->{
                String[] split = line.split("\t");
                if(!train.containsKey(split[1]) || !train.get(split[1]).contains(split[0]))
                {
                    try {
                        bw.write(line + "\n");
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                    
                    
            });
        }
    }
}
