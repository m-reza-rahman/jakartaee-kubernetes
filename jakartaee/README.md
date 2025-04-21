# Basic Jakarta EE CRUD Application
This is the basic Jakarta EE 10 application used throughout the Kubernetes demos. It is a simple CRUD application. It uses Maven and Jakarta EE 10 (REST, CDI, 
Persistence, Faces, Bean Validation).

We use Eclipse IDE but you can use any Maven capable IDE/editor such as IntelliJ or Visual Studio Code. We use Open Liberty but you should be able to use any
Jakarta EE 10 compatiple application server such as WebSphere Liberty, WildFly, JBoss EAP, GlassFish, or Payara. We use PostgreSQL but you can use any relational 
database such as MySQL, SQL Server, or Oracle DB.

## Setup

- Install JDK 17 (we used [Eclipse Temurin OpenJDK 17 LTS](https://adoptium.net/temurin/releases/?version=17)).
- Install the Eclipse IDE for Enterprise Java Developers from [here](https://www.eclipse.org/downloads/packages/).
- Install Docker for your OS.
- Download this repository somewhere in your file system (easiest way might be to download as a zip and extract).

## Database Creation
The first step to getting the application running is getting the database up. The simplest way to actually do this is through Docker. Please follow the instructions 
below to get the database running.
* Make sure Docker is running. Open a console.
* Enter the following command and wait for the database to come up fully.
```
docker run -it --rm -e POSTGRES_HOST_AUTH_METHOD=trust --name jakartaee-cafe-db -v pgdata:/var/lib/postgresql/data -p 5432:5432 postgres
```
* The database is now ready. To stop it, simply press Control-C.

## Running the Application
The next step is to get the application up and running. Follow the steps below to do so.
* Start Eclipse IDE.
* Go to Help -> Eclipse Marketplace. Search for and properly install [Liberty Tools](https://github.com/OpenLiberty/open-liberty-tools).
* Get the jakartaee-cafe application into the IDE. In order to do that, go to File -> Import -> Maven -> Existing Maven Projects. Then browse to where you have this repository code in your file system and select jakartaee/jakartaee-cafe. Accept the rest of the defaults and finish.
* Once the application loads, you should do a full Maven build by going to Right click the application -> Run As -> Maven install.
* It is now time to run the application. Right click the application -> Run As -> Liberty Start. Wait for the application to finish running.
* Once the application starts, it is available at [http://localhost:9080/jakartaee-cafe](http://localhost:9080/jakartaee-cafe).

## Content

The application is composed of:

- **A RESTFul service*:** protocol://hostname:port/jakartaee-cafe/rest/coffees

	- **_GET by Id_**: protocol://hostname:port/jakartaee-cafe/rest/coffees/{id} 
	- **_GET all_**: protocol://hostname:port/jakartaee-cafe/rest/coffees
	- **_POST_** to add a new element at: protocol://hostname:port/jakartaee-cafe/rest/coffees
	- **_DELETE_** to delete an element at: protocol://hostname:port/jakartaee-cafe/rest/coffees/{id}

- **A JSF Client:** protocol://hostname:port/jakartaee-cafe/index.xhtml

Feel free to take a minute to explore the application.
