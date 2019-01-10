/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.content;

import es.uam.eps.ir.socialnetwork.content.parsing.ToLowerParser;
import es.uam.eps.ir.socialnetwork.content.parsing.TextParser;
import es.uam.eps.ir.socialnetwork.content.index.exceptions.WrongModeException;
import es.uam.eps.ir.socialnetwork.content.index.twittomender.LuceneTwittomenderIndex;
import es.uam.eps.ir.socialnetwork.content.index.twittomender.TwittomenderIndex;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ranksys.formats.parsing.Parsers;

/**
 * Class for generating Twittomender indexes.
 * @author Javier Sanz-Cruzado Puig
 */
public class SimpleTwittomenderIndexGenerator 
{
    public static void main(String[] args) throws IOException, WrongModeException
    {
        if(args.length < 3)
        {
            System.err.println("Invalid arguments");
            System.err.println("Usage: <trainGraph> <info> <indexRoute>");
            return;
        }
        
        String trainGraphRoute = args[0];
        String infoRoute = args[1];
        String indexRoute = args[2];
        
        TwittomenderIndex<Long> index = new LuceneTwittomenderIndex(indexRoute, false);
        index.setWriteMode();
        TextParser tparser = new ToLowerParser();
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, false, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(trainGraphRoute, true, false);
         
        Map<Long, String> docs = new HashMap<>();
        
        graph.getAllNodes().forEach(u -> docs.put(u, ""));
        
        File f = new File(infoRoute);
        CSVFormat format = CSVFormat.TDF.withHeader("tweetId","userId","text","retweetCount","favoriteCount","created","truncated");
        CSVParser parser = CSVParser.parse(f, Charset.forName("UTF-8"), format);
        
        boolean header = true;
        int j = 0; 
        long a = System.currentTimeMillis();
        System.out.println("Starting indexing");
        // Obtain the different data records.
        for(CSVRecord record : parser.getRecords())
        {
            if(j == 0 && header)
            {
                ++j;
                continue;
            }
            
            String userIdStr = record.get("userId");
            String tweet = record.get("text");
            
            long userId = new Long(userIdStr);
            docs.put(userId, docs.get(userId) + tweet);
            
        }
        long b = System.currentTimeMillis();
        System.out.println("Tweets read (" + (b-a) + " ms.)" );
        
        List<Long> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        j=0;
        for(long node : nodes)
        {
            if(j % 100 == 0)
           {
               b = System.currentTimeMillis();
               System.out.println("Users read : " + j + " (" + (b-a) + " ms.)");
           }
           String docText = docs.get(node);
                      
           index.writeContent(node, docText, tparser);
           ++j;
        }
        
        System.out.println("Index created");
        index.close();
        
        
    }
}
