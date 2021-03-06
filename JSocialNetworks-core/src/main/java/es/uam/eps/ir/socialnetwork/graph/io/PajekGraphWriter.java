/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.io;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes the graph in the Pajek format. Note: this format does support
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users
 */
public class PajekGraphWriter<U extends Serializable> implements GraphWriter<U>
{

    @Override
    public boolean write(Graph<U> graph, String file)
    {
        try
        {
            return this.write(graph, new FileOutputStream(file));
        } 
        catch (FileNotFoundException ex)
        {
            return false;
        }
        
    }

    @Override
    public boolean write(Graph<U> graph, OutputStream file)
    {
        return this.write(graph, file, true, false);
    }

    @Override
    public boolean write(Graph<U> graph, String file, boolean writeWeights, boolean writeTypes)
    {
        try
        {
            return this.write(graph, new FileOutputStream(file));
        } 
        catch (FileNotFoundException ex)
        {
            return false;
        }
    }

    @Override
    public boolean write(Graph<U> graph, OutputStream file, boolean writeWeights, boolean writeTypes)
    {
        if(writeTypes)
        {
            throw new UnsupportedOperationException("ERROR: Pajek format does not support writing types");
        }
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(file)))
        {
            
            bw.write("*Vertices "+graph.getVertexCount() + "\n");
            for(int l = 0; l < graph.getVertexCount(); ++l)
            {
                bw.write((l+1) + " \"" + graph.idx2object(l) +  "\"\n");
            }
            
            bw.write("*Edges " + graph.getEdgeCount());
            graph.getAllNodes().forEach(u -> 
            {
                int uidx = graph.object2idx(u);
                graph.getAdjacentNodesWeights(u).forEach(v -> 
                {
                    int vidx = graph.object2idx(v.getIdx());
                    try
                    {
                        if(writeWeights)
                        {
                            bw.write("\n" + (uidx+1) + " " + (vidx + 1) + " " + v.getValue());
                        }
                        else
                        {
                            bw.write("\n" + (uidx+1) + " " + (vidx + 1));
                        }
                    } 
                    catch (IOException ex)
                    {
                        Logger.getLogger(PajekGraphWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            });

            return true;
        }
        catch(IOException ioe)
        {
            return false;
        }
    }
    
}
