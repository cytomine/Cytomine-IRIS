
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

import be.cytomine.client.Cytomine
import be.cytomine.client.collections.AnnotationCollection

import java.text.SimpleDateFormat

/**
 * Maintenance script for synchronizing the cached user progress in the IRIS database. 
 * It updates the total number of annotations and computes the user progress for each user.
 *
 * @author Philipp Kainz
 * @since 1.4
 */
class SynchronizeUserProgressJob {
    static triggers = {
        simple name: 'initAtStartup', startDelay: 10000L, repeatCount: 0 // runs once at server start (10 seconds delay)
//        cron name: 'every2Hours', cronExpression: "0 0 0/2 * * ?" // runs every two hours
        cron name: 'nightlySync', cronExpression: "0 0 3 * * ?" // runs once a day at 3 AM
    }

    // disable concurrent running
    def concurrent = false

    def description = "Synchronize user labeling progress from the Cytomine core server. " +
            " Running once at server startup and once at 3 AM."

    // the configuration of the IRIS server
    def grailsApplication
    def sessionService
    def imageService
    def activityService
    def mailService
    def syncService
    def executorService
    def annotationService

    String _override = "";

    def syncExceptions = []

    /**
     * Custom constructor for calling via controller.
     *
     * @param grailsApplication
     * @param sessionService
     * @param imageService
     * @param activityService
     * @param mailService
     * @param syncService
     * @param executorService
     * @param annotationService
     */
    SynchronizeUserProgressJob(def grailsApplication, def sessionService,
                               def imageService, def activityService,
                               def mailService, def syncService,
                               def executorService, def annotationService, String override) {
        this.grailsApplication = grailsApplication
        this.sessionService = sessionService
        this.imageService = imageService
        this.activityService = activityService
        this.mailService = mailService
        this.syncService = syncService
        this.executorService = executorService
        this.annotationService = annotationService
        this._override = override
    }

    /**
     * Empty constructor for scheduled access.
     */
    SynchronizeUserProgressJob() {
    }

    /**
     * Sync job which can be triggered by the admin.
     *
     * @return
     */
    def runExplicit() throws Exception{
        return execute()
    }

    boolean isEnabled(){
        String className = getClass().simpleName
        return !(grailsApplication.config."$className".disabled)
    }

    boolean isOverridden(){
        return "override_config".equals(_override)
    }

    /**
     * Worker method called by the quartz scheduler.
     *
     * @return
     * @throws Exception
     */
    def execute() throws Exception {
        // check, if the job is enabled
        if (!isOverridden()){
            if (!isEnabled()){
                return "deactivated"
            }
        }

        log.info("Starting user progress synchronization...")

        try {
            // get all 'synchronizable' users from the DB
            List<IRISUser> irisUsers
            IRISUser.withTransaction {
                def userCriteria = IRISUser.createCriteria()
                irisUsers = userCriteria.list {
                    and {
                        isNull('cmDeleted')
                        eq('synchronize', true)
                        not {
                            'in'('cmUserName', ['system', 'admin', 'superadmin'])
                        }
                    }
                }
            }

            String userNameStr = irisUsers.cmUserName.join(",")

//          if the query did not result in any 'synchronizable' user instances, return true
            if (checkSkip(irisUsers)) {
                String msg = "No eligible users found, skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing progress for users [" + userNameStr + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert irisUsers.size() != 0
            }

            // TODO 1. get a list of ALL ENABLED DISTINCT project IDs of synchronizable users in the DB
            List projectIDs
            IRISUserProjectSettings.withTransaction {
                def prjCriteria = IRISUserProjectSettings.createCriteria()
                projectIDs = prjCriteria.list {
                    and {
                        isNull('deleted')
                        eq('enabled', true)
                        'in'('user', irisUsers)
                    }
                    projections {
                        distinct('cmProjectID')
                    }
                }
            }

            String projectIDStr = projectIDs.join(",")

//          if the query did not result in any 'synchronizable' projects, return true
            if (checkSkip(projectIDs)) {
                String msg = "No eligible projects found for users [" + userNameStr + "], skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing progress for projects [" + projectIDStr + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert projectIDs != []
            }

            // TODO 2. for each of these projects get the ENABLED images
            List imageIDs
            IRISUserImageSettings.withTransaction {
                def imgCriteria = IRISUserImageSettings.createCriteria()
                imageIDs = imgCriteria.list {
                    and {
                        isNull('deleted')
                        eq('enabled', true)
                        'in'('cmProjectID', projectIDs)
                    }
                    projections {
                        distinct('cmImageInstanceID')
                    }
                }
            }

            String imageIDStr = imageIDs.join(",")

//          if the query did not result in any 'synchronizable' images in these projects, return true
            if (checkSkip(imageIDs)) {
                String msg = "No eligible images found for projects [" + projectIDStr + "], skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing progress for images [" + imageIDStr + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert imageIDs != []
            }

            int nImages = imageIDs.size()

            // for each image, get its annotations and compute the progress for every user
            for (int i = 0; i < nImages; i++) {
                Long imageID = imageIDs[i]
                try {
                    List<IRISUser> imageUsers
                    // get the users per image and compute their progress
                    IRISUserImageSettings.withTransaction {
                        def imgCriteria = IRISUserImageSettings.createCriteria()
                        imageUsers = imgCriteria.list {
                            and {
                                eq('cmImageInstanceID', imageID)
                                isNull('deleted')
                                eq('enabled', true)
                            }
                            projections {
                                distinct('user')
                            }
                        }
                    }

                    // get all annotations (including ACL check for that user)
                    AnnotationCollection allImageAnnotations

                    for (int j = 0; j < imageUsers.size(); j++) {
                        // a user which has access to this image is required
                        IRISUser someUserWithAccessToThisImage = imageUsers.get(j)
                        try {
                            // create the cytomine connection for that user
                            Cytomine cytomine = new Cytomine(grailsApplication.config.grails.cytomine.host as String,
                                    someUserWithAccessToThisImage.cmPublicKey, someUserWithAccessToThisImage.cmPrivateKey, "./")

                            allImageAnnotations = annotationService.getImageAnnotationsLight(cytomine,
                                    someUserWithAccessToThisImage, null, imageID)

                            // we have all we want, leave the loop
                            break
                        } catch (Exception ex) {
                            addSyncException(ex, "User '" + someUserWithAccessToThisImage.cmUserName + "' is not allowed to access " +
                                    "image [" + imageID + "].")
                            if (j < imageUsers.size() - 2) {
                                log.warn("I will concern '" + imageUsers.get(j + 1).cmUserName + "' next...")
                            } else {
                                String msg = "I cannot try anyone else to get this " +
                                        "image and have to skip ID [" + imageID + "]!"
                                addSyncException(ex, msg)
                            }
                        }
                    }

                    // skip the image if there are no annotations!
                    if (allImageAnnotations == null || allImageAnnotations.isEmpty()) {
                        log.info("Skipping computing user progress for this image, since no annotations can be found.")
                        continue
                    }

                    imageUsers.each { user ->

                        try {
                            def progressInfo = syncService.computeUserProgress(allImageAnnotations, user)
                            int nLabeled = progressInfo['labeledAnnotations']
                            int nTotal = progressInfo['totalAnnotations']


                            IRISUserImageSettings.withTransaction {
                                // store the record of this user
                                IRISUserImageSettings settings = IRISUserImageSettings
                                        .findByUserAndCmImageInstanceID(user, imageID)

                                settings?.lock()
                                settings?.setLabeledAnnotations(nLabeled)
                                settings?.setNumberOfAnnotations(nTotal)
                                settings?.computeProgress()

                                settings?.merge(flush: true)
                            }

                            log.trace("Done synchronizing image [" + imageID + "] for user '" + user.cmUserName + "'.")
                        } catch (Exception e) {
                            String msg = "Cannot synchronize image [" + imageID +
                                    "] for user '" + user.cmUserName + "'."
                            addSyncException(e, msg)
                        }
                    }
                    String msg = "Done synchronizing image [" + imageID + "] for all its users."
                    log.info(msg)
                    activityService.logSync(msg)
                } catch (Exception e) {
                    addSyncException(e, "Cannot synchronize image [" + imageID + "]!")
                }
            }

        } catch (Exception ex) {
            String msg = "The synchronization failed! This is a serious global error, you should act quickly!"
            log.fatal(msg, ex)
            // GLOBAL ERROR
            addSyncException(ex, msg)
        }

        if (!this.syncExceptions.isEmpty()) {
            String recipient = grailsApplication.config.grails.cytomine.apps.iris.server.admin.email

            log.info("User synchronization succeeded, but had some errors! Sending email to admin...")

            // notify the admin
            mailService.sendMail {
                async true
                to recipient
                subject new SimpleDateFormat('E, yyyy-MM-dd').format(new Date()) + ": Progress synchronization had some errors"
                body('Errors occurred during scheduled user progress synchronization. See stack traces below. \n\n\n'
                    + exceptionsToString()
                )
            }
        } else {
            log.info("Splendit! All synchronizations completed without errors :-)")
        }

        log.info("Done synchronizing user progress.")
        return syncExceptions
    }

    /**
     * Checks whether the sync of an item should be skipped
     * @param item
     * @return
     */
    boolean checkSkip(def item) {
        boolean skip = false

        if (item == null)
            skip = true
        else if (item instanceof Collection)
            if (item.isEmpty())
                skip = true

        return skip
    }

    /**
     * Adds an Exception to the list of errors which will be sent to the admin.
     *
     * @param e
     * @param msg
     */
    void addSyncException(Exception e, String msg) {
        syncExceptions.add(['msg': msg, 'exception': e])
        log.error(msg, e)
        executorService.execute({
            activityService.logSync("ERROR\n" + msg)
        })
    }

    /**
     * Prints the exceptions in a reverse chronologically stack of messages/exceptions.
     *
     * @return
     */
    String exceptionsToString(){
        String exStr = "\nEXCEPTIONS ARE ORDERED REVERSE CHRONOLOGICALLY (MOST RECENT FIRST)"
        syncExceptions.reverse()
        syncExceptions.each { item ->
            exStr += (item['msg'] + "\n" + item['exception'] + "\n " +
                    "------------------------------------------------------\n")
        }
    }
}