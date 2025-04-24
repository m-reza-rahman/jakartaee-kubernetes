# Jakarta EE Application Server Monitoring with Kubernetes

This demo will show how to make Kubernetes liveness/readiness probes and monitoring via OpenTelemetry logs/tracing/metrics work with MicroProfile Health and Telemetry. We use Azure App Insights for the demos but you could use Prometheus, Grafana, Jaeger, or any other tool that works with OpenTelemetry.

## Prerequisites

* You will need to have [Maven installed](https://maven.apache.org/install.html).
* You need to have a Kubernetes cluster with kubectl installed and configured to use your cluster. We used the Azure Kubernetes Service but you can use any Kubernetes capable platform including running Kubernetes locally.
* You need to have Docker CLI installed and you must be signed into your Docker Hub account. To create a Docker Hub account go to [https://hub.docker.com](https://hub.docker.com).
* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).

## Start Managed PostgreSQL on Azure
We will be using the fully managed PostgreSQL offering in Azure for this demo. If you have not set it up yet, please do so now. 

* Go to the [Azure portal](http://portal.azure.com).
* Select 'Create a resource'. In the search box, enter and select 'Azure Database for PostgreSQL Flexible Server'. Hit create.
* Create a new resource group named jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the server name to be jakartaee-cafe-db-`<your suffix>` (the suffix could be your first name such as "reza"). Choose PostgreSQL authentication only. Specify the login name to be postgres. Specify the password to be Secret123!. Click next to go to Networking. Enable access from Azure services. Add the current client IP address. Hit 'Review + create'. Hit 'Create'. It will take a moment for the database to deploy and be ready for use.

Once you are done exploring the demo, you should delete the jakartaee-cafe-group-`<your suffix>` resource group. You can do this by going to the portal, going to resource groups, finding and clicking on jakartaee-cafe-group-`<your suffix>` and hitting delete. This is especially important if you are not using a free subscription! If you do keep these resources around (for example to begin your own prototype), you should in the least use your own passwords and make the corresponding changes in the demo code.

## Setup the Kubernetes Cluster
You will now need to create a Kubernetes cluster if you have not done so yet.

* Go to the [Azure portal](http://portal.azure.com).
* Hit Create a resource -> Compute -> Azure Kubernetes Service (AKS). Select the resource group to be jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the cluster name as jakartaee-cafe-cluster-`<your suffix>` (the suffix could be your first name such as "reza"). Hit 'Review + create'. Hit Create.

## Setup Kubernetes Tooling
* You will now need to setup kubectl. [Here](https://kubernetes.io/docs/tasks/tools/install-kubectl/) are instructions on how to do that.
* Next you will install the Azure CLI. [Here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) are instructions on how to do that.
* Please delete the ~/.kube directory for good measure.
* Log into Azure:

   ```
   az login
   ```
* You will then connect kubectl to the Kubernetes cluster you created. To do so, run the following command:

   ```
   az aks get-credentials --resource-group jakartaee-cafe-group-<your suffix> --name jakartaee-cafe-cluster-<your suffix>
   ```
  If you get an error about an already existing resource, you may need to delete the ~/.kube directory. 

## Set up Monitoring Tool
You will now set up Azure App Insights.

* Go to the [Azure portal](http://portal.azure.com).
* Hit Create a resource -> Monitoring & Diagnostics -> Application Insights. Select the resource group to be jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the instance name as jakartaee-cafe-insights-`<your suffix>` (the suffix could be your first name such as "reza"). Hit 'Review + create'. Hit 'Create'.
* In the portal, go to 'All resources'. Find and click on jakartaee-cafe-insights-`<your suffix>`. Note down the connection string in the overview panel.

## Set up OpenTelemetry Collector
The next step is to get OpenTelemetry Collector set up on the Kubernetes cluster so you can view logs, metrics and traces forwarded to App Insights.

* You must first build the custom OpenTelemetry Collector image and push the image to Docker Hub by issuing the following commands. You should explore the `otel-collector-config.yml` file referenced in the Docker build. It is configured to collect data from the Open Liberty deployments and send it to Log Analytics/App Insights.
   ```
   docker build -t <your Docker Hub account>/otel-collector:v1 -f Dockerfile-otel-collector .
   docker push <your Docker Hub account>/otel-collector:v1
   ```
* You need to deploy the OpenTelemetry Collector by issuing the following command. Please replace the `<your Docker Hub account>` value with your account name in the `otel-collector.yml` file, as well the `<your App Insights connection string>` value with the connection string you noted down earlier, before issuing the command.
   ```
   kubectl apply -f otel-collector.yml
   ```

## Deploy the Jakarta EE Application on Kubernetes
* Open a terminal. Browse to where you have this repository code in your file system. Go into the monitoring/jakartaee-cafe directory. Do a full build of the jakartaee-cafe application via Maven:
   ```
   mvn clean package
   ```
* Go back to the monitoring/ directory. You should explore the Dockerfile in this directory used to build the Docker image. It starts from the `open-liberty` Java 17 image, adds the `jakartaee-cafe.war` file to the `dropins` directory, copies the PostgreSqQL driver into the `shared/resources` directory and replaces the defaultServer configuration file `server.xml`.
* You should note the `server.xml`. We have enabled MicroProfile Health and Telemetry.
* You should note the `jakartaee-cafe.yml`. We have added liveness and readiness probes that utilize the MicroProfile Health endpoints. We have also added the configuration needed for the OpenTelemetry Collector.
* Log in to Docker Hub using the docker login command:
   ```
   docker login
   ```
* Build a Docker image:
   ```
   docker build -t <your Docker Hub account>/jakartaee-cafe:v2 .
   ```

* Test the Docker image locally using the following command. Once the application starts, it is available at [http://localhost:9080/jakartaee-cafe](http://localhost:9080/jakartaee-cafe). To exit, simply press Control-C.
   ```
   docker run -it --rm -p 9080:9080 -p 9443:9443 -e POSTGRES_SERVER="jakartaee-cafe-db-<your suffix>.postgres.database.azure.com" -e POSTGRES_USER="postgres" -e POSTGRES_PASSWORD="Secret123!" <your Docker Hub account>/jakartaee-cafe:v2
   ```

* Push the Docker image to Docker Hub:
   ```
   docker push <your Docker Hub account>/jakartaee-cafe:v2
   ```

* Replace the `<your Docker Hub account>` value with your account name and `<your suffix>` value with what you used previously in the `jakartaee-cafe.yml` file, then deploy the application:
   ```
   kubectl create -f jakartaee-cafe.yml
   ```
* Wait for all the pods to be ready and running:
   ```
   kubectl get pods --watch
   ```
* Get the External IP address of the service, then the application will be accessible at `http://<External IP Address>/jakartaee-cafe`:
   ```
   kubectl get svc jakartaee-cafe --watch
   ```
  It may take a few minutes for the load balancer to be created. When the external IP changes over from *pending* to a valid IP, just hit Control-C to exit.

   > **Note:** Use the command below to find the assigned IP address and port if you are running Kubernetes locally on `Minikube`:

 	```
 	minikube service jakartaee-cafe --url
 	```
* Scale your application:
   ```
   kubectl scale deployment jakartaee-cafe --replicas=3
   ```

## Monitoring the Application
In the portal, go to 'All resources'. Find and click on jakartaee-cafe-insights-`<your suffix>`. You can navigate to:

* Investigate > Application map - shows the application components and their dependencies.
* Investigate > Failures - shows the failures and exceptions in the application.
* Investigate > Performance - shows the performance of the application.
* Monitoring > Metrics - shows the metrics of the application including Kubernetes, Open Liberty, and JVM metrics.
* Monitoring > Logs - shows the logs and traces of the application.

## Deleting the Resources
* Delete the Jakartaee EE deployment:
   ```
   kubectl delete -f jakartaee-cafe.yml
   ```
* Delete the OpenTelemetry Collector deployment:
   ```
   kubectl delete -f otel-collector.yml
   ```
