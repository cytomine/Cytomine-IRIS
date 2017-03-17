
When using our software, we kindly ask you to cite our website url and related publications in all your work (publications, studies, oral presentations,...). In particular, we recommend to cite (Marée et al., Bioinformatics 2016) paper, and to use our logo when appropriate. See our license files for additional details.

- URL: http://www.cytomine.be/
- Logo: http://www.cytomine.be/logo/logo.png
- Scientific paper: Raphaël Marée, Loïc Rollus, Benjamin Stévens, Renaud Hoyoux, Gilles Louppe, Rémy Vandaele, Jean-Michel Begon, Philipp Kainz, Pierre Geurts and Louis Wehenkel. Collaborative analysis of multi-gigapixel imaging data using Cytomine, Bioinformatics, DOI: 10.1093/bioinformatics/btw013, 2016. http://bioinformatics.oxfordjournals.org/cgi/content/abstract/btw013?ijkey=dQzEgmXVozFRPPf&keytype=ref 



# Cytomine-IRIS
[Cytomine IRIS](https://github.com/cytomine/Cytomine-IRIS/wiki#functionality-and-purpose), the **I**nter-observer **R**el**i**ability **S**tudy module, is a web-based rich internet application for blinded assessment of histopathological slide annotations. 

# Getting Started
Once IRIS is installed, visit the [Getting Started with IRIS User Guide](https://github.com/cytomine/Cytomine-IRIS/wiki/Getting-Started-with-IRIS) on the wiki pages to get a short introduction. You are also welcome to watch the short getting-started tutorial on [YouTube](https://www.youtube.com/watch?v=S0PhEJmqlmA). 
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
Now alter the configurations according to your deployment environment, server name, port, Cytomine core server connection etc. Find detailed instructions [here](https://github.com/cytomine/Cytomine-IRIS/wiki/IRIS-Host-Configuration).

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

# Bug Reports
Please file any Cytomine-IRIS related bugs or issues here in this [Github issue tracker](https://github.com/cytomine/Cytomine-IRIS/issues). We will do our best to fix them as soon as possible.
