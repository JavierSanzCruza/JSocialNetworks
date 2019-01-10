/* *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import java.io.*;
import org.openide.util.Exceptions;
/**
 * Creates a dataset for detecting the sign of networks.
 * @author Javier Sanz-Cruzado Puig
 */
public class LeskovecSignedNetworks 
{
    public static void main(String[] args)
    {
        if(args.length < 2)
            return;
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
            BufferedWriter bwTrain = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]+"-train.txt")));
            BufferedWriter bwTest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]+"-test.txt"))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                bwTrain.write(line+"\n");
                if(line.contains("-1"))
                    bwTest.write(line+"\n");
            }
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
