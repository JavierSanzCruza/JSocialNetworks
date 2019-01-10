/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.utils;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimplePreferenceData;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.ranksys.core.preference.PreferenceData;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.RecommendationFormat.Writer;
import org.ranksys.formats.rec.TRECRecommendationFormat;

/**
 *
 * @author Javier
 */
public class OutputReducer 
{
    public static void main(String[] args) throws IOException
    {
        if(args.length < 4)
        {
            System.err.println("Usage: <userIndex> <test> <recFolder> <outputFolder> <directed>");
            return;
        }
        
        String userIndex = args[0];
        String test = args[1];
        String recFolder = args[2];
        String outputFolder = args[3];
        Boolean directed = args[4].equalsIgnoreCase("true");
        
        PreferenceData<Long,Long> testData = GraphSimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(test, lp,lp), directed, false);
        
        File f = new File(recFolder);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to reduce");
        }
        
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        
        Set<Long> users = testData.getUsersWithPreferences().collect(Collectors.toCollection(HashSet::new));
        for(String recomm : recommenders)
        {
            Writer<Long, Long> writer = format.getWriter(outputFolder + recomm);
            format.getReader(recFolder + recomm)
                .readAll()
                .filter(rec -> users.contains(rec.getUser()))
                .forEach(rec -> 
                {
                    try 
                    {
                        writer.write(rec);
                    } 
                    catch (IOException ex) 
                    {
                        System.err.println("ERROR WHILE WRITING RECOMMENDATION FOR USER " + rec.getUser());
                    }
                    
                });
            writer.close();
        }
        
    }
}
