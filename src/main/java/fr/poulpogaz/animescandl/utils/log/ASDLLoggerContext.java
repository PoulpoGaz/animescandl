package fr.poulpogaz.animescandl.utils.log;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.MessageFactory;

import java.net.URI;

public class ASDLLoggerContext extends LoggerContext {

    public ASDLLoggerContext(String name) {
        super(name);
    }

    public ASDLLoggerContext(String name, Object externalContext) {
        super(name, externalContext);
    }

    public ASDLLoggerContext(String name, Object externalContext, URI configLocn) {
        super(name, externalContext, configLocn);
    }

    public ASDLLoggerContext(String name, Object externalContext, String configLocn) {
        super(name, externalContext, configLocn);
    }

    @Override
    public void setName(String name) {
        super.setName("ASDLContext[" + name + "]");
    }

    @Override
    protected Logger newInstance(LoggerContext ctx, String name, MessageFactory messageFactory) {
        return new ASDLLogger(ctx, name, messageFactory);
    }
}
