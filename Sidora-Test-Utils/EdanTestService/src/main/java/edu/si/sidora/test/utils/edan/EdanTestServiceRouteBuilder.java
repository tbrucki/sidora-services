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

package edu.si.sidora.test.utils.edan;

import org.apache.camel.LoggingLevel;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * @author jbirkhimer
 */
public class EdanTestServiceRouteBuilder extends RouteBuilder {

    @PropertyInject(value = "edu.si.edan.test")
    static private String LOG_NAME;

    @Override
    public void configure() throws Exception {

        from("cxfrs://bean://rsServer?bindingStyle=SimpleConsumer")
                .routeId("EdanTestService")
                .log(LoggingLevel.INFO, LOG_NAME, "Starting Edan Service Request for: ${header.operationName} ... ")
                .recipientList(simple("direct:${header.operationName}"))
                .log(LoggingLevel.INFO, LOG_NAME, "Finished Edan Service Request for: ${header.operationName} ... ");

        from("direct:addEdan")
                .routeId("addEdan")
//                .to("log:edanService?showAll=true&multiline=true&maxChars=100000")
                .log(LoggingLevel.INFO, LOG_NAME, "===================================================")
                .log(LoggingLevel.INFO, LOG_NAME, "${body}")
                .log(LoggingLevel.INFO, LOG_NAME, "===================================================");

        from("direct:edan").routeId("edan-test")
                .log(LoggingLevel.INFO, LOG_NAME, "===================================================")
                .to("log:edan.test.service?showAll=true&multiline=true&maxChars=100000")
                .log(LoggingLevel.INFO, LOG_NAME, "===================================================");
    }
}
