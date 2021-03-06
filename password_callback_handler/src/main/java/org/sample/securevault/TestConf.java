
/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sample.securevault;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestConf {

    private static final Log log = LogFactory.getLog(TestConf.class);
    private String password;
    private String serverURL;
    private String remote;

    public TestConf() {
        InputStream fileInputStream = null;
        String configPath = CarbonUtils.getCarbonHome()+ File.separator + "repository" + File.separator + "conf" +
                File.separator + "conf-test1.xml";

        File registryXML = new File(configPath);
        if (registryXML.exists()) {
            try {
                fileInputStream = new FileInputStream(registryXML);

                StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                builder.setNamespaceURIInterning(true);
                OMElement configElement = builder.getDocumentElement();
                //Initialize the SecretResolver providing the configuration element.

                SecretResolver secretResolver = SecretResolverFactory.create(configElement, false);

                OMElement module = configElement.getFirstChildWithName(new QName("module"));
                if (module != null) {

                    //same entry used in cipher-text.properties and cipher-tool.properties.
                    String secretAlias = "testconf.module.password";

                    //Resolved the secret password.
                    if (secretResolver != null && secretResolver.isInitialized()) {
                        if (secretResolver.isTokenProtected(secretAlias)) {
                            password = secretResolver.resolve(secretAlias);
                        } else {
                            password = module.getFirstChildWithName(new QName("password")).getText();
                        }
                    }
                    serverURL = module.getAttributeValue(new QName("serverURL"));
                    remote = module.getAttributeValue(new QName("remote"));
                }
            } catch (XMLStreamException e) {
                log.error("Unable to parse conf-test1.xml", e);
            } catch (IOException e) {
                log.error("Unable to read conf-test1.xml", e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        log.error("Failed to close the FileInputStream, file : " + configPath);
                    }
                }
            }
        }
    }


    public String getPassword() {
        return password;
    }

    public String getServerURL() {
        return serverURL;
    }

    public boolean isRemote() {
        return Boolean.valueOf(remote);
    }
}
