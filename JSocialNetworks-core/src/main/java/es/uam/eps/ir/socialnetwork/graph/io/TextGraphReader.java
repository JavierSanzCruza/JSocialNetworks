/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.io;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyMultiGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.index.Index;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.ranksys.formats.parsing.Parser;
import org.ranksys.formats.parsing.Parsers;

/**
 * Reads a graph from a file.
 * 
 * The read format is the following:
 * 
 * userA userB (weight) (types)
 * 
 * where the weight must appear only if the graph is weighted. In other case, this
 * column will be ignored. The type must only appear if it is going to be read. In
 * that case, the fourth column will be read. In other, it will be ignored. A weight
 * column must exist in case the types appear, but it can be empty.
 * Every column appart from that ones will be ignored at loading
 * the graph. Columns are separated by a certain delimiter. By defect, this delimiter
 * is a tab space.
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> The type of the user nodes.
 */
public class TextGraphReader<U> implements GraphReader<U>
{
    /**
     * Indicates if the graph to read is a multigraph (true) or not (false)
     */
    private final boolean multigraph;
    /**
     * Indicates if the graph to read is directed (true) or not (false)
     */
    private final boolean directed;
    /**
     * Indicates if the graph to read is weighted (true) or not (false)
     */
    private final boolean weighted;
    /**
     * Indicates if the graph to read allows autoloops (true) or not (false)
     */    
    private final boolean selfloops;
    /**
     * Parser for reading the users
     */
    private final Parser<U> uParser;
    /**
     * File delimiter
     */
    private final String delimiter;
    
    /**
     * Constructor
     * @param multigraph Indicates if the graph to read is a multigraph (true) or not (false)
     * @param directed Indicates if the graph to read is directed (true) or not (false)
     * @param weighted Indicates if the graph to read is weighted (true) or not (false)
     * @param selfloops Indicates if the graph to read allows autoloops (true) or not (false)
     * @param delimiter File delimiter
     * @param uParser Parser for reading the users
     */
    public TextGraphReader(boolean multigraph, boolean directed, boolean weighted, boolean selfloops, String delimiter, Parser<U> uParser)
    {
        this.multigraph = multigraph;
        this.directed = directed;
        this.weighted = weighted;
        this.selfloops = selfloops;
        this.delimiter = delimiter;
        this.uParser = uParser;
    }

    @Override
    public Graph<U> read(String file)
    {
        try
        {
            InputStream ios = new FileInputStream(file);
            return this.read(ios, true, false);
        }
        catch(IOException ioe)
        {
            return null;
        }
    }

    @Override
    public Graph<U> read(String file, boolean readWeights, boolean readTypes)
    {
        try
        {
            InputStream ios = new FileInputStream(file);
            return this.read(ios, readWeights, readTypes);
        }
        catch(IOException ioe)
        {
            return null;
        }    
    }

    @Override
    public Graph<U> read(InputStream stream)
    {
        return this.read(stream, true, false);
    }

    @Override
    public Graph<U> read(InputStream stream, boolean readWeights, boolean readTypes)
    {
        try{
            GraphGenerator<U> gg = multigraph ? new EmptyMultiGraphGenerator<>() : new EmptyGraphGenerator<>();
            if(multigraph)
            {
                EmptyMultiGraphGenerator<U> empty = (EmptyMultiGraphGenerator<U>) gg;
                empty.configure(directed, weighted);
            }
            else
            {
                EmptyGraphGenerator<U> empty = (EmptyGraphGenerator<U>) gg;
                empty.configure(directed, weighted);
            }
            Graph<U> graph = gg.generate();
            
            try(BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
            {
                br.lines().forEach(line -> {
                    String[] splits = line.split(delimiter);
                    U origin = uParser.parse(splits[0]);
                    U destiny = uParser.parse(splits[1]);
                    
                    if(!origin.equals(destiny) || selfloops)
                    {
                        double weight = 1.0;
                        int type = 0;
                        if(weighted)
                        {
                            weight = Parsers.dp.parse(splits[2]);
                        }
                        
                        if(readTypes && weighted)
                        {
                            type = Parsers.ip.parse(splits[3]);
                        }
                        else if(readTypes)
                        {
                            type = Parsers.ip.parse(splits[2]);
                        }
                        
                        graph.addEdge(origin, destiny, weight, type, true);
                    }
                });
            }
            catch(IOException ioe)
            {
                return null;
            }
            
            return graph;
        }
        catch(GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }

    @Override
    public Graph<U> read(String file, boolean readWeights, boolean readTypes, Index<U> users)
    {
        try
        {
            InputStream ios = new FileInputStream(file);
            return this.read(ios, readWeights, readTypes, users);
        }
        catch(IOException ioe)
        {
            return null;
        }    
    }

    @Override
    public Graph<U> read(InputStream stream, boolean readWeights, boolean readTypes, Index<U> users)
    {
        try
        {
            GraphGenerator<U> gg = multigraph ? new EmptyMultiGraphGenerator<>() : new EmptyGraphGenerator<>();
            if(multigraph)
            {
                EmptyMultiGraphGenerator<U> empty = (EmptyMultiGraphGenerator<U>) gg;
                empty.configure(directed, weighted);
            }
            else
            {
                EmptyGraphGenerator<U> empty = (EmptyGraphGenerator<U>) gg;
                empty.configure(directed, weighted);
            }
            Graph<U> graph = gg.generate();
            
            users.getAllObjectsIds().sorted().forEach(i -> graph.addNode(users.idx2object(i)));
            
            try(BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
            {
                br.lines().forEach(line -> {
                    String[] splits = line.split(delimiter);
                    U origin = uParser.parse(splits[0]);
                    U destiny = uParser.parse(splits[1]);
                    
                    if(!origin.equals(destiny) || selfloops)
                    {
                        double weight = 1.0;
                        int type = 0;
                        if(weighted)
                        {
                            weight = Parsers.dp.parse(splits[2]);
                        }
                        
                        if(readTypes && weighted)
                        {
                            type = Parsers.ip.parse(splits[3]);
                        }
                        else if(readTypes)
                        {
                            type = Parsers.ip.parse(splits[2]);
                        }
                        
                        graph.addEdge(origin, destiny, weight, type, false);
                    }
                });
            }
            catch(IOException ioe)
            {
                return null;
            }
            
            return graph;
        }
        catch(GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }



    
    
}
