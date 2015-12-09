package com.cat.ddsw.base;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Created by hpatel on 8/31/2015.
 */
public class DDSWParallelRunner extends BlockJUnit4ClassRunner {

    public DDSWParallelRunner(Class<?> klass) throws InitializationError {
        super(klass);
        setScheduler(new DDSWParallelScheduler());
    }
}