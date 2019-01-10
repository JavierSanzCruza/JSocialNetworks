/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.TreeSet;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given the training and test graphs for a user recommendation problem, generates
 * the user/item index file.
 * @author Javier Sanz-Cruzado Puig
 */
public class IndexCalculator
{
    /**
     * Given the training and test graphs for a user recommendation problem, generates
     * the user/item index file.
     * @param args Execution arguments
     * <ul>
     *  <li><b>trainData:</b> The training data</li>
     *  <li><b>testData:</b> The test data</li>
     *  <li><b>outputPath:</b> The output path for the index</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <trainData> <testData> <outputFile>");
            return;
        }
        
        String trainData = args[0];
        String testData = args[1];
        String outputPath = args[2];
        Set<String> users = new TreeSet<>();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trainData))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                String u = Parsers.sp.parse(split[0]);
                String v = Parsers.sp.parse(split[1]);
                
               
                users.add(u);
                users.add(v);
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Error while reading the training file: " + trainData);
            return;
        }
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testData))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                String u = Parsers.sp.parse(split[0]);
                String v = Parsers.sp.parse(split[1]);
                
                users.add(u);
                users.add(v);
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Error while reading the training file: " + testData);
            return;
        }
                
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath))))
        {
            for(String user : users)
            {
                bw.write(user + "\n");
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Error while writing the index file: " + outputPath);
        }
    }
}
