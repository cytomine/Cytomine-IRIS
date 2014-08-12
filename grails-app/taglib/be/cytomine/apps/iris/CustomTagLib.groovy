package be.cytomine.apps.iris

class CustomTagLib {
	static defaultEncodeAs = 'html'
	//static encodeAsForTags = [tagName: 'raw']

	// redirect to the angular framework's index page
	def redirectIndex = {
		response.sendRedirect("${request.contextPath}/index.html")
	}
}
