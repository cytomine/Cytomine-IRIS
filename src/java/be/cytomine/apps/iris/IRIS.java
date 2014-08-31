package be.cytomine.apps.iris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.models.Annotation;
import be.cytomine.client.models.User;

/**
 * 
 * @author Philipp Kainz
 *
 */
public class IRIS {

	private Cytomine cytomine;

	/**
	 * Construct a new application instance
	 * 
	 * @param cytomine
	 */
	public IRIS(Cytomine cytomine) {
		this.cytomine = cytomine;
	}

	public AnnotationCollection getRemainingAnnotations(long projectID, long imageID, long userID) throws Exception {
		User user = cytomine.getUser(userID);
		
		// define the filter for the query
		Map<String, String> filters = new HashMap<String, String>();
		filters.put("project", String.valueOf(projectID));
		filters.put("image", String.valueOf(imageID));

		// get the annotations of this image
		AnnotationCollection annotations = cytomine.getAnnotations(filters);
				
		ArrayList<JSONObject> annList = annotations.getList();
		
		// preallocate an arraylist of items to remove
		ArrayList<JSONObject> toRemove = new ArrayList<JSONObject>(annList.size());
		
		int woLabel = 0;
		int wLabel = 0;
		for (JSONObject annotation : annList) {
				// get all assignments
				List<JSONObject> assignments = (ArrayList<JSONObject>) annotation.get("userByTerm");
//				System.out.println("This annotation has " + assignments.size() + " assignments.");
				
				boolean userHasLabels = false;
				// search in each assigned term for the specific user
				for (JSONObject assignment : assignments){
//					System.out.println(assignment);
					List userList = (ArrayList) assignment.get("user");
					
					if (userList.contains(userID)){
						userHasLabels = true;
						break;
					}
				}
				if (!userHasLabels) {
//					System.out.println("no label: " + annotation);
					woLabel++;
				} else{
					toRemove.add(annotation);
				}
		}

			System.out.println(woLabel + " of " + annotations.size() + " annotations left for labeling by " + user.getStr("username"));
			annList.removeAll(toRemove);
			System.out.println("The AnnotationCollection contains " + annotations.getList().size() + " elements.");

		return annotations;
	}

	public Annotation getAnnotation(long projectID, long imageID, long annID)
			throws Exception {
		// define the filter for the query
		Map<String, String> filters = new HashMap<String, String>();
		filters.put("project", String.valueOf(projectID));
		filters.put("image", String.valueOf(imageID));
		filters.put("annotation", String.valueOf(annID));

		// get the annotations of this image
		AnnotationCollection annotations = cytomine.getAnnotations(filters);
//		for (Object annotation : annotations.getList()) {
//			System.out.println(annotation);
//		}
		// Annotation ann = cytomine.getAnnotation(annID);
		// System.out.println(JSON.parse(ann.toJSON()));
		return null;
	}

	public static void main(String[] args) throws Exception {
		Cytomine c = new Cytomine("http://beta.cytomine.be",
				"0880e4b4-fe26-4967-8169-f15ed2f9be5c",
				"a511a35c-5941-4932-9b40-4c8c4c76c7e7", "./");
		IRIS iris = new IRIS(c);
		long projectID = 93519082L;
		long imageID = 94255021L; // 100117637L 94255021L 
		long annID = 136334701L;
		long userID = 93518990L; // pkainz: 93518990L; masslaber: 107758862L

		// get a single annotation
		// iris.getAnnotation(projectID, imageID, annID);

		// get all annotations, where a specific user has not yet added a term
		iris.getRemainingAnnotations(projectID, imageID, userID);
	}

}
