package es.uam.eps.ir.socialnetwork.utils.indexes;

/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for the Gini index.
 * @author Javier
 */
public class GiniIndexTest {
    
    public GiniIndexTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void giniTest()
    {
        GiniIndex gini = new GiniIndex();
        List<Double> list1 = new ArrayList<>();
        for(int i = 0; i < 100; ++i)
        {
            list1.add(1.0);
        }
        
        // Check the perfectly equilibrated case.
        Assert.assertEquals(0.0, gini.compute(list1, false), 10e-4);
        Assert.assertEquals(gini.compute(list1, false), gini.compute(list1, true), 10e-4);
        
        list1.clear();
        Assert.assertEquals(0.0, gini.compute(list1, false), 10e-4);
        Assert.assertEquals(0.0, gini.compute(list1, true), 10e-4);
        
        list1.add(1.0);
        Assert.assertEquals(0.0, gini.compute(list1, true));
        Assert.assertEquals(0.0, gini.compute(list1, false));
        
        list1.clear();
        for(int i = 0; i < 100; ++i)
        {
            list1.add(i+1.0);
        }
        Assert.assertEquals(0.33333, gini.compute(list1, false), 10e-4);
        Assert.assertEquals(0.33333, gini.compute(list1, true), 10e-4);

        list1.clear();
        for(int i = 0; i < 100; ++i)
        {
            list1.add(100.0 - i);
        }
        Assert.assertEquals(0.33333, gini.compute(list1, true), 10e-4);

        list1.clear();
        for(int i = 1; i <= 4; ++i)
        {
            for(int j = 0; j < i; ++j)
            {
                list1.add(i + 0.0);
            }
        }
        Assert.assertEquals(0.2, gini.compute(list1, false), 10e-4);
        Assert.assertEquals(0.2, gini.compute(list1, true), 10e-4);
        
        list1.clear();
        list1.add(4.0);
        list1.add(3.0);
        list1.add(2.0);
        list1.add(3.0);
        list1.add(1.0);
        list1.add(2.0);
        list1.add(4.0);
        list1.add(4.0);
        list1.add(3.0);
        list1.add(4.0);
        Assert.assertEquals(0.2, gini.compute(list1, true), 10e-4);
        
        list1.clear();
        list1.add(0.0);
        list1.add(1.0);
        
        Assert.assertEquals(1.0, gini.compute(list1, true),10e-4);
        Assert.assertEquals(1.0, gini.compute(list1, false),10e-4);
    }
}
