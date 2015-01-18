package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISAnnotation
import be.cytomine.apps.iris.model.IRISImage
import grails.converters.JSON
import org.json.simple.JSONArray;
import org.json.simple.JSONObject
import grails.transaction.Transactional
import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.TermCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.AnnotationTerm;

/**
 * The annotation service allows term modifications of annotations, provides prefetch support for navigation and
 * handles persistence for the IRISUserSession.
 *
 * @author Philipp Kainz
 * @since 0.5
 */
@Transactional
class AnnotationService {

    def grailsApplication
    def activityService
    def sessionService
    def projectService
    def imageService
    def irisService
    def syncService

    /**
     * Gets all annotations in an image in a very light format (but containing the user labels).
     *
     * @param cytomine a Cytomine instance
     * @param user the IRISUser
     * @param cmProjectID the Cytomine project ID - may be null, but only if the cmImageID is not null
     * @param cmImageID the Cytomine image ID - may be null, but only if the cmProjectID is not null
     * @return the AnnotationCollection
     * @throws CytomineException
     * @throws Exception
     */
    AnnotationCollection getAllAnnotationsLight(Cytomine cytomine, IRISUser user,
                                                Long cmProjectID, Long cmImageID)
            throws CytomineException, Exception {

        log.debug("Getting all annotations in 'light' format for user '" + user.cmUserName + "'.")

        // get the annotations for this image
        Map<String, String> filters = new HashMap<String, String>()
        filters.put("project", String.valueOf(cmProjectID)) // ACL gets checked when using "project"
        filters.put("image", String.valueOf(cmImageID)) // ACL gets checked when using "image"
        filters.put("showTerm", "true") // retrieves a minimal set of annotations containing the user labels

        // fetch all annotations (necessary because this contains info on the assignments by this user)
        AnnotationCollection annotations = cytomine.getAnnotations(filters)

        return annotations
    }

    /**
     * Resolves the terms assigned by a single user. This method stops searching,
     * as soon as the user has a term assigned, injects the term and returns this annotation.
     * <br/>
     * <b>WARNING: This method is not checking for multiple terms assigned by one user!</b>
     *
     * @param ontologyID the ontologyID
     * @param terms the term list
     * @param user the querying user
     * @param annotation the annotation to search in
     * @param irisAnn the IRIS annotation (result)
     * @return the IRIS annotation
     */
    IRISAnnotation resolveTerms(long ontologyID, TermCollection terms, IRISUser user, Annotation annotation, IRISAnnotation irisAnn) {
        // grab all terms from all users for the current annotation
        List userByTermList = annotation.getList("userByTerm");

        for (assignment in userByTermList) {
            //println currentUser.get("id") + ", " + assignment.get("user")
            if (user.cmID in assignment.get("user")) {
                String cmTermName = terms.list.find { it.id == assignment.get("term") }.get("name")
                log.debug(user.cmUserName + " assigned " + cmTermName)

                // store the properties in the irisAnnotation
                irisAnn.setCmUserID(user.cmID)
                irisAnn.setCmTermID(assignment.get("term"))
                irisAnn.setCmTermName(cmTermName)
                irisAnn.setCmOntologyID(ontologyID)
                irisAnn.setAlreadyLabeled(true)

                log.debug("annotationService.resolveTerms(): METHOD JUST LOOKS FOR THE FIRST TERM ASSIGNMENT BY A USER")

                break
            }
        }

        return irisAnn
    }

    /**
     * Resolves the terms a user assigned to an annotation and injects full term name and other
     * properties into the annotation.
     *
     * @param ontologyID the ontology ID
     * @param terms the list of terms
     * @param user the calling user
     * @param annotation the query annotation
     * @param irisAnn the IRIS annotation
     * @param searchForNoTerm a flag, whether the user is also searching for annotations,
     * 						where he/she did not yet assign a term to
     * @param queryTerms the list of terms to query for
     * @param annotationMap the map of filtered annotations
     * @return the annotation where the query terms match
     */
    IRISAnnotation resolveTermsByUser(long ontologyID,
                                      TermCollection terms, IRISUser user, Annotation annotation,
                                      IRISAnnotation irisAnn, boolean searchForNoTerm,
                                      def queryTerms, JSONObject annotationMap) {
        // grab all terms from all users for the current annotation
        List userByTermList = annotation.getList("userByTerm");

        log.debug(userByTermList)
        log.debug("Searching for 'no Term': " + searchForNoTerm + ". Query terms: " + queryTerms)

        // if no assignment at all is done
        // add the annotation to the map at -99 ('no term assigned'), if the query contains termID=IRISConstants.ANNOTATION_NO_TERM_ASSIGNED
        if (userByTermList.isEmpty()) {
            log.debug("This annotation does not have any terms assigned at all [" + annotation.id + "]")
            if (searchForNoTerm) {
                annotationMap[IRISConstants.ANNOTATION_NO_TERM_ASSIGNED.toString()].get("annotations").add(irisAnn)
            }
        } else {
            boolean userAssignedOntologyTerm = false

            // search the assignments for a match both "user" and "term"
            for (assignment in userByTermList) {

                //log.debug("Querying user is " + user.cmUserName + " (" + user.cmID + ")")

                // if the user has assessed this annotation
                if (user.cmID in assignment.get("user")) {
                    log.debug("user " + user.cmUserName + " has assignment: " + (user.cmID in assignment.get("user")))

                    // check, if the assessed term ID is in the query list
                    long ontologyTermID = assignment.get("term")

                    // if the user has assigned one of the queryTerms
                    // ASSUMPTION: USER CAN ONLY SET UNIQUE LABELS
                    if (queryTerms.contains("" + ontologyTermID)) {
                        String cmTermName = terms.list.find { it.id == ontologyTermID }.get("name")
                        log.debug(user.cmUserName + " assigned an ontology term "
                                + cmTermName + " (" + ontologyTermID + ") to [" + annotation.id + "]")

                        // store the properties in the irisAnnotation
                        irisAnn.setCmUserID(user.cmID)
                        irisAnn.setCmTermID(assignment.get("term"))
                        irisAnn.setCmTermName(cmTermName)
                        irisAnn.setCmOntologyID(ontologyID)
                        irisAnn.setAlreadyLabeled(true)

                        // add the annotation to the corresponding list in the map
                        annotationMap[ontologyTermID + ""].get("annotations").add(irisAnn)
                        userAssignedOntologyTerm = true
                        break
                    } else {
                        log.debug(user.cmUserName + " did not assign any ontology term to [" + annotation.id
                                + "], checking for 'no term' query")
                        if (!searchForNoTerm) {
                            log.debug(user.cmUserName + " did not assign any term to [" + annotation.id + "]")
                            userAssignedOntologyTerm = false
                        } else {
                            log.debug(user.cmUserName + " did not assign any query term to [" + annotation.id + "], skipping annotation")
                            // if querying annotations 'without terms' and the user has none of the terms in the list
                            // count this as hit such that the annotation does not get added to the response
                            userAssignedOntologyTerm = true
                        }
                    }
                }

                // if the user is not in this assignment, continue searching in the other ones
                else {
                    log.debug("User '" + user.cmUserName + "' is not in this assignment, continuing search...")
                }
            }

            // if the user does not have an assignment and we are searching for 'noTerms'
            // add it to the no term list
            if (searchForNoTerm && !userAssignedOntologyTerm) {
                annotationMap[IRISConstants.ANNOTATION_NO_TERM_ASSIGNED.toString()].get("annotations").add(irisAnn)
            }
        }

        return irisAnn
    }

    /**
     * Gets the current annotation from a list and updates the image where the calling user is navigating.
     *
     * @param annotations the list
     * @param startIdx the start index
     * @param hideCompleted flag, whether terms having a label by this user should be skipped
     * @param ontologyID the ontology ID
     * @param terms the list of terms
     * @param user the user to search for
     * @param image the image where the user is searching in
     * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
     */
    IRISAnnotation getCurrentAnnotation(AnnotationCollection annotations,
                                        int startIdx, Boolean hideCompleted,
                                        Long ontologyID, TermCollection terms,
                                        IRISUser user, IRISImage image) {

        Annotation currCmAn = null
        IRISAnnotation currIrisAnn = null

        DomainMapper dm = new DomainMapper(grailsApplication)

        // DETERMINE THE CURRENT ANNOTATION
        currCmAn = annotations.get(startIdx)
        assert currCmAn != null

        currIrisAnn = dm.mapAnnotation(currCmAn, null)
        // resolve the terms for the annotation
        currIrisAnn = resolveTerms(ontologyID, terms, user, currCmAn, currIrisAnn)

        def origIrisAnn = currIrisAnn

        if (hideCompleted) {
            if (currIrisAnn.getAlreadyLabeled()) {
                // if already labeled move to first unlabeled annotation in this list
                // start at -1, such that the first element is 0
                currIrisAnn = getNextAnnotation(annotations, -1, hideCompleted, ontologyID, terms, user)
                if (currIrisAnn == null) {
                    currIrisAnn = origIrisAnn
                }
            }
        }

        // save the current annotation for that image
        image.settings.setCurrentCmAnnotationID(currIrisAnn.cmID)
        image.settings.setHideCompletedAnnotations(hideCompleted)
        image.settings.save(flush: true)

        log.debug("hideCompletedAnnotations: " + image.settings.hideCompletedAnnotations)

        assert currIrisAnn != null

        log.debug("############ CURRENT ANNOTATION RESOLVED")

        return currIrisAnn
    }

    /**
     * Gets the successor of an annotation in a list. If hideCompleted is true, this method
     * searches for the next unlabeled annotation.
     *
     * @param annotations the list
     * @param startIdx the start index
     * @param hideCompleted flag, whether terms with a label should be skipped
     * @param ontologyID the ontology ID
     * @param terms the list of terms
     * @param user the user to search for
     * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
     */
    IRISAnnotation getNextAnnotation(AnnotationCollection annotations,
                                     int startIdx, boolean hideCompleted, long ontologyID, TermCollection terms, IRISUser user) {

        Annotation succCmAnn = null
        IRISAnnotation succIrisAnn = null
        int succIdx = startIdx + 1

        DomainMapper dm = new DomainMapper(grailsApplication)

        boolean foundSuccessor = false

        while (!foundSuccessor) {
            try {
                succCmAnn = annotations.get(succIdx)
                succIrisAnn = dm.mapAnnotation(succCmAnn, null)
                succIrisAnn = resolveTerms(ontologyID, terms, user, succCmAnn, succIrisAnn)

                if (hideCompleted) {
                    if (!succIrisAnn.getAlreadyLabeled()) {
                        foundSuccessor = true
                    } else {
                        succIrisAnn = null
                        // move on in the index list
                        succIdx++
                    }
                } else {
                    foundSuccessor = true
                }
            } catch (IndexOutOfBoundsException e) {
                log.debug("Index " + startIdx + " has no successor.")
                break
            }
        }

        log.debug("############ SUCCESSOR ANNOTATION RESOLVED: has successor " + foundSuccessor)

        return succIrisAnn
    }

    /**
     * Gets the predecessor of an annotation in a list. If hideCompleted is true, this method
     * searches for the previous unlabeled annotation.
     *
     * @param annotations the list
     * @param startIdx the start index
     * @param hideCompleted flag, whether terms with a label should be skipped
     * @param ontologyID the ontology ID
     * @param terms the list of terms
     * @param user the user to search for
     *
     * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
     */
    IRISAnnotation getPreviousAnnotation(AnnotationCollection annotations,
                                         int startIdx, boolean hideCompleted,
                                         long ontologyID, TermCollection terms,
                                         IRISUser user) {

        Annotation predCmAnn = null
        IRISAnnotation predIrisAnn = null
        int predIdx = startIdx - 1

        DomainMapper dm = new DomainMapper(grailsApplication)

        boolean foundPredecessor = false

        while (!foundPredecessor) {
            try {
                predCmAnn = annotations.get(predIdx)
                predIrisAnn = dm.mapAnnotation(predCmAnn, null)
                predIrisAnn = resolveTerms(ontologyID, terms, user, predCmAnn, predIrisAnn)

                if (hideCompleted) {
                    // check if the predecessor is already labeled
                    if (!predIrisAnn.getAlreadyLabeled()) {
                        foundPredecessor = true
                    } else {
                        predIrisAnn = null
                        // move backwards in the index list
                        predIdx--
                    }
                } else {
                    foundPredecessor = true
                }
            } catch (IndexOutOfBoundsException e) {
                log.debug("Index " + startIdx + " has no predecessor.")
                break
            }
        }

        log.debug("############ PREDECESSOR ANNOTATION RESOLVED: has predecessor " + foundPredecessor)

        return predIrisAnn
    }

    /**
     * Removes a single term, or all terms, a user has assigned to an annotation.
     * If the cmTermID is <code>null</code>, then all terms will be removed.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param cmImageID the Cytomine image ID
     * @param cmAnnID the Cytomine annotation ID
     * @param cmTermID the Cytomine term ID to be removed, or <code>null</code>, if <b>every</b> term should be removed
     *
     * @return a map, indicating whether the user has assignments left on that annotation
     * @throws CytomineException if anything goes wrong with the Cytomine Java client
     * @throws Exception in any case of error
     */
    def deleteTermByUser(Cytomine cytomine, IRISUser user,
                             Long cmProjectID, Long cmImageID,
                             Long cmAnnID, Long cmTermID)
            throws CytomineException, Exception {

        AnnotationCollection annotations = getAllAnnotationsLight(cytomine, user, cmProjectID, cmImageID)
        def annList = annotations.list

        Annotation annotation
        try {
            // provoke exception if the annotation is not in the list
            annotation = annotations.get(
                    annList.indexOf(annList.find { ann -> ann.id == cmAnnID }))
        } catch (Exception e) {
            // handle Exception by computation of new user progress
            def progressInfo = syncService.computeUserProgress(annotations,user)

            IRISUserImageSettings settings
            IRISUserImageSettings.withTransaction {
                settings = IRISUserImageSettings
                        .findByUserAndCmImageInstanceID(user, cmImageID)

                settings?.setLabeledAnnotations(progressInfo['labeledAnnotations'] as Long)
                settings?.setNumberOfAnnotations(progressInfo['totalAnnotations'] as Long)
                settings?.computeProgress()

                settings?.merge(flush: true)
            }

            throw e
        }

        // remove all terms
        List userByTermList = annotation.getList("userByTerm");
        // just delete the specific term
        boolean hasTermsLeft = false

        // if term is NULL, delete all terms
        if (cmTermID == null) {
            // search for user in all assignments
            for (assignment in userByTermList) {
                if (user.cmID in assignment.get("user")) {
                    long cmTermID_ = Long.valueOf(assignment.get("term"))
                    cytomine.deleteAnnotationTerm(cmAnnID, cmTermID_)
                    log.debug("Deleted term: " + cmTermID_ + " from annotation [" + annotation.getId() + "]")

                    activityService.logTermDelete(user, cmProjectID, cmImageID, annotation.getId(), cmTermID_)
                }
            }
        } else {
            // search for user in all assignments
            for (assignment in userByTermList) {
                if (user.cmID in assignment.get("user")) {
                    long cmTermID_ = Long.valueOf(assignment.get("term"))
                    // if the term matches the query, remove it
                    if (cmTermID == cmTermID_) {
                        cytomine.deleteAnnotationTerm(cmAnnID, cmTermID_)
                        log.debug("Deleted term: " + cmTermID_ + " from annotation [" + annotation.getId() + "]")

                        activityService.logTermDelete(user, cmProjectID, cmImageID, annotation.getId(), cmTermID_)

                        hasTermsLeft = false
                    } else {
                        hasTermsLeft = true
                    }
                }
            }
        }

        // once the user does not have any labels left,
        // decrement the "labeledAnnotations" by 1 and re-calculate userProgress
        // then save the iris image back to the IRIS db
        if (!hasTermsLeft) {
            log.debug("User '" + user.cmUserName + "' has no other terms assigned to annotation [" + annotation.getId() + "]")

            // decrement labeled annotations (submit to background job)
            irisService.appendToDBQueue {
                syncService.decrementLabeledAnnotations(user, cmImageID)
            }


        } else {
            log.debug("User '" + user.cmUserName + "' has some other terms assigned to annotation [" + annotation.getId() + "]")
        }

        return ["hasTermsLeft": hasTermsLeft]
    }

    /**
     * Removes all existing terms (by user) and sets a <b>single</b> term to an annotation.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param cmImageID the Cytomine image ID
     * @param cmAnnID the Cytomine annotation ID
     * @param cmTermID the Cytomine term ID to be set
     * @return a map of results: AnnotationTerm, boolean flag whether the user had already a label assigned
     * @throws CytomineException if anything goes wrong with the Cytomine Java client
     * @throws Exception in any case of error
     */
    def setUniqueTermByUser(Cytomine cytomine, IRISUser user,
                                       Long cmProjectID, Long cmImageID,
                                       Long cmAnnID, Long cmTermID)
            throws CytomineException, Exception {

        AnnotationCollection annotations = getAllAnnotationsLight(cytomine, user, cmProjectID, cmImageID)
        def annList = annotations.list

        Annotation annotation
        try {
            // provoke exception if the annotation is not in the list
            annotation = annotations.get(
                    annList.indexOf(
                            annList.find {
                                ann -> ann.id == cmAnnID
                            }
                    )
            )
        } catch (Exception e) {
            // handle exception, when annotation is not in the list
            def progressInfo = syncService.computeUserProgress(annotations,user)

            IRISUserImageSettings settings
            IRISUserImageSettings.withTransaction {
                settings = IRISUserImageSettings
                        .findByUserAndCmImageInstanceID(user, cmImageID)

                settings?.setLabeledAnnotations(progressInfo['labeledAnnotations'] as Long)
                settings?.setNumberOfAnnotations(progressInfo['totalAnnotations'] as Long)
                settings?.computeProgress()

                settings?.merge(flush: true)
            }

            throw e
        }

        // check, if there is at least one term set by this user
        List userByTermList = annotation.getList("userByTerm");

        boolean hadTermsAssigned = false
        for (assignment in userByTermList) {
            if (user.cmID in assignment.get("user")) {
                hadTermsAssigned = true
                break
            }
        }

        // set the unique term to the annotation
        AnnotationTerm annTerm = cytomine.setAnnotationTerm(cmAnnID, cmTermID)

        // once the user does not have any labels left,
        // decrement the "labeledAnnotations" by 1 and re-calculate userProgress
        // then save the iris image back to the IRIS db
        if (!hadTermsAssigned) {
            log.debug("User '" + user.cmUserName + "' had no terms assigned to annotation [" + annotation.getId() + "]")
            // increment labeled annotations (submit to single-threaded background job in order to avoid locking exceptions)
            irisService.appendToDBQueue {
                syncService.incrementLabeledAnnotations(user, cmImageID)
            }
        } else {
            log.debug("User '" + user.cmUserName + "' has one or more terms assigned to annotation [" + annotation.getId() + "]")
        }

        activityService.logTermAssign(user, cmProjectID, cmImageID, annotation.getId(), cmTermID)

        return ["annotationTerm": annTerm.getAttr(), "hadTermsAssigned": hadTermsAssigned]
    }

    /**
     * This service method computes the prefetch (previous and next annotation) for the labeling view.
     *
     * @param cytomine a Cytomine client
     * @param user the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param cmImageID the Cytomine image ID
     * @param currentCmAnnID the current Cytomine annotation ID
     * @param hideCompleted a flag, whether already labeled annotations should be skipped, if this parameter is null,
     * the default settings from the image settings are taken.
     * @return a JSONObject containing the 3-tuple of annotations
     * @throws CytomineException
     * @throws Exception
     */
    JSONObject get3Tuple(Cytomine cytomine, IRISUser user,
                         Long cmProjectID, Long cmImageID,
                         Long currentCmAnnID, Boolean hideCompleted)
            throws CytomineException, Exception {

        // get the image
        IRISImage irisImage = sessionService.getImage(cytomine, user, cmProjectID, cmImageID)

        // get the default value from the settings, if not specified differently
        if (hideCompleted == null){
            hideCompleted = irisImage.settings.hideCompletedAnnotations
        }

        // fetch the terms from the ontology
        TermCollection terms = cytomine.getTermsByOntology(irisImage.projectSettings.cmOntologyID)

        // retrieve the annotations of this image
        Map<String, String> filters = new HashMap<String, String>()
        filters.put("project", String.valueOf(cmProjectID))
        filters.put("image", String.valueOf(cmImageID))
        filters.put("showWKT", "true")
        filters.put("showGIS", "true")
        filters.put("showMeta", "true")
        filters.put("showTerm", "true")

        // get all annotations from Cytomine
        AnnotationCollection annotations = cytomine.getAnnotations(filters)
        // previous call equals e.g. cytomine.doGet("/api/annotation.json?project=93519082&image=94255021")

        // if no annotations are available for this image
        if (annotations.isEmpty()) {
            throw new CytomineException(404, "No annotations found for that query.")
        }

        int queryIdx = 0
        int tupleSize = 0
        if (currentCmAnnID != null) {
            queryIdx = annotations.list.indexOf(
                    annotations.list.find {
                        ann -> ann.id == currentCmAnnID
                    }
            )
        }

        // if the list does not contain the required annotation id
        // start from the beginning and get the first element
        if (queryIdx == -1) {
            queryIdx = 0;
        }

        IRISAnnotation currIrisAnn = getCurrentAnnotation(annotations, queryIdx, hideCompleted,
                irisImage.projectSettings.cmOntologyID, terms, user, irisImage)
        // update the index (the current annotation may have changed)
        queryIdx = annotations.list.indexOf(
                annotations.list.find {
                    ann -> ann.id == currIrisAnn.cmID
                }
        )
        tupleSize++

        // find the predecessor
        IRISAnnotation predIrisAnn = getPreviousAnnotation(annotations, queryIdx, hideCompleted,
                irisImage.projectSettings.cmOntologyID, terms, user)
        if (predIrisAnn != null) {
            tupleSize++
        }

        // find the successor
        def succIrisAnn = getNextAnnotation(annotations, queryIdx, hideCompleted,
                irisImage.projectSettings.cmOntologyID, terms, user)
        if (succIrisAnn != null) {
            tupleSize++
        }

        // log the information message
        log.debug("Request: annotation " + currIrisAnn.cmID + " at index [" + queryIdx + "] has " +
                (predIrisAnn == null ? "no" : "annotation " + predIrisAnn.cmID + " as") + " predecessor, and " +
                (succIrisAnn == null ? "no" : "annotation " + succIrisAnn.cmID + " as") + " successor.")

        // add annotations to a new JSON object
        JSONObject result = new JSONObject()

        result.put("previousAnnotation", predIrisAnn) // may be null
        result.put("hasPrevious", !(predIrisAnn == null))
        result.put("currentAnnotation", currIrisAnn) // must not be null at this point
        result.put("nextAnnotation", succIrisAnn) // may be null
        result.put("hasNext", !(succIrisAnn == null))
        result.put("size", tupleSize) // number of annotations in the tuple
        result.put("hideCompleted", hideCompleted)

        result.put("currentIndex", queryIdx)

        // TODO inform the user, if an annotation got deleted
        result.put("fallback", "")

        // compute the user's progress on this image and return it in the object
        // TODO required here??
//        JSONObject progressInformation = syncService.computeUserProgress(cytomine, cmProjectID, irisImage, user)
        result.put("imageSettings", irisImage.getSettings())

        def sortedIDs = annotations.list.collect { ann-> ann.id }.sort().reverse()
        result.put("annotationIDList", sortedIDs)

        return result
    }

    /**
     * Retrieves user annotations filtered by image and terms for a specific project.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param imageIDs the Cytomine image IDs. Comma-separated string: id#1,id#2,id#3, or null, which searches in the
     * entire project.
     * @param termIDs the Cytomine term IDs. Comma-separated string: id#1,id#2,id#3, or null, which queries all terms
     * @param offset the offset for pagination
     * @param max the maximum for pagination
     * @return a JSONObject (map, containing all filtered terms where termID is the key)
     */
    JSONObject filterAnnotationsByUser(Cytomine cytomine, IRISUser irisUser,
                                       Long cmProjectID, String imageIDs,
                                       String termIDs, int offset, int max)
            throws CytomineException, Exception {

        // time the duration
        long start = System.currentTimeMillis()

        // create a new domain mapper
        DomainMapper dm = new DomainMapper(grailsApplication)

        // get all IRIS enabled images and their configuration
        List<IRISImage> images = imageService.getImages(cytomine, irisUser, cmProjectID, 0, 0)

        if (images.isEmpty()) {
            throw new CytomineException(404, "This project does not have any images.")
        }

        // ##################################################
        // default filter for a specific project/image/term
        Map<String, String> filters = new HashMap<String, String>()
        filters.put("project", String.valueOf(cmProjectID))
        filters.put("showGIS", "true") // show the centroid information on the location
        filters.put("showMeta", "true") // show the meta information
        filters.put("showTerm", "true") // show the term information

        // match query image IDs with filtered images on IRIS
        List irisEnabledImageIDs = images.collect { it.cmID }
        // convert the comma separated string to a 'Long' list
        List queryImageIDs = imageIDs.split(",").collect { Long.valueOf(it) }

        /*  ################################################################################################
            TODO BUG? IN CYTOMINE ANNOTATION FILTER
            FILTERING ALL IMAGES ALSO FILTERS THE IMAGES WHICH HAVE BEEN DELETED FROM THE PROJECT
            ACCESS CONTROL LIST IS NOT CHECKED ON IMAGE INSTANCE IDs ON THE
            "images" FILTER!!
            THIS ONLY OCCURS WHEN THE NUMBER OF QUERY IMAGES EQUALS THE NUMBER OF
            'VISIBLE' IMAGES IN THE PROJECT

            TODO TEMPORARY WORKAROUND
            PUT A NON-EXISTING IMAGE ID (e.g. '-1') ON THE IMAGE FILTER IF THE QUERY IS
            EMPTY OR THE TOTAL NUMBER OF QUERY TERMS EQUALS THE NUMBER OF 'VISIBLE' IMAGES IN THE PROJECT
         */
        if (queryImageIDs.isEmpty()) {
            // EXPLICITLY USE ALL TERMS AVAILABLE
            queryImageIDs = irisEnabledImageIDs
        } else {
            // remove the requested, but disabled images from the query
            def commons = irisEnabledImageIDs.intersect(queryImageIDs)
            if (commons.isEmpty()) throw new Exception("No known image ids on the filter!")
            def difference = irisEnabledImageIDs.plus(queryImageIDs)
            difference.removeAll(commons)
            queryImageIDs.removeAll(difference)
        }

        // AND NOOOOOW.... USE THE HACK-AROUND ;-)
        if (queryImageIDs.size() == irisEnabledImageIDs.size()) {
            queryImageIDs.add("-1")
        }

        // BUILD THE QUERY STRING
        imageIDs = queryImageIDs.join(",")
        filters.put("images", String.valueOf(imageIDs))
        // ################################################################################################

        // #####################################################################################
        // NEVER RETRIEVE NOTERM=TRUE, BECAUSE THIS DOES NOT ENSURE THAT THIS USER DOES NOT HAVE
        // YET LABELED THE ANNOTATION, BUT JUST SAYS THAT THE ANNOTATION HAS NO LABEL AT ALL!
        // #####################################################################################

        // HINT: putting no "term" on the filter map retrieves all annotations for the selected images
        boolean termsEmpty = (termIDs == null || termIDs.equals(""))
        // split the list in the params
        def queryTerms = String.valueOf(termIDs).split(",") as List

        boolean searchForNoTerm = false
        if (!termsEmpty) {
            //  check if 'no terms assigned' is on the list
            if (queryTerms.contains(IRISConstants.ANNOTATION_NO_TERM_ASSIGNED.toString())) {
                searchForNoTerm = true
                log.debug("Querying all annotations and search for 'no term' (" + IRISConstants.ANNOTATION_NO_TERM_ASSIGNED + ")...")
            } else {
                filters.put("terms", String.valueOf(termIDs))
                log.debug("Querying selected terms...")
            }
        }

        // print the filter map
        log.debug(filters)
        // get all annotations according to the filter
        AnnotationCollection annotations = cytomine.getAnnotations(filters)

        // store all filtered annotations in a map,
        // where key = termID and value = array/list of annotations
        JSONObject annotationMap = new JSONObject()
        // prepare the annotation map
        for (termID in queryTerms) {
            JSONObject termInformation = new JSONObject()
            termInformation.put("termID", Long.valueOf(termID))
            termInformation.put("annotations", new JSONArray())

            annotationMap.put(termID, termInformation)
        }
        log.debug("Finished preparation of annotation map: " + annotationMap)
        log.info("Retrieved " + annotations.size() + " annotations.")

        TermCollection terms = cytomine.getTermsByOntology(images.get(0).getProjectSettings().getCmOntologyID())
        int nAnnotations = annotations.size()

        // resolve each annotation into the annotation map
        for (int i = 0; i < nAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            log.debug("Resolving annotation " + annotation.id)

            // map the annotation to the IRIS model
            IRISAnnotation irisAnn = dm.mapAnnotation(annotation, null)

            // resolve the terms for a user according to the query terms
            resolveTermsByUser(irisAnn.cmOntologyID, terms, irisUser,
                    annotation, irisAnn, searchForNoTerm, queryTerms, annotationMap)

            // ######################################################################################
            // IMPORTANT: do NOT inject the cytomine annotation, because they contain information on
            // mappings done by other users
            // ######################################################################################
            // add the annotation to the result and use the AnnotationMarshaller in order to
            // serialize the domain objects
        }

        // for each map entry, just take the elements from offset to max and compute the page
        Set keys = annotationMap.keySet()
        for (key in keys) {
            JSONObject mapEntry = annotationMap[key]
            long termID = mapEntry["termID"]
            JSONArray anns = mapEntry["annotations"]

            int totalItems = anns.size()
            int localMax = (max == 0 ? totalItems : max)

            int pages = 0
            int currentPage = 0

            def anns_crop = []

            if (totalItems != 0 && offset < totalItems) {
                // compute the start and end index of the sub list
                int startIdx = Math.min(offset, totalItems)
                int endIdx = Math.min(totalItems, (offset + localMax))

                // get the sublist as page
                anns_crop = anns.subList(startIdx, endIdx)

                // compute pages
                pages = Math.ceil(totalItems / localMax)
                currentPage = (offset / localMax) + 1
            }

            mapEntry.put("totalItems", totalItems) // overwrite total annotations
            mapEntry.put("currentPage", currentPage) // overwrite current page
            mapEntry.put("pages", pages) // overwrite total number of pages
            mapEntry.put("annotations", anns_crop) // set the cropped array
            mapEntry.put("pageItems", anns_crop.size()) // set the cropped array
            mapEntry.put("offset", offset)
            mapEntry.put("max", localMax)
        }

        long diff = System.currentTimeMillis() - start

        // compute total number of resolved annotations
        log.info("Computation of annotation filter took " + diff + " ms.")

        return annotationMap
    }
}
