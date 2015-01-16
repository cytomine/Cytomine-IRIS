package be.cytomine.apps.iris.model

import be.cytomine.apps.iris.IRISUserProjectSettings

class IRISProject {
    // class members
    Long cmID = 0L
    String cmName = "defaultProject"
    Boolean cmBlindMode = false
    Long cmOntologyID = 0L
    String cmOntologyName = "defaultOntology"
    Long cmDisciplineID = 0L
    String cmDisciplineName = null
    Long cmCreated = null
    Long cmUpdated = null

    Boolean isReadOnly = false
    Boolean isClosed = false
    Boolean hideAdminsLayers = false
    Boolean hideUsersLayers = false
    Boolean retrievalDisable = false
    Boolean retrieveAllOntology = false

    Long cmNumberOfAnnotations = 0L
    Long cmNumberOfImages = 0

    /**
     * Custom settings (injected for each user on request).
     */
    IRISUserProjectSettings settings
}
