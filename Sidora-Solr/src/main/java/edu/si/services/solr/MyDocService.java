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

package edu.si.services.solr;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jbirkhimer
 */
public class MyDocService {

    private static final Logger LOG = LoggerFactory.getLogger(MyDocService.class);

    /**
     * We just handle the pid by returning a solr doc for the pid
     */
    public String handleDoc_gsearch_solr(Exchange exchange) throws InterruptedException {
        String response = doc(exchange);
        LOG.debug("handleDoc_gsearch_solr Doc :\n{}", response);
        return response;
    }

    public String handleDoc_gsearch_sianct(Exchange exchange) {
        String response = doc(exchange);
        LOG.debug("handleDoc_gsearch_sianct Doc :\n{}", response);
        return response;
    }

    public String handleDeleteDoc(Exchange exchange) {
        String response = delete(exchange);
        LOG.debug("handleDeleteDoc Doc :\n{}", response);
        return response;
    }

    /**
     * We use the same bean for building the combined response to send
     * back to the original caller
     */
    public String buildCombinedResponse(Exchange exchange) {
        Message out = exchange.getIn();
        String doc = out.getBody(String.class).trim();
        String solrOperation = out.getHeader("methodName", String.class);

        String response = "<update>\n" + doc + "\n</update>";
        LOG.info("BuildCombinedResponse:\n" + response);

        return response;
    }

    public String doc(Exchange exchange) {
        Message out = exchange.getIn();
        ArrayList<String> pidDataList = out.getBody(ArrayList.class);

        //return DOC.replaceAll("TEST_PID:.*?(?=[</])", pid);
        return "   <doc>\n" +
                "       " + pidDataList + " \n" +
                "   </doc>";
    }

    public String delete(Exchange exchange) {
        Message out = exchange.getIn();
        ArrayList<String> pidDataList = out.getBody(ArrayList.class);

        //return DOC.replaceAll("TEST_PID:.*?(?=[</])", pid);
        return "   <id> " + pidDataList + " </id>";
    }

    public void printReceived(Exchange exchange) {
        Message out = exchange.getIn();
        String aggHeader = null;
        List<Exchange> ex = exchange.getIn().getBody(List.class);
        List<MySolrJob> r = new ArrayList<>();
        for (Exchange e : ex) {
            r.add(e.getIn().getHeader("solrJob", MySolrJob.class));
            aggHeader = e.getIn().getHeader("batch", String.class);
        }

        LOG.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        for(MySolrJob msj : r) {
            LOG.info("Received END {} ({} of {}): {}", aggHeader, r.indexOf(msj) + 1,r.size(), msj);
        }
    }
}
