/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation.utils;

import java.io.*;
/**
 * Generates an index for the information pieces.
 * @author Javier Sanz-Cruzado Puig
 */
public class InformationPiecesIndexGenerator 
{
    /**
     * Generates an index for the information pieces
     * @param args Execution arguments:
     * <ol>
     *  <li><b>Info. file:</b> Route to a file containing the information pieces data</li>
     *  <li><b>Output:</b> Route to the file where we want to store the index</li>
     * </ol>
     * @throws IOException if something fails while reading/writing.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <infoFile> <output>");
            return;
        }
        
        String infoFile = args[0];
        String outputFile = args[1];
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile))))
        {
            String line = br.readLine(); //Header line.
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                bw.write(split[0] + "\n");
            }
        }
    }
}
