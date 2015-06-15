# Cytomine-IRIS
[Cytomine IRIS](https://github.com/cytomine/Cytomine-IRIS/wiki#functionality-and-purpose), the Interobserver Reliability Study module, is a web-based rich internet application for blinded assessment of histopathological slide annotations. 

Watch the short getting-started tutorial on [YouTube](https://www.youtube.com/watch?v=S0PhEJmqlmA). 
[![Getting Started Tutorial](http://img.youtube.com/vi/S0PhEJmqlmA/0.jpg)](https://www.youtube.com/watch?v=S0PhEJmqlmA)

# Documentation
Please visit our [wiki pages](https://github.com/cytomine/Cytomine-IRIS/wiki) for detailed documentation on how to install, administer and use Cytomine-IRIS.

# Installation
For a more detailed guide on how to set up a production IRIS instance, please see the [Installation Guide](https://github.com/cytomine/Cytomine-IRIS/wiki/Installation-Guide) on our wiki pages.
### Prerequisites
```
- Java 7 or higher
- Apache Tomcat 7.0.x or higher (Servlet compatibility 3.0)
- Grails 2.3.11 (just for compilation)
```

### Checkout
```
$ cd ~
$ git clone https://github.com/cytomine/Cytomine-IRIS
$ cd Cytomine-IRIS
```

### Configuration
```
$ cd src/java
$ ls -l
```
There should be 3 config files, one for common config, and one for each environment *production* and *development*, which mainly contains the server configuration and mail server settings. 
Make a directory for externalized config in the home of the user which will run the tomcat server and copy the groovy files
```
$ mkdir ~/.grails 
$ cp iris-* ~/.grails 
$ cd ~/.grails
```
Now alter the configurations according to your deployment environment, server name, port, Cytomine core server connection etc. 
### Compile IRIS
```
$ cd ~/Cytomine-IRIS
$ grails war
```

### Deploy to Apache Tomcat
```
$ mv target/iris-<version>.war target/iris.war  # rename for auto-deploy with context server.domain.com/iris in tomcat
$ cp target/iris.war $CATALINA_HOME/webapps/
```

Finally run the tomcat instance and the application will be available with your custom configuration from `~/.grails/iris*-config.groovy`.
The database (H2) is created automatically and so are the server log files.

### Access the frontend
Browse to http://server.domain.com/iris to view the application front end.

## Limitations
As of version 2.5, IRIS is restricted to assign one single label per user only.
This essentially means that you should not use IRIS for projects, where users are allowed to assign labels using the Cytomine core web interface as well.
This is because once a user uses IRIS to assign a label to an annotation, all previously assigned labels are removed!

