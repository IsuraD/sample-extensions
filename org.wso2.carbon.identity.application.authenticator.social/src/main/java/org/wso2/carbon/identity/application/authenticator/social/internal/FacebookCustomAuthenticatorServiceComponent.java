/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authenticator.social.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authenticator.social.facebook2.FacebookCustomAuthenticator;

import java.util.Hashtable;

/**
 * @scr.component name="identity.application.authenticator.facebook.component"
 *                immediate="true"
 */
public class FacebookCustomAuthenticatorServiceComponent {

    private static final Log LOGGER = LogFactory.getLog(FacebookCustomAuthenticatorServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            FacebookCustomAuthenticator facebookAuthenticator = new FacebookCustomAuthenticator();
            Hashtable<String, String> props = new Hashtable<String, String>();

            ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                                                    facebookAuthenticator, props);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Facebook Custome Authenticator bundle is activated");
            }
        } catch (Throwable e) {
            LOGGER.fatal(" Error while activating Facebook authenticator ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Facebook Custom Authenticator bundle is deactivated");
        }
    }
}
