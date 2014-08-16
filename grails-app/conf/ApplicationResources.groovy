modules = {
    application {
        resource url:'js/application.js'
    }
	
	favicon{
		resource id: 'favicon', url: [file: '../web-app/images/favicon.ico'], disposition: 'head'
	}
}