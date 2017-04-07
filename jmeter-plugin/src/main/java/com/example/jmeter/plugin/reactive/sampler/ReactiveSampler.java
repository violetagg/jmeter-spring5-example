package com.example.jmeter.plugin.reactive.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class ReactiveSampler extends AbstractJavaSamplerClient {

    private static final String PROTOCOL = "protocol";
    private static final String PROTOCOL_DEFAULT = "http";

    private static final String DOMAIN = "domain";
    private static final String DOMAIN_DEFAULT = "localhost";

    private static final String PORT = "port";
    private static final String PORT_DEFAULT = "8080";

    private static final String PATH = "path";
    private static final String PATH_DEFAULT = "";

    private static final String METHOD = "method";
    private static final String METHOD_DEFAULT = "GET";

    private static final String SAMPLE_TIMEOUT = "sample.timeout";
    private static final String SAMPLE_TIMEOUT_DEFAULT = "500"; // milliseconds

    private static final String INITIAL_RESPONSE_READ_DELAY = "initial.response.read.delay";
    private static final String INITIAL_RESPONSE_READ_DELAY_DEFAULT = "0"; // milliseconds

    private static final String RESPONSE_READ_DELAY = "response.read.delay";
    private static final String RESPONSE_READ_DELAY_DEFAULT = "0"; // milliseconds

    private static final String ACCEPT = "accept";
    private static final String ACCEPT_DEFAULT = ""; // milliseconds

    private final ReactiveWebClient client = new ReactiveWebClient();

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(PROTOCOL, PROTOCOL_DEFAULT);
        defaultParameters.addArgument(DOMAIN, DOMAIN_DEFAULT);
        defaultParameters.addArgument(PORT, PORT_DEFAULT);
        defaultParameters.addArgument(PATH, PATH_DEFAULT);
        defaultParameters.addArgument(METHOD, METHOD_DEFAULT);
        defaultParameters.addArgument(SAMPLE_TIMEOUT, SAMPLE_TIMEOUT_DEFAULT);
        defaultParameters.addArgument(INITIAL_RESPONSE_READ_DELAY,
                INITIAL_RESPONSE_READ_DELAY_DEFAULT);
        defaultParameters.addArgument(RESPONSE_READ_DELAY, RESPONSE_READ_DELAY_DEFAULT);
        defaultParameters.addArgument(ACCEPT, ACCEPT_DEFAULT);
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        String method = context.getParameter(METHOD, METHOD_DEFAULT);
        String url = getURL(context);
        long sampleTimeout = context.getLongParameter(SAMPLE_TIMEOUT,
                Long.valueOf(SAMPLE_TIMEOUT_DEFAULT));
        long initialResponseReadDelay = context.getLongParameter(INITIAL_RESPONSE_READ_DELAY,
                Long.valueOf(INITIAL_RESPONSE_READ_DELAY_DEFAULT));
        long responseReadDelay = context.getLongParameter(RESPONSE_READ_DELAY,
                Long.valueOf(RESPONSE_READ_DELAY_DEFAULT));
        String accept = context.getParameter(ACCEPT, ACCEPT_DEFAULT);

        return this.client.request(method, url, sampleTimeout, initialResponseReadDelay,
                responseReadDelay, accept);
    }

    private String getURL(JavaSamplerContext context) {
        String protocol = context.getParameter(PROTOCOL, PROTOCOL_DEFAULT);
        String domain = context.getParameter(DOMAIN, DOMAIN_DEFAULT);
        int port = context.getIntParameter(PORT, Integer.valueOf(PORT_DEFAULT));
        String path = context.getParameter(PATH);
        return protocol + "://" + domain + ":" + port + path;
    }
}
