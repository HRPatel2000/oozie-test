package com.cat.ddsw.base;

import com.cat.ddsw.oozietest.DealerHierarchyOozieTest;
import com.cat.ddsw.oozietest.DealerInventoryOozieTest;
import com.cat.ddsw.oozietest.InvoiceOozieTest;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Created by hpatel on 8/31/2015.
 *
 * ####IMPORTANT####
 * Every Test Case Class needs to be registered here under @Suite.SuiteClasses Block for execution in Parallel.
 * Nos. of Tests to run in Parallel is Configured in oozie-test/build.gradle under the property below,
 * Change the maxParallelForks to the number that Machine will sustain from resource perspective.
 * Currently it is 4. Meaning only 4 Tests will be running in parallel.
 *
 * test {
 *    systemProperties['oozie.properties'] = 'oozie.properties'
 *    maxParallelForks = 4
 * }
 *
 */
@Suite.SuiteClasses(
        {
                DealerHierarchyOozieTest.class,
                DealerInventoryOozieTest.class,
                InvoiceOozieTest.class
        }
)
public class DDSWParallelSuite extends Suite {

    public DDSWParallelSuite(Class<?> klass, RunnerBuilder builder)
            throws InitializationError {
        super(klass, builder);
        setScheduler(new DDSWParallelScheduler());
    }
}