/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.community.io;

import es.uam.eps.ir.socialnetwork.community.Communities;
import org.ranksys.formats.parsing.Parser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ranksys.formats.parsing.Parsers;

/**
 * Reads a community file
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class CommunitiesReader<U> 
{
    /**
     * Reads the communities of a graph from a file.
     * @param file The file.
     * @param delimiter The delimiter that separates different values to retrieve.
     * @param parser Vertex parser.
     * @return The community relation.
     */
    public Communities<U> read(String file, String delimiter, Parser<U> parser)
    {
        Map<Integer, Integer> visitedComms = new HashMap<>();
        Communities<U> communities = new Communities<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            br.lines().forEach((line)->{
                String[] split = line.split(delimiter);
                U user = parser.parse(split[0]);
                Integer comm = Parsers.ip.parse(split[1]);
                
                if(!visitedComms.containsKey(comm))
                {
                    visitedComms.put(comm, visitedComms.size());
                    communities.addCommunity();
                }
                
                communities.add(user, visitedComms.get(comm));
            });
            
            return communities;
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: An error ocurred while reading the file");
            return null;
        }
    }
}
