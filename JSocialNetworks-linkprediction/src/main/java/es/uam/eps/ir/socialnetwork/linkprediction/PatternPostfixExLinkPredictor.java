/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.linkprediction;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.recommendation.ml.GraphPatternSet;
import es.uam.eps.ir.socialnetwork.recommendation.ml.Pattern;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Predicts the presence or absence of links using some of the patterns of a GraphPatternSet 
 * (a pattern set where the edges which represent the patterns are identified).
 * 
 * It is possible to indicate an operation of some of those patterns using a postfix expression.
 * 
 * Format of the postfix expression:
 * - If two numbers are next to each other, they must be separated using a dot (".").
 * - Operations allowed: +,-,*,/
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of users
 */
public class PatternPostfixExLinkPredictor<U> extends AbstractLinkPredictor<U>
{

    /**
     * List which contains the results of the algorithm link prediction.
     */
    private final List<Tuple2od<Pair<U>>> results;
    
    /**
     * Constructor.
     * @param graph The graph.
     * @param patterns The set of patterns.
     * @param postfix The postfix notation expression.
     * @param comparator Comparator for ordering the nodes.
     * @throws UnsupportedOperationException if the postfix expression is badly created.
     */
    public PatternPostfixExLinkPredictor(Graph<U> graph, Comparator<Tuple2od<Pair<U>>> comparator, GraphPatternSet<U> patterns, String postfix) 
    {
        super(graph, comparator);
        
        TreeSet<Tuple2od<Pair<U>>> ordered = new TreeSet<>(this.getComparator());
        
        if(Character.isDigit(postfix.charAt(postfix.length()-1))) // a lonely column
        {
            Integer attrId = new Integer(postfix);
            patterns.getAllPatterns().forEach(pattern -> {
               Tuple2od<Pair<U>> value = new Tuple2od<>(pattern.v1(), pattern.v2().getValue(attrId));
               ordered.add(value);
            });
        }
        else // a regular expression in postfix format.
        {
            Stack<Double> stack = new Stack<>();
            TreeSet<Integer> points = new TreeSet<>();

            
            // Store which points contain a difference between numbers and characters
            int lastIndex = 0;
            while(lastIndex != -1) // points represent a separation between two numbers
            {
                lastIndex = postfix.indexOf(".", lastIndex+1);
                if(lastIndex != -1)
                {
                    points.add(lastIndex);
                    points.add(lastIndex+1);
                }
            }

            lastIndex = 0; // sums
            while(lastIndex != -1)
            {
                lastIndex = postfix.indexOf("+",lastIndex+1);
                if(lastIndex != -1)
                {
                    points.add(lastIndex);
                    points.add(lastIndex+1);
                }
            }

            lastIndex = 0; // substractions
            while(lastIndex != -1)
            {
                lastIndex = postfix.indexOf("-",lastIndex+1);
                if(lastIndex != -1)
                {
                    points.add(lastIndex);
                    points.add(lastIndex+1);
                }
            }

            lastIndex = 0;
            while(lastIndex != -1) // divisions
            {
                lastIndex = postfix.indexOf("/",lastIndex+1);
                if(lastIndex != -1)
                {
                    points.add(lastIndex);
                    points.add(lastIndex+1);
                }
            }

            lastIndex = 0; 
            while(lastIndex != -1) // products
            {
                lastIndex = postfix.indexOf("*",lastIndex+1);
                if(lastIndex != -1)
                {
                    points.add(lastIndex);
                    points.add(lastIndex+1);
                }
            }

            // Store the different elements of the expression
            int currentIndex = 0;
            List<String> expression = new ArrayList<>();
            Iterator<Integer> iterator = points.iterator();
            while(iterator.hasNext())
            {
                Integer next = iterator.next();
                String substring = postfix.substring(currentIndex, next);

                if(!substring.equals("."))
                    expression.add(substring);
                currentIndex = next;
            }

            // For each identified pattern, process the postfix expression
            patterns.getAllPatterns().forEach(pattern ->
            {
                Pair<U> pair = pattern.v1();
                Pattern p = pattern.v2();

                
                for(String term : expression)
                {
                    double v1;
                    double v2;
                    switch (term) 
                    {
                        case "+": // sum
                            if(stack.size() < 2)
                                throw new UnsupportedOperationException("Invalid regexp: not enough operands");
                            v2 = stack.pop();
                            v1 = stack.pop();
                            stack.push(v1 + v2);
                            break;
                        case "-": // substract
                            if(stack.size() < 2)
                                throw new UnsupportedOperationException("Invalid regexp: not enough operands");
                            v2 = stack.pop();
                            v1 = stack.pop();
                            stack.push(v1 - v2);
                            break;
                        case "*": // product
                            if(stack.size() < 2)
                                throw new UnsupportedOperationException("Invalid regexp: not enough operands");
                            v2 = stack.pop();
                            v1 = stack.pop();
                            stack.push(v1 * v2);
                            break;
                        case "/": // division
                            if(stack.size() < 2)
                                throw new UnsupportedOperationException("Invalid regexp: not enough operands");
                            v2 = stack.pop();
                            v1 = stack.pop();
                            stack.push(v1 / v2);
                            break;
                        default: // store the value of the selected attribute
                            Integer attrId = new Integer(term);
                            v1 = p.getValue(attrId);
                            stack.push(v1);
                    }
                }
                
                if(stack.size() > 1)
                    throw new UnsupportedOperationException("Invalid regexp");
                ordered.add(new Tuple2od<>(pair, stack.pop()));
            });
        }
        
        this.results = new ArrayList<>(ordered);
    }

    @Override
    public List<Tuple2od<Pair<U>>> getPrediction(int maxLength, Predicate<Pair<U>> filter) 
    {
        return this.results.stream().filter(tuple -> filter.test(tuple.v1)).limit(maxLength).collect(Collectors.toList());
    }
}
