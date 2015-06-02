package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISAnnotation
import be.cytomine.client.Cytomine
import be.cytomine.client.collections.AnnotationCollection
import be.cytomine.client.collections.UserCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.Ontology
import be.cytomine.client.models.Project
import grails.transaction.Transactional
import org.json.simple.JSONObject

/**
 * This service computes various annotation and user statistics.
 *
 */
@Transactional
class StatisticsService {

    def annotationService
    def grailsApplication

    /**
     * Compute the distribution of majority agreements on a given list of terms.
     * <p>
     *     Given a set of annotations (within one or more images),
     *     filter the agreement on specific terms.
     * </p>
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the querying IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param imageIDs the query image IDs
     * @param termIDs the query term IDs
     * @param options a map of options
     * @param offset pagination parameter
     * @param max pagination parameter
     * @return a Map object
     *
     */
    // TODO currently unused
    def getMajorityAgreement(Cytomine cytomine, IRISUser irisUser, Long cmProjectID,
                             String imageIDs, String termIDs, Map options,
                             int offset, int max) {

        // get all annotations according to a filter
        // images and terms are filtered directly
        AnnotationCollection annotations = annotationService.getAllAnnotations(
                cytomine, irisUser, cmProjectID, imageIDs, termIDs, offset, max)
        int totalAnnotations = annotations.size()

        // reset the Cytomine paging parameters
        cytomine.setOffset(0)
        cytomine.setMax(0)

        // get the cytomine project
        Project cmProject = cytomine.getProject(cmProjectID)

        // get the number of users in this project
        UserCollection projectUsers = cytomine.getProjectUsers(cmProjectID)
        int nProjectUsers = projectUsers.size()

        // get the term list for the project
        //TermCollection terms = cytomine.getTermsByOntology(cmProject.getLong("ontology"))

        Ontology ontology = cytomine.getOntology(cmProject.getLong("ontology"))
        List<JSONObject> flatOntology = new Utils().flattenOntology(ontology)

        // for each annotation, make statistics of how many users agree
        // store an n-dim vector for each term, indicating the frequency of agreeing users
        // n, n-1, n-2, ..., 1
        Map<Long, List> termAgreementStats = [:]

        // prepare the stats map
        for (int i = 0; i < flatOntology.size(); i++) {
            // initialize a list with zeros
            termAgreementStats[flatOntology[i]['id']] = [0] * nProjectUsers
        }

        // run through all annotations
        for (int i = 0; i < totalAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            // grab all terms from all users for the current annotation
            List userByTermList = annotation.getList("userByTerm")

            // search all assigned terms for the number of users
            for (assignment in userByTermList) {
                //println currentUser.get("id") + ", " + assignment.get("user")
                // get the term ID
                Long termID = assignment.get("term")

                // count how many users assigned this term
                // we don't care, who exactly assigned the terms
                int nUsers = assignment.get("user").size()

                // increase the corresponding entry in the map
                termAgreementStats[termID][nUsers] = termAgreementStats[termID][nUsers] + 1
                // all terms without assignment stay zero
            }
        }

        // TODO resolve ties, i.e. when multiple maximum exists in the votings for a term
        Set<Long> keys = termAgreementStats.keySet()
        for (key in keys) {
            // sort the array descending
            termAgreementStats[key].sort().reverse()
            if (!((boolean) options['ignoreTies'])) {
                def list = termAgreementStats[key]

                // check whether the first two elements are equal
                def hasTies
                if (list.size() >= 2 && list.max() != 0) {
                    hasTies = list[0] == list[1]
                    if (hasTies) {
                        log.debug("Multiple majority votings for term '" + key +
                                "' detected.")

                        // TODO how to resolve ties??
                    }
                }
            }
        }

        def result = [:]
        result['termAgreementStats'] = termAgreementStats
        result['terms'] = flatOntology

        return result
    }

    /**
     * Get a list of all annotations and the agreement present within given images,
     * or all images in the given project.
     *
     * @param cytomine
     * @param irisUser
     * @param cmProjectID
     * @param imageIDs
     * @param termIDs
     * @param userIDs
     * @param options
     * @param offset
     * @param max
     * @return a Map object
     */
    def getAgreementList(Cytomine cytomine, IRISUser irisUser, Long cmProjectID,
                         String imageIDs, String termIDs, String userIDs, Map options,
                         int offset, int max) {

        DomainMapper dm = new DomainMapper(grailsApplication)
        Utils utils = new Utils()

        // get all annotations according to a filter
        // images and terms are filtered directly
        AnnotationCollection annotations = annotationService.getAllAnnotations(
                cytomine, irisUser, cmProjectID, imageIDs, termIDs, offset, max)
        int totalAnnotations = annotations.size()

        cytomine.setOffset(0)
        cytomine.setMax(0)

        // get the cytomine project
        Project cmProject = cytomine.getProject(cmProjectID)

        // get the number of users in this project
        UserCollection projectUsers = cytomine.getProjectUsers(cmProjectID)

        // filter all users from the parameter list
        List allUserIDs = projectUsers.list.collect { Long.valueOf(it.id) }
        List queryUserIDs
        if (userIDs == null || userIDs.isEmpty())
            queryUserIDs = allUserIDs
        else
            queryUserIDs = userIDs.split(",").collect { Long.valueOf(it) }

        // filter all requested users
        def commons = allUserIDs.intersect(queryUserIDs)
        if (commons.isEmpty()) throw new Exception("No known user IDs on the filter!")
        def difference = allUserIDs.plus(queryUserIDs)
        difference.removeAll(commons)
        queryUserIDs.removeAll(difference)

        // the list of filtered users
        List users = projectUsers.list.findAll {
            it.id in queryUserIDs
        }

        // sort the users by lastname, firstname asc
        users = utils.sortUsersAsc(users)

        // get the ontology terms and flatten them
        Ontology ontology = cytomine.getOntology(cmProject.getLong("ontology"))
        List<JSONObject> flatOntology = utils.flattenOntology(ontology)
        def ontologyMap = flatOntology.collectEntries { [(it.id): [ 'name': it.name, 'color': it.color ]] }

        // list of annotation statistics
        def annStats = []

        def emptyUserMap = [:]
        for (u in users) {
            emptyUserMap[u['id']] = null;
        }

        // collect query term IDs in an array
        def queryTermIDs = termIDs.split(",").collect { Long.valueOf(it) }

        // the total number of users that assigned a term anywhere in this query
        def uniqueUsersOverall = [:]

        // run through all annotations
        for (int i = 0; i < totalAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            // grab all terms from all users for the current annotation
            List userByTermList = annotation.getList("userByTerm")

            // skip this annotation if there is no label assigned at all
            if (userByTermList.isEmpty())
                continue

            // map to an IRISAnnotation and convert to JSON
            IRISAnnotation irisAnn = dm.mapAnnotation(annotation, null)
            def irisAnnJSON = utils.toJSONObject(irisAnn)

            // store a map, where each user is identified by its ID
            irisAnnJSON['userStats'] = utils.deepcopy(emptyUserMap)
            irisAnnJSON['assignmentRanking'] = []

            // total number of terms assigned to this annotation
            int nAssignments = userByTermList.size()

            // record the maximum number of users that assigned anything to this term
            // and are within the query userIDs
            def uniqueUsersPerTerm = [:]

            // clone the list
            List origUserByTermList = userByTermList.collect()

            // a list of assignments to remove from this annotation
            for (int j = nAssignments-1; j>=0 ; j--) {
                def assignment = origUserByTermList[j]

                Long termID = assignment.get("term")

                // if this term is not of interest, remove this assignment
                if (!(termID in queryTermIDs)){
                    userByTermList.remove(j)
                }
            }

            // re-calculate the total assignments
            nAssignments = userByTermList.size()

            // skip the annotation, if there aren't any assignments left
            if (nAssignments == 0)
                continue

            // clone the list again
            origUserByTermList = userByTermList.collect()

            // otherwise run through the remaining assignments and filter out all users
            // that are not in the query
            for (int j = nAssignments-1; j>=0 ; j--) {

                def assignment = origUserByTermList[j]
                def userIDList = assignment.get("user")

                // filter out all users not in the query
                // by simply removing them from the array
                int nUsers = userIDList.size()

                // clone the user list
                def origUserIDList = userIDList.collect()

                // TODO count unique users after removing the ones not in the filter!!
                for (int k = nUsers-1; k>=0 ; k--) {
                    def userID = origUserIDList[k]

                    if (!(userID in queryUserIDs)) {
                        // the user is not in the query list
                        // and will be removed from this assignment
                        userIDList.remove(k)
                    } else {
                        // if the user is in the query
                        // simply overwrite the map entry for performance reasons
                        uniqueUsersPerTerm[userID] = userID
                        uniqueUsersOverall[userID] = userID
                    }
                }

                // if after filtering no-one did assign the term
                if (userIDList.isEmpty()){
                    // remove the assignment completely
                    userByTermList.remove(j)
                } else {
                    // otherwise
                    // store back the 'cleaned' user list for that term
                    assignment.putAt("user", userIDList)
                }
            }

            ///////////////////////////
            // at this point the annotations are filtered to compute the statistics
            ///////////////////////////

            // all users that assigned any term
            int maxUsers = uniqueUsersPerTerm.size()
            if (maxUsers == 0)
                continue

            // resolve and collect assignments for each user
            for (assignment in userByTermList) {

                Long termID = assignment.get("term")

                // resolve the assigned terms for each user
                def userIDList = assignment.get("user")
                for (userID in userIDList) {
                    irisAnnJSON['userStats'][userID] = termID
                }

                // compute a compressed version of the agreements on each term
                // compute the ratio as:
                //      all users that agree on this term divided by all users that assigned anything
                double agreementRatio = Math.min(userIDList.size()*1.0 / maxUsers, 1.0)
                irisAnnJSON['assignmentRanking'].add([ 'termID' : termID, 'ratio' : agreementRatio,
                                                       'totalAssignments' : nAssignments , 'nUsers' : userIDList.size(),
                                                        'maxUsers' : maxUsers,
                                                        'termName' : ontologyMap[termID].name ])
            }

            // sort 'irisAnnJSON['assignmentRanking']' field desc by ratio and then by term name
            irisAnnJSON['assignmentRanking'] = utils.sortAgreementsDesc(irisAnnJSON['assignmentRanking'])

            // add the annotation to the list
            annStats.add(irisAnnJSON)
        }

        def result = [:]
        result['annotationStats'] = annStats
        result['terms'] = ontologyMap
        result['users'] = utils.sortUsersAsc(users)
        result['nUniqueUsersOverall'] = uniqueUsersOverall.size() // the maximum number of users that assigned a term

        return result
    }

    /**
     * Compute the statistics for a given list of users.
     *
     * @param cytomine
     * @param irisUser
     * @param cmProjectID
     * @param imageIDs
     * @param termIDs
     * @param userIDs
     * @param options
     * @param offset
     * @param max
     * @return the user statistics
     */
    def getUserStatistics(Cytomine cytomine, IRISUser irisUser, Long cmProjectID,
                          String imageIDs, String termIDs, String userIDs, Map options,
                          int offset, int max) {

        Utils utils = new Utils()

        // get the project
        Project cmProject = cytomine.getProject(cmProjectID)

        // get all annotations
        AnnotationCollection annotations = annotationService.getAllAnnotationsLight(
                cytomine, irisUser, cmProjectID, imageIDs)

        // total annotations in a given image
        int totalAnnotations = annotations.size()

        // labeled annotations (empty)
        Map empty_labeledAnnotations = [:]

        // get all terms of the project ontology
        Ontology ontology = cytomine.getOntology(cmProject.getLong("ontology"))
        List<JSONObject> flatOntology = utils.flattenOntology(ontology)

        // filter the requested terms
        List allTermIDs = flatOntology.asList().collect { Long.valueOf(it['id']) }
        // filter all terms, if unspecified
        List queryTermIDs = []
        if (termIDs == null || termIDs.isEmpty())
            queryTermIDs = allTermIDs
        else
            queryTermIDs = termIDs.split(",").collect { Long.valueOf(it) }

        // prepare the labeled annotations map (will be copied to each user
        for (int i = 0; i < flatOntology.size(); i++) {
            if (allTermIDs[i] in queryTermIDs){
                // initialize the labeled annotations
                empty_labeledAnnotations[flatOntology[i]['id']] = 0
            }
        }

        // get all project users
        UserCollection projectUsers = cytomine.getProjectUsers(cmProjectID)

        // filter all users from the parameter list
        List allUserIDs = projectUsers.list.collect { Long.valueOf(it.id) }
        List queryUserIDs
        if (userIDs == null || userIDs.isEmpty())
            queryUserIDs = allUserIDs
        else
            queryUserIDs = userIDs.split(",").collect { Long.valueOf(it) }

        // filter all requested users
        def commons = allUserIDs.intersect(queryUserIDs)
        if (commons.isEmpty()) throw new Exception("No known user IDs on the filter!")
        def difference = allUserIDs.plus(queryUserIDs)
        difference.removeAll(commons)
        queryUserIDs.removeAll(difference)

        // the list of filtered users
        List users = projectUsers.list.findAll {
            it.id in queryUserIDs
        }

        // sort the users by lastname, firstname asc
        users = utils.sortUsersAsc(users)

        // each user gets its own statistics
        Map userStatistics = [:]
        for (u in users) {
            userStatistics[u.get("id")] = [:]
            userStatistics[u.get("id")]["summary"] = ["total": 0]
            userStatistics[u.get("id")]["stats"] = utils.deepcopy(empty_labeledAnnotations)
        }

        int labeledAnnotations = 0

        // count the annotations per user and term
        for (int i = 0; i < totalAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            // grab all terms from all users for the current annotation
            List userByTermList = annotation.getList("userByTerm")

            // skip empty elements
            if (userByTermList.isEmpty())
                continue

            labeledAnnotations++

            for (assignment in userByTermList) {
                // only add the annotation, if it matches one of the query terms
                if (assignment.get("term") in queryTermIDs) {
                    for (u in users) {
                        //println currentUser.get("id") + ", " + assignment.get("user")
                        if (u.get("id") in assignment.get("user")) {
                            // overall counter
                            userStatistics[u.get("id")]["summary"]["total"] =
                                    userStatistics[u.get("id")]["summary"]["total"] + 1
                            // class-wise label counter
                            userStatistics[u.get("id")]["stats"][assignment.get("term")] =
                                    userStatistics[u.get("id")]["stats"][assignment.get("term")] + 1
                        }
                    }
                }
            }
        }

        // inject the user's statistics
        for (u in users) {
            // make a list of json objects for d3 visualization
            def valueList = userStatistics[u.get("id")]['stats'].collect {
                k, v ->
                   return ([ 'label' : k , 'value' : v ])
            }
            userStatistics[u.get("id")]['stats'] = valueList
            u['userStats'] = userStatistics[u.get("id")]['stats']
            u['summary'] = userStatistics[u.get("id")]["summary"]
        }

        def result = [:]
//        result['userStats'] = userStatistics
        result['annotations'] = [ 'total': totalAnnotations, 'labeled' : labeledAnnotations ]
        result['users'] = users
//        result['terms'] = flatOntology
        result['terms'] = flatOntology.collectEntries { [(it.id): [ 'name': it.name, 'color': it.color ]] }

        return result
    }

//    /**
//     * Compute the statistics of a specific user versus all other users.
//     *
//     * @param cytomine
//     * @param irisUser
//     * @param cmProjectID
//     * @param imageIDs
//     * @param termIDs
//     * @param userID
//     * @param options
//     * @return the one-vs-all statistics
//     */
//    def oneVsAll(Cytomine cytomine, IRISUser irisUser, Long cmProjectID,
//                          String imageIDs, String termIDs, Long userID, Map options) {
//
//        Utils utils = new Utils()
//
//        // get the project
//        Project cmProject = cytomine.getProject(cmProjectID)
//
//        // get all annotations
//        AnnotationCollection annotations = annotationService.getAllAnnotationsLight(
//                cytomine, irisUser, cmProjectID, imageIDs)
//
//        // total annotations in a given image
//        int totalAnnotations = annotations.size()
//
//        // labeled annotations (empty)
//        Map empty_labeledAnnotations = [:]
//
//        // get all terms of the project ontology
//        Ontology ontology = cytomine.getOntology(cmProject.getLong("ontology"))
//        List<JSONObject> flatOntology = utils.flattenOntology(ontology)
//
//        // filter the requested terms
//        List allTermIDs = flatOntology.asList().collect { Long.valueOf(it['id']) }
//        // filter all terms, if unspecified
//        List queryTermIDs = []
//        if (termIDs == null || termIDs.isEmpty())
//            queryTermIDs = allTermIDs
//        else
//            queryTermIDs = termIDs.split(",").collect { Long.valueOf(it) }
//
//        // prepare the labeled annotations map (will be copied to each user
//        for (int i = 0; i < flatOntology.size(); i++) {
//            if (allTermIDs[i] in queryTermIDs){
//                // initialize the labeled annotations
//                empty_labeledAnnotations[flatOntology[i]['id']] = 0
//            }
//        }
//
//        // get all project users
//        UserCollection projectUsers = cytomine.getProjectUsers(cmProjectID)
//        List users = projectUsers.list

    // sort the users by lastname, firstname asc
//    users = utils.sortUsersAsc(users)
//
//        // each user gets its own statistics
//        Map userStatistics = [:]
//        for (u in users) {
//            userStatistics[u.get("id")] = [:]
//            userStatistics[u.get("id")]["summary"] = ["total": 0]
//            userStatistics[u.get("id")]["stats"] = utils.deepcopy(empty_labeledAnnotations)
//        }
//
//        // count the annotations per user and term
//        for (int i = 0; i < totalAnnotations; i++) {
//            Annotation annotation = annotations.get(i)
//            // grab all terms from all users for the current annotation
//            List userByTermList = annotation.getList("userByTerm")
//
//            // skip empty elements
//            if (userByTermList.isEmpty())
//                continue
//
//            for (assignment in userByTermList) {
//                // only add the annotation, if it matches one of the query terms
//                if (assignment.get("term") in queryTermIDs) {
//                    for (u in users) {
//                        //println currentUser.get("id") + ", " + assignment.get("user")
//                        if (u.get("id") in assignment.get("user")) {
//                            // overall counter
//                            userStatistics[u.get("id")]["summary"]["total"] =
//                                    userStatistics[u.get("id")]["summary"]["total"] + 1
//                            // class-wise label counter
//                            userStatistics[u.get("id")]["stats"][assignment.get("term")] =
//                                    userStatistics[u.get("id")]["stats"][assignment.get("term")] + 1
//                        }
//                    }
//                }
//            }
//        }
//
//        def result = [:]
//        result['userStats'] = userStatistics
//        result['users'] = users
//        result['terms'] = flatOntology
//
//        return result
//    }
}