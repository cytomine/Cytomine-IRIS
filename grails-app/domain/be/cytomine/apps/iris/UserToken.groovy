
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
package be.cytomine.apps.iris

/**
 * A token authorizes the user for a one time action without providing the API keys to
 * the REST api.
 *
 * @author Philipp Kainz
 * @since 2.4
 */
class UserToken {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        token nullable: false
        valid nullable: false
    }

    /**
     * Generate a random UUID as token.
     */
    UserToken(){
        this.token = UUID.randomUUID()
    }

    /**
     * The string-token that allows the owner to do something
     */
    String token

    /**
     * The user that is bound to the token
     */
    IRISUser user

    /**
     * Description of the action bound to this token
     */
    String description

    /**
     * A flag that's indicating whether the token is valid.
     * Per default, it is true.
     */
    Boolean valid = true

    /**
     * A method to directly invalidate the token
     */
    def invalidate() {
        this.valid = false
        save(flush: true)
    }
}
