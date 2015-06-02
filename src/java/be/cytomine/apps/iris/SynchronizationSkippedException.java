
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

/**
 * Custom exception, which is thrown if the synchronization of a domain object with Cytomine
 * is skipped due to any reason.
 */
public class SynchronizationSkippedException extends Exception {

    public static enum REASON {
        HOST_DID_NOT_MATCH,
        SYNC_DISABLED,
        UNDEFINED
    }

    private REASON reason = REASON.UNDEFINED;

    // hide default constructor
    private SynchronizationSkippedException() {
    }

    public SynchronizationSkippedException(REASON reason) {
        super();
        this.reason = reason;
    }

    public SynchronizationSkippedException(String message, REASON reason) {
        super(message);
        this.reason = reason;
    }

    public REASON getReason() {
        return reason;
    }

    @Override
    public String toString() {
        String supStr = super.toString();
        return supStr + " -> Reason " + this.getReason();
    }
}
