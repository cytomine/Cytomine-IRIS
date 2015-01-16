package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISImage
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.AnnotationCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.Ontology

import org.apache.log4j.Logger
import org.json.simple.parser.JSONParser


/**
 * Utility class for the Cytomine IRIS application.
 * @author Philipp Kainz
 * @since 0.1
 */
class Utils {

    Logger log = Logger.getLogger(Utils.class)

    /**
     * Computes the annotation progress of a user in an image. Each
     * annotation has to have at least one term assigned by the user,
     * otherwise this does not attribute to the progress.
     *
     * @param cytomine a Cytomine instance
     * @param projectID the cytomine project id
     * @param imageID the cytomine image id
     * @param userID the cytomine user id
     * @return a JSONObject (map) which contains progress information for the user
     */
    JSONObject getUserProgress(Cytomine cytomine, long projectID, long imageID, long userID) {
        JSONObject jsonResult = new JSONObject()
        // clone the object and retrieve every object without pagination
        Cytomine cm = new Cytomine(cytomine.host, cytomine.publicKey, cytomine.privateKey, cytomine.basePath)

        int totalAnnotations = 0
        int labeledAnnotations = 0

        // define the filter for the query
        Map<String, String> filters = new HashMap<String, String>()
        filters.put("project", String.valueOf(projectID))
        filters.put("image", String.valueOf(imageID))

        // get the annotations of this image (as batch, causes 1 access per image)
        AnnotationCollection annotations = cm.getAnnotations(filters)

        // total annotations in a given image
        totalAnnotations = annotations.size();

        // count the annotations per user
        for (int i = 0; i < totalAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            // grab all terms from all users for the current annotation
            List userByTermList = annotation.getList("userByTerm");
            for (assignment in userByTermList) {
                List userList = assignment.get("user").toList()

                // if the user has assigned a label to this annotation, increase the counter
                if (userID in userList) {
                    labeledAnnotations++
                }
            }
        }

        jsonResult.put("projectID", projectID)
        jsonResult.put("imageID", imageID)
        jsonResult.put("labeledAnnotations", labeledAnnotations)
        jsonResult.put("numberOfAnnotations", totalAnnotations)
        // compute the progress in percent
        int userProgress = (totalAnnotations == 0 ? 0 : (int) ((labeledAnnotations / totalAnnotations) * 100));
        jsonResult.put("userProgress", userProgress)

        // return the json result
        return jsonResult;
    }

    /**
     * Flattens an ontology, which may be hierarchical.
     *
     * @param ontology
     * @return a list of JSONObjects
     */
    List<JSONObject> flattenOntology(Ontology ontology) {
        // perform recursion and flatten the hierarchy
        JSONObject root = ontology.getAttr();
        List flatHierarchy = new ArrayList<JSONObject>();

        // build a lookup table for each term in the annotation
        Map<Long, Object> dict = new HashMap<Long, Object>();

        // pass the root node
        flatHelper(root, dict, flatHierarchy);

        return flatHierarchy;
    }

    /**
     * Flattens a hierarchy, where the parent node is an attribute of each child node.
     *
     * @param node the node (mostly root node)
     * @param dict the dictionary to map from
     * @param flatHierarchy the flat hierarchy to write to
     */
    private void flatHelper(JSONObject node, Map<Long, Object> dict, List flatHierarchy) {
        // get the node's children
        List childrenList = node.get("children").toList()

        // recurse through the children
        for (child in childrenList) {
            // put each node to the dictionary
            dict.put(Long.valueOf(child.get("id")), child);

            if (child.get("isFolder")) {
                flatHelper(child, dict, flatHierarchy);
            } else {
                String parentName = "root";

                if (child.get("parent") != null) {
                    // these are the non-root elements
                    parentName = dict.get(Long.valueOf(child.get("parent"))).get("name")
                }

                // add the child to the flat ontology
                child.put("parentName", parentName)
                flatHierarchy.add(child);
            }
        }
    }

    /**
     * Converts a domain object to JSON format using the custom marshaller classes.
     * @param object
     * @return the JSONElement object
     */
    JSONElement modelToJSON(def object) {
        return JSON.parse((object as JSON).toString())
    }

    JSONObject toJSONObject(def object) {
        JSONParser parser = new JSONParser()
        StringReader objectReader = new StringReader((object as JSON).toString())
        JSONObject result = (JSONObject) parser.parse(objectReader)
        if (result.isEmpty())
            return null
        else
            return result
    }

    /**
     * Gets the predecessor annotation from a collection.
     *
     * @param annotations the collection
     * @param currentIndex the current index
     * @return
     * @throws IndexOutOfBoundsException if the current index is already the beginning of the collection
     */
    Annotation getPredecessor(
            AnnotationCollection annotations,
            int currentIndex)
            throws IndexOutOfBoundsException {
        return annotations.get(currentIndex - 1)
    }

    /**
     * Gets the successor annotation from a collection.
     *
     * @param annotations the collection
     * @param currentIndex the current index
     * @return
     * @throws IndexOutOfBoundsException if the current index is already the end of the collection
     */
    Annotation getSuccessor(
            AnnotationCollection annotations,
            int currentIndex)
            throws IndexOutOfBoundsException {
        return annotations.get(currentIndex + 1)
    }

    /**
     * Compute split indices for a list of objects.
     *
     * @param theList
     * @param nParts
     * @return an array constisting of split indices
     */
    def getSplitIndices(def theList, int nParts) {
        def nItems = theList.size()
        def splitIndices;

        if (nItems == 0) {
            splitIndices = []
        } else if (nParts == 1 || nItems == 1) {
            splitIndices = [nItems - 1]
        } else {
            int maxSplits = nParts - 1;
            // determine the number of parts
            if (nItems <= nParts) {
                // limit the number of parts by the number of items
                nParts = nItems
                maxSplits = nParts
            }

            int nElementsPerPart = Math.round(nItems / nParts)
            //assert nParts == new Double(Math.round(nItems / nElementsPerPart)).intValue()

            // split the list in subparts
            splitIndices = (1..maxSplits).collect { (it * nElementsPerPart) - 1 }
            log.debug("max elements per part: " + nElementsPerPart + ", parts: " + nParts + ", maxSplits: " + maxSplits)
        }
        log.debug("      --> Split indices: " + splitIndices)

        return splitIndices
    }

    /**
     * Resolve a CytomineException object to JSON data, which can be rendered to the client.
     * The status code is inherently stored in the CytomineException object.
     *
     * @param e the exception
     * @return a JSONObject
     */
    JSONObject resolveCytomineException(CytomineException e) {
        JSONObject errorMsg = new JSONObject()
        errorMsg.putAt("class", CytomineException.class.getName())
        errorMsg.putAt("error", new JSONObject())
        org.codehaus.groovy.grails.web.json.JSONObject msgObj
        String msg
        try {
            msgObj = new org.codehaus.groovy.grails.web.json.JSONObject(e.toString().replace(e.httpCode + " ", ""))
            msg = msgObj.getString("message")
        } catch (Exception ex) {
            msg = e.toString().replace(e.httpCode + " ", "")
        }
        errorMsg.getAt("error").putAt("message", msg)
        errorMsg.getAt("error").putAt("status", e.httpCode)
        return errorMsg
    }

    /**
     * Resolve an Exception object to a HTTP status code which can be rendered to the client as JSON.
     * @param e the exception
     * @param httpCode the status code
     * @return a JSONObject
     */
    JSONObject resolveException(Exception e, int httpCode) {
        JSONObject errorMsg = new JSONObject()
        errorMsg.putAt("class", e.getClass().getName())
        org.codehaus.groovy.grails.web.json.JSONObject errorObj
        try {
            String msg = e.getMessage() == null ? "The requested operation cannot be performed." : e.getMessage()
            errorObj = new org.codehaus.groovy.grails.web.json.JSONObject("{ status : " +
                    httpCode + ", message : \"" + msg + "\" }")
        } catch (Exception ex) {
            errorObj = new org.codehaus.groovy.grails.web.json.JSONObject("{ status : " +
                    httpCode + ", message : \"The requested operation cannot be performed.\" }")
        }
        errorMsg.putAt("error", errorObj)
        return errorMsg
    }
}
