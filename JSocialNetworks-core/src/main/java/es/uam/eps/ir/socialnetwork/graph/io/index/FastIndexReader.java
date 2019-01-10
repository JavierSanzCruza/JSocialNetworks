/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.graph.io.index;

import es.uam.eps.ir.socialnetwork.index.Index;
import es.uam.eps.ir.socialnetwork.index.fast.FastIndex;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.ranksys.formats.parsing.Parser;

/**
 *
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 */
public class FastIndexReader<U> implements IndexReader<U>
{
    /**
     * A parser.
     */
    private final Parser<U> parser;

    /**
     * Constructor.
     * @param parser a parser, for reading the elements in the index. 
     */
    public FastIndexReader(Parser<U> parser)
    {
        this.parser = parser;
    }
    @Override
    public Index<U> read(String file)
    {
        try
        {
            InputStream stream = new FileInputStream(file);
            return this.read(stream);
        }
        catch(IOException ioe)
        {
            return null;
        }//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Index<U> read(InputStream stream)
    {
        Index<U> index = new FastIndex<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                index.addObject(parser.parse(line));
            }
            return index;
        }
        catch(IOException ioe)
        {
            return null;
        }
    }

}
