
/* Copyright the original author or authors.
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
package be.cytomine.apps.iris;

import java.io.File;

/**
 * A simple ping manager, which writes unsuccessful ping counter in a temporary file.
 *
 * @author Philipp Kainz
 * @since 1.7
 */
public class ServerPing extends PropertyManager {

    private static ServerPing instance;

    /**
     * Gets the singleton instance of this class.
     *
     * @return the singleton
     */
    public static ServerPing getInstance() {
        if (ServerPing.instance == null) {
            ServerPing.instance = new ServerPing();
        }
        return ServerPing.instance;
    }

    static String tmpRoot = System.getProperty("java.io.tmpdir");
    static String fileName = tmpRoot + File.separator + "ServerPing.properties";
    String failKey = "pingfailcount";

    public ServerPing() {
        super(new File(fileName));
        // write the default mapping file, if there is no mapping available
        if (!this.propFile.exists()) {
            // write defaults
            this.resetFailCount();
        } else {
            // initialize the property manager with the new file
            this.read();
        }
    }

    /**
     * Increments the fail count by one and returns the new number.
     *
     * @return the new fail count number
     */
    public int incrementFailCount() {
        int oldCount = getFailCount();
        int newCount = oldCount + 1;
        this.setProperty(failKey, String.valueOf(newCount));
        return newCount;
    }

    /**
     * Gets the fail count.
     *
     * @return the current fail count number
     */
    public int getFailCount() {
        return Integer.valueOf(this.getProperty(failKey));
    }

    /**
     * Resets the fail count to zero.
     */
    public void resetFailCount() {
        this.setProperty(failKey, "0");
    }
}
