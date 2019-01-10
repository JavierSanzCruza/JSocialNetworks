/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.content;

import es.uam.eps.ir.socialnetwork.content.Content;
import es.uam.eps.ir.socialnetwork.content.index.ContentIndex;
import es.uam.eps.ir.socialnetwork.content.index.LuceneContentIndex;
import es.uam.eps.ir.socialnetwork.content.parsing.ToLowerParser;
import es.uam.eps.ir.socialnetwork.content.parsing.TextParser;
import es.uam.eps.ir.socialnetwork.content.index.exceptions.WrongModeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Generates a content index from a csv file
 * @author Javier Sanz-Cruzado Puig
 */
public class ContentIndexGenerator 
{
    public static void main(String[] args) throws IOException, WrongModeException, ParseException
    {
        if(args.length < 3)
        {
            System.err.println("Usage: <file> <indexRoute> <created>");
        }
        
        String file = args[0];
        String idxRoute = args[1];
        boolean created = args[2].equalsIgnoreCase("true");
        
        ContentIndex<Long,Long> index = new LuceneContentIndex(idxRoute, created);
        index.setWriteMode();
        TextParser tparser = new ToLowerParser();
        
        File f = new File(file);
        CSVFormat format = CSVFormat.TDF.withHeader("tweetId","userId","text","retweetCount","favoriteCount","created","truncated");
        CSVParser parser = CSVParser.parse(f, Charset.forName("UTF-8"), format);
        boolean header = true;
        int j = 0;
        for(CSVRecord record : parser.getRecords())
        {
           try
           {
           if(j == 0)
           {
               ++j;
               continue;
           }
           String userIdStr = record.get("userId");
           String tweetIdStr = record.get("tweetId");
           String tweet = record.get("text");
           String timestampStr =record.get("created");
           
           long userId = new Long(userIdStr);
           long tweetId = new Long(tweetIdStr);
           DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           Date res = df.parse(timestampStr);
           long timestamp = res.getTime();
           
           
           Content<Long, String> content = new Content<>(tweetId, tweet, timestamp);
           index.writeContent(userId, content, tparser);
           }
           catch(Exception ex)
           {
               System.err.println("ERROR");
           }
        }
        index.close();
        System.out.println("Index created");
        
    }
}
