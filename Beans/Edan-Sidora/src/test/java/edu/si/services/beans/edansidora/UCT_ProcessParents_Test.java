/*
 * Copyright 2015-2016 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 */

package edu.si.services.beans.edansidora;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * Testing Project and Subproject findObject logic using alternate id's to check if an object exists
 * and falling back to checking using string name. The test assumes neither Project or SubProject exist
 * and will continue to create the object, However actual object creation is intercepted or sent to mock endpoints.
 *
 * @author jbirkhimer
 */
public class UCT_ProcessParents_Test extends EDAN_CT_BlueprintTestSupport {

    private static String LOG_NAME = "edu.si.uctingest";

    private static final File testManifest = new File("src/test/resources/unified-test-deployment/deployment_manifest.xml");
    private static final File projectRELS_EXT = new File("src/test/resources/test-data/projectRELS-EXT.rdf");
    private static final File subProjectRELS_EXT = new File("src/test/resources/test-data/subprojectRELS-EXT.rdf");
    private static final File objectNotFoundFusekiResponse = new File("src/test/resources/test-data/objectNotFoundFusekiResponse.xml");


    @Override
    protected String getBlueprintDescriptor() {
        return "Routes/unified-camera-trap-route.xml";
    }

    @Override
    protected String[] preventRoutesFromStarting() {
        return new String[]{"UnifiedCameraTrapInFlightConceptStatusPolling"};
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    /**
     * Testing Project and Subproject findObject logic using alternate id's to check if an object exists
     * and falling back to checking using string name. The test assumes neither Project or SubProject exist
     * and will continue to create the object, However actual object creation is intercepted or sent to mock endpoints.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void processParentsMockFedoraTest() throws Exception {

        //The mock endpoint we are sending to for assertions
        MockEndpoint mockParents = getMockEndpoint("mock:processParentsResult");
        mockParents.expectedMessageCount(2);
        //We want to make sure that the expected bodies have the correct Project and SubProject RELS-EXT
        mockParents.expectedBodiesReceived(readFileToString(projectRELS_EXT), readFileToString(subProjectRELS_EXT));

        /* Advicewith the routes as needed for this test */
        context.getRouteDefinition("UnifiedCameraTrapProcessParents")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        //Intercept sending to processPlot we are only testing Project and Subproject RELS-EXT Creation
                        interceptSendToEndpoint("direct:processPlot").skipSendToOriginalEndpoint().log(LoggingLevel.INFO, "Skipping processPlot");
                    }
                });

        context.getRouteDefinition("UnifiedCameraTrapProcessProject")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        //NOTE: for some reason the intercepts also gets applied to other routes as well so we handle them here
                        //Intercept sending to fedora:create but provide a pid
                        interceptSendToEndpoint("fedora:create.*").skipSendToOriginalEndpoint()
                                .choice()
                                    .when(simple("${routeId} == 'UnifiedCameraTrapProcessProject'"))
                                        .setHeader("CamelFedoraPid", simple("test:0001"))
                                    .endChoice()
                                    .when(simple("${routeId} == 'UnifiedCameraTrapProcessSubproject'"))
                                        .setHeader("CamelFedoraPid", simple("test:0002"))
                                    .endChoice()
                                .end();

                        //intercept sending to fedora:addDatastream and send to mock endpoint to assert correct RELS-EXT
                        interceptSendToEndpoint("fedora:addDatastream.*RELS-EXT.*").skipSendToOriginalEndpoint().setHeader("routeId", simple("${routeId}")).to("mock:processParentsResult");

                        //intercept other calls to fedora that are not needed and skip them
                        interceptSendToEndpoint("fedora:hasConcept.*").skipSendToOriginalEndpoint().log(LoggingLevel.INFO, "Skipping fedora:hasConcept");
                        interceptSendToEndpoint("fedora:addDatastream.*EAC-CPF.*").skipSendToOriginalEndpoint().log(LoggingLevel.INFO, "Skipping fedora:addDatastream for EAC-CPF");
                    }
                });

        context.getRouteDefinition("UnifiedCameraTrapProcessSubproject")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        //intercept other calls to fedora that are not needed and have not been intercepted yet and skip them
                        interceptSendToEndpoint("fedora:hasConcept.*").skipSendToOriginalEndpoint().log(LoggingLevel.INFO, "Skipping fedora:hasConcept");
                        interceptSendToEndpoint("fedora:addDatastream.*FGDC-Research.*").skipSendToOriginalEndpoint().log(LoggingLevel.INFO, "Skipping fedora:addDatastream for FGDC-Research");
                    }
                });

        context.getRouteDefinition("UnifiedCameraTrapFindObject")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        //replace the actual fuseki http call and provide our own response
                        weaveById("findObjectFusekiHttpCall").replace().setBody().simple(readFileToString(objectNotFoundFusekiResponse));
                    }
                });

        context.start();

        //Initialize the exchange with body and headers as needed
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setHeader("ManifestXML", readFileToString(testManifest));
        exchange.getIn().setHeader("CamelFileParent", "someCamelFileParent");
        exchange.getIn().setHeader("CamelFedoraPid", getExtra().getProperty("si.ct.root"));

        // The endpoint we want to start from with the exchange body and headers we want
        template.send("direct:processParents", exchange);

        //assert again that the expected bodies have the correct Project and SubProject RELS-EXT
        assertEquals(readFileToString(projectRELS_EXT), mockParents.getExchanges().get(0).getIn().getBody());
        assertEquals(readFileToString(subProjectRELS_EXT), mockParents.getExchanges().get(1).getIn().getBody());

        //Show the bodies sent to the mock endpoint in the log
        for (Exchange mockExchange : mockParents.getExchanges()) {
            log.info("Result from {} route: {}", mockExchange.getIn().getHeader("routeId"), mockExchange.getIn().getBody(String.class));
        }

        assertMockEndpointsSatisfied();
    }
}
