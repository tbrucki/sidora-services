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

import edu.si.services.fedorarepo.FedoraComponent;
import edu.si.services.fedorarepo.FedoraSettings;
import org.apache.camel.CamelContext;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.junit.Before;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author jbirkhimer
 */
public class EDAN_CT_BlueprintTestSupport extends CamelBlueprintTestSupport {

    private Boolean useRealFedoraServer = false;
    private static Configuration config = null;
    private String defaultTestProperties = "src/test/resources/test.properties";
    private String propertiesPersistentId = "edu.si.sidora.karaf";

    protected Boolean isUseActualFedoraServer() {
        return useRealFedoraServer;
    }

    protected void setUseActualFedoraServer(Boolean useActualFedoraServer) {
        this.useRealFedoraServer = useActualFedoraServer;
    }

    protected static Configuration getConfig() {
        return config;
    }

    protected void setDefaultTestProperties(String defaultTestProperties) {
        this.defaultTestProperties = defaultTestProperties;
    }

    protected void setPropertiesPersistentId(String propertiesPersistentId) {
        this.propertiesPersistentId = propertiesPersistentId;
    }

    protected List<String> loadAdditionalPropertyFiles() {
        return null;
    }

    protected String[] preventRoutesFromStarting() {
        return null;
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        //add fedora component using test properties to the context
        if (isUseActualFedoraServer()) {
            FedoraSettings fedoraSettings = new FedoraSettings(
                    String.valueOf(config.getString("si.fedora.host")),
                    String.valueOf(config.getString("si.fedora.user")),
                    String.valueOf(config.getString("si.fedora.password"))
            );
            FedoraComponent fedora = new FedoraComponent();
            fedora.setSettings(fedoraSettings);
            context.addComponent("fedora", fedora);
        }

        //Prevent Certain Routes From Starting
        String[] routeList = preventRoutesFromStarting();
        if (routeList != null) {
            for (String route : routeList) {
                context.getRouteDefinition(route).autoStartup(false);
            }
        }

        return context;
    }

    @Override
    protected String[] loadConfigAdminConfigurationFile() {
        return new String[]{defaultTestProperties, "edu.si.sidora.karaf"};
    }


    @Before
    @Override
    public void setUp() throws Exception {
        System.setProperty("karaf.home", "target/test-classes");

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.fileBased().setFile(new File(defaultTestProperties)));
        config = builder.getConfiguration();

        List<String> propFileList = loadAdditionalPropertyFiles();
        if (loadAdditionalPropertyFiles() != null) {
            for (String propFile : propFileList) {

                FileBasedConfigurationBuilder<FileBasedConfiguration> builder2 =
                        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                                .configure(params.fileBased().setFile(new File(propFile)));

                for (Iterator<String> i = builder2.getConfiguration().getKeys(); i.hasNext(); ) {
                    String key = i.next();
                    Object value = builder2.getConfiguration().getProperty(key);
                    if (!config.containsKey(key)) {
                        config.setProperty(key, value);
                    }
                }
            }
        }

        builder.save();

        super.setUp();
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {

        Properties extra = new Properties();

        for (Iterator<String> i = config.getKeys(); i.hasNext();) {
            String key = i.next();
            Object value = config.getProperty(key);
            extra.setProperty(key, String.valueOf(value));
        }

        return extra;
    }
}
