package com.mmaioe.nifi.processor;

import com.mmaioe.nifi.logging.ComponentLogFactory;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;

/**
 *
 *
 * Created by mi186020 on 2017/08/31.
 */
public abstract class AbstractNifiProcessor extends AbstractProcessor {
    /*
         * Component log
         */
    private ComponentLog log;

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        // Create Spring application context
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
        applicationContext.refresh();

        // Get component log
        final ComponentLogFactory logFactory = applicationContext.getBean(ComponentLogFactory.class);
        log = logFactory.getLog(context);
    }

    /**
     * Gets the logger for this processor.
     *
     * @return the component log
     */
    protected final ComponentLog getLog() {
        return log;
    }
}
