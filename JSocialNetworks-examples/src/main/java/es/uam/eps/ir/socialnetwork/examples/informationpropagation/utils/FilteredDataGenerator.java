/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation.utils;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.io.DataReader;
import java.io.IOException;
import org.ranksys.formats.parsing.Parsers;

/**
 *
 * @author Javier
 */
public class FilteredDataGenerator 
{
    public static void main(String[] args) throws IOException
    {
        if(args.length < 13)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\tuIndexPath: Path to the a file containing the list of users");
            System.err.println("\tiIndexPath: Path to a file containing a list of information pieces identifiers");
            System.err.println("\tgraphFile: Path to a file containing a graph");
            System.err.println("\tmultigraph: Indicates if the graph is (true) or not (false) a multigraph");
            System.err.println("\tdirected: Indicates if the graph is directed (true) or not (false)");
            System.err.println("\tweighted: Indicates if the graph is weighted (true) or not (false)");
            System.err.println("\treadTypes: Indicates if the graph types have to be read from file (true) or not (false)");
            System.err.println("\tinfoFile: File containing the relation between users and info. pieces");
            System.err.println("\tuserFeatureFiles: Comma-separated, files containing the features for the nodes in the graph");
            System.err.println("\tinfoPiecesFeatureFiles: Comma-separated, files containing the features for the information pieces");
            System.err.println("\trealPropagatedInfoFile: A file containing a relation between users and propagated information");
            System.err.println("\tconfiguration: XML file containing the configuration for the filters");
            System.err.println("\toutput: Directory for storing the binary data");
            return;
        }
        
        String uIndexPath = args[0];
        String iIndexPath = args[1];
        String graphFile = args[2];
        boolean multigraph = args[3].equalsIgnoreCase("true");
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("true");
        boolean readTypes = args[6].equalsIgnoreCase("true");
        String infoFile = args[7];
        String[] userParamFiles = args[8].split(",");
        String[] infoPiecesParamFiles = args[9].split(",");
        String realPropInfo = args[10];
        String configuration = args[11];
        String output = args[12];
        
        long timea = System.currentTimeMillis();
        DataReader<Long, Long, Long> datareader = new DataReader<>();
        Data<Long, Long, Long> data = datareader.readData(multigraph, directed, weighted, readTypes, uIndexPath, iIndexPath, graphFile, infoFile, userParamFiles, infoPiecesParamFiles, realPropInfo, Parsers.lp, Parsers.lp, Parsers.lp);
        long timeb = System.currentTimeMillis();
        System.out.println("Data read: " + (timeb - timea) + " ms.");
         //PREPARAR FILTROS.
       /* FilterParamReader filterReader = new FilterParamReader()
        FilterSelector<Long, Long, Long> filterSel = new FilterSelector<>(Parsers.lp);
        DataFilter<Long, Long, Long> dataFilter = 
        
        
                BinaryDataWriter.write(data, output);*/
        
        
    }
}
