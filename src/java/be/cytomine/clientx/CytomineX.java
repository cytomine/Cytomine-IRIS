package be.cytomine.clientx;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.HttpClient;
import be.cytomine.client.models.AnnotationTerm;
import be.cytomine.client.models.Model;

public class CytomineX extends Cytomine {
	
	private static final Logger log = Logger.getLogger(CytomineX.class);

	public CytomineX(String host, String publicKey, String privateKey,
			String workingPath) {
		super(host, publicKey, privateKey, workingPath);
	}

	public CytomineX(String host, String publicKey, String privateKey,
			String workingPath, boolean isBasicAuth) {
		super(host, publicKey, privateKey, workingPath, isBasicAuth);
	}

	public AnnotationTerm setAnnotationTerm(Long idAnnotation, Long idTerm) throws Exception{
		AnnotationTerm annotationTerm = new AnnotationTerm();
		annotationTerm.set("userannotation", idAnnotation);
		annotationTerm.set("annotationIdent", idAnnotation);
		annotationTerm.set("term", idTerm);

		Model model = annotationTerm;

		String clearBeforeURL = "/api/annotation/" + idAnnotation + "/term/"
				+ idTerm + "/clearBefore.json";

		HttpClient client = null;
		if (!isBasicAuth) {
			client = new HttpClient(publicKey, privateKey, host);
			client.authorize("POST", clearBeforeURL, "", "application/json,*/*");
			client.connect(host + clearBeforeURL);
		} else {
			client = new HttpClient();
			client.connect(host + clearBeforeURL, login, pass);
		}
		int code = client.post(model.toJSON());
		String response = client.getResponseData();
		client.disconnect();
		JSONObject json = createJSONResponse(code, response);
		analyzeCode(code, json);
		model.setAttr((JSONObject) json.get(model.getDomainName()));
		return (AnnotationTerm) model;
	}
	
	private JSONObject createJSONResponse(int code, String response) throws Exception {
        try {
            Object obj = JSONValue.parse(response);
            JSONObject json = (JSONObject) obj;
            return json;
        } catch (Exception e) {
            log.error(e);
            throw new CytomineException(code, response);
        } catch (Error ex) {
            log.error(ex);
            throw new CytomineException(code, response);
        }
    }
	
	private void analyzeCode(int code, JSONObject json) throws Exception {

        if (code == 200 || code == 201 || code == 304) return;
        if (code == 400) throw new CytomineException(code, json);
        if (code == 401) throw new CytomineException(code, json);
        if (code == 404) throw new CytomineException(code, json);
        if (code == 500) throw new CytomineException(code, json);
        if (code == 302) throw new CytomineException(code, json);
    }
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		CytomineX cX = new CytomineX("http://beta.cytomine.be",
				"0880e4b4-fe26-4967-8169-f15ed2f9be5c",
				"a511a35c-5941-4932-9b40-4c8c4c76c7e7", "./");
		long projectID = 93519082L;
		long imageID = 94255021L; // 100117637L 94255021L 
		long annID = 136334701L;
		long termID = 95202360L; // 95202366L 95202360L
		long userID = 93518990L; // pkainz: 93518990L; masslaber: 107758862L
		
		try {
			//AnnotationTerm aT = cX.setAnnotationTerm(annID, termID);
			//System.out.println(aT.toJSON());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Cytomine c = new Cytomine("http://beta.cytomine.be",
				"0880e4b4-fe26-4967-8169-f15ed2f9be5c",
				"a511a35c-5941-4932-9b40-4c8c4c76c7e7", "./");
		cX = (CytomineX) c;
	}

}
