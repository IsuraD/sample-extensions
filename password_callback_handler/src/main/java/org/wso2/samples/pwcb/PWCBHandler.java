/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.samples.pwcb;

import org.apache.ws.security.WSPasswordCallback;
import org.sample.securevault.TestConf;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class PWCBHandler implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        TestConf testConf = new TestConf();
        for (Callback callback : callbacks) {
            WSPasswordCallback pwcb = (WSPasswordCallback) callback;
            String id = pwcb.getIdentifer();
            int usage = pwcb.getUsage();
            if (usage == WSPasswordCallback.USERNAME_TOKEN) {
                // Logic to get the password to build the username token
                if ("wso2carbon".equals(id)) {
                    pwcb.setPassword(testConf.getPassword());
                }
            } else if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {
                // Logic to get the private key password for signature or decryption
                if ("wso2carbon".equals(id)) {
                    pwcb.setPassword(testConf.getPassword());
                }
                if ("wso2carbon".equals(id)) {
                    pwcb.setPassword(testConf.getPassword());
                }
            }
        }
    }
}
