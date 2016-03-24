/*
 * Copyright 2015 Smithsonian Institution.
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
package edu.si.services.beans.cameratrap;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * The CameraTrap Route Builder contains some of the CameraTrap Camel routes such as the post validation logic in Java DSL
 * to execute during the CameraTrap Deployment Package ingestion process.  The current CameraTrap routes are in
 * both Camel Java DSL and XML DSL, with most of the ingestion logic in the XML DSL.
 *
 * @author parkjohn
 */
public class CameraTrapRouteBuilder extends RouteBuilder {

    static final private String CT_LOG_NAME = "edu.si.ctingest";

    /**
     * Configure the Camel routing rules for the Camera Trap Deployment Package Ingestion Process.
     */
    @Override
    public void configure() {

        //CameraTrapValidatePostResourceCount route for RELS-EXT resource reference count validation
        from("direct:validatePostResourceCount")
            .routeId("CameraTrapValidatePostResourceCount")
            .choice()
                .when(header("ResourceCount").isEqualTo(header("RelsExtResourceCount")))
                    .log(LoggingLevel.INFO, CT_LOG_NAME, "${id} CameraTrapIngest: Post Resource Count validation passed")
                    .id("ValidatePostResourceCountWhenBlock")
                .otherwise()
                    .log(LoggingLevel.WARN, CT_LOG_NAME, "${id} CameraTrapIngest: Post Resource Count validation failed - sending msg to queue")
                    .setHeader("ValidationErrors", simple("ValidationErrors"))
                    .to("bean:cameraTrapValidationMessage?method=createValidationMessage(${header.CamelFileParent}, 'Post Resource Count validation failed. " +
                            "Expected ${header.ResourceCount} but found ${header.RelsExtResourceCount}', false)")
                    .to("direct:validationErrorMessageAggregationStrategy")
            .endChoice();

        //CameraTrapValidateFedoraResource route for Fedora RI resource search validation
        from("direct:validateFedoraResource")
            .routeId("CameraTrapValidateFedoraResource")
            .setBody(header("ValidationPID"))
            .to("direct:findObjectByPIDPredicate")
            .choice()
                //Fedora RI search successful
                .when(body().isEqualTo("true"))
                    .log(LoggingLevel.INFO, CT_LOG_NAME, "${id} CameraTrapIngest: Fedora RI search validation passed")
                    .stop()
                //Fedora RI search unsuccessful
                .otherwise()
                    .choice()
                         //Checks if the redelivery attempts has reached the max
                        .when(simple("${header.ValidationRedeliveryCounter} >= ${header.ValidationMaxRedeliveryAttempt}"))
                            .log(LoggingLevel.WARN, CT_LOG_NAME, "${id} CameraTrapIngest: Fedora RI search validation failed - sending msg to queue")
                            .setHeader("ValidationErrors", simple("ValidationErrors"))
                            .to("bean:cameraTrapValidationMessage?method=createValidationMessage(${header.CamelFileParent}, '${header.ValidationPID}', " +
                                    "'Fedora RI Search validation failed', false)")
                            .to("direct:validationErrorMessageAggregationStrategy")
                        //The redelivery attempts has not reached the max and re-retry searching the Fedora RI for the PID.
                        .otherwise()
                            //Increment the redelivery counter
                            .setHeader("ValidationRedeliveryCounter", simple("${header.ValidationRedeliveryCounter}++"))
                            .delay(simple("${header.ValidationRedeliveryDelay}"))
                            .log(LoggingLevel.DEBUG, CT_LOG_NAME, "${id} CameraTrapIngest: Fedora RI search validation failed - try again")
                            .to("direct:validateFedoraResource")
                    .endChoice() // end of inner choice for the redelivery attempt logic
            .endChoice();  // end of outer choice for the Fedora RI search result check

    }
}
