package be.cytomine.apps.iris.model

import be.cytomine.apps.iris.IRISUserImageSettings
import be.cytomine.apps.iris.IRISUserProjectSettings

class IRISImage {
    // Cytomine stuff
    Long cmID = 0L
    Long cmProjectID = 0L
    Long cmCreated = 0L
    Long cmBaseImageID = 0L
    Boolean cmReviewed = false
    Boolean cmInReview = false

    Long magnification = 0L
    Double resolution = 0.0d
    Long depth = 0L

    String originalFilename = "defaultIRISImage"
    String instanceFilename = "defaultIRISImage"

    String goToURL = null
    String olTileServerURL = null
    String macroURL = null
    String mime = null

    Long width = 0
    Long height = 0

    Long numberOfAnnotations = 0L

    /**
     * Custom settings (injected for each user on request).
     */
    IRISUserImageSettings settings

    /**
     * The project settings for the parent project.
     */
    IRISUserProjectSettings projectSettings
}
