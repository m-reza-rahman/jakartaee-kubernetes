# Jakarta EE Application Server Monitoring with Kubernetes

This demo will show how to make Kubernetes liveness/readiness probes and monitoring via OpenTelemetry logs/tracing/metrics work with MicroProfile Health and Telemetry. We use Azure Monitor/App Insights for the demos but you could use Prometheus, Grafana, Jaeger, or any other tool that works with OpenTelemetry.

## Prerequisites

* You will need to have [Maven installed](https://maven.apache.org/install.html).
* You need to have a Kubernetes cluster with kubectl installed and configured to use your cluster. We used the Azure Kubernetes Service but you can use any Kubernetes capable platform including running Kubernetes locally.
* You need to have Docker CLI installed and you must be signed into your Docker Hub account. To create a Docker Hub account go to [https://hub.docker.com](https://hub.docker.com).
* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).

## Start Managed PostgreSQL on Azure
We will be using the fully managed PostgreSQL offering in Azure for this demo. If you have not set it up yet, please do so now. 

* Go to the [Azure portal](http://portal.azure.com).
* Select 'Create a resource'. In the search box, enter and select 'Azure Database for PostgreSQL Flexible Server'. Hit create.
* Create a new resource group named jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the server name to be jakartaee-cafe-db-`<your suffix>` (the suffix could be your first name such as "reza"). Choose PostgreSQL authentication only. Specify the login name to be postgres. Specify the password to be Secret123!. Click next to go to Networking. Enable access from Azure services. Add the current client IP address. Hit 'Create'. It will take a moment for the database to deploy and be ready for use.

Once you are done exploring the demo, you should delete the jakartaee-cafe-group-`<your suffix>` resource group. You can do this by going to the portal, going to resource groups, finding and clicking on jakartaee-cafe-group-`<your suffix>` and hitting delete. This is especially important if you are not using a free subscription! If you do keep these resources around (for example to begin your own prototype), you should in the least use your own passwords and make the corresponding changes in the demo code.

## Setup the Kubernetes Cluster
You will now need to create a Kubernetes cluster if you have not done so yet.

* Go to the [Azure portal](http://portal.azure.com).
* Hit Create a resource -> Compute -> Azure Kubernetes Service (AKS). Select the resource group to be jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the cluster name as jakartaee-cafe-cluster-`<your suffix>` (the suffix could be your first name such as "reza"). Hit Review + create. Hit Create.

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

## Deploy the Jakarta EE Application on Kubernetes
* Browse to where you have this repository code in your file system. Go into the monitoring/jakartaee-cafe directory. Do a full build of the jakartaee-cafe application via Maven:
   ```
   mvn clean package
   ```
* Go back to the monitoring/ directory. You should explore the Dockerfile in this directory used to build the Docker image. It starts from the `open-liberty` Java 17 image, adds the `jakartaee-cafe.war` file to the `dropins` directory, copies the PostgreSqQL driver into the `shared/resources` directory and replaces the defaultServer configuration file `server.xml`.
* You should note the `server.xml`. We have enabled MicroProfile Health.
* You should note the `jakartaee-cafe.yml`. We have added liveness and readiness probes that utilize the MicroProfile Health endpoints.
* Open a terminal. Navigate to where you have this repository code in your file system. Navigate to the monitoring/ directory.
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

## Deploy Prometheus/Grafana Dashboard to Kubernetes
The next step is to get Prometheus/Grafana up and running on the Kubernetes cluster so you can view a dashboard with the metrics from the application.

* You must first build the custom Prometheus image and push the image to Docker Hub by issuing the following commands. You should explore the `prometheus.yml` file referenced in the Docker build. It is configured to collect the MicroProfile Metrics data from the Open Liberty deployments.
   ```
   docker build -t <your Docker Hub account>/prometheus:v1 -f Dockerfile-prometheus .
   docker push <your Docker Hub account>/prometheus:v1
   ```

* You must then build the custom Granafa image and push the image to Docker Hub by issuing the following commands. The Grafana image is configured with Prometheus as a data source as well as an Open Liberty dashboard that works with MicroProfile Metrics.
   ```
   docker build -t <your Docker Hub account>/grafana:v1 -f Dockerfile-grafana .
   docker push <your Docker Hub account>/grafana:v1
   ```   
* You need to deploy Prometheus and Grafana by issuing the following command. Please replace the `<your Docker Hub account>` value with your account name in the `jakartaee-cafe-dashboard.yml` file before issuing the command.
   ```
   kubectl apply -f jakartaee-cafe-dashboard.yml
   ```
* Get the External IP address of the Grafana service. The Grafana UI, including the provisioned dashboard will be accessible at `http://<External IP Address>:3000`:
   ```
   kubectl get service grafana --watch
   ```
  It may take a few minutes for the load balancer to be created. When the external IP changes over from *pending* to a valid IP, just hit Control-C to exit.

* You can also explore the Prometheus UI. Get the External IP address of the Prometheus service to do this. The Prometheus UI will be accessible at `http://<External IP Address>:9090`:
   ```
   kubectl get service prometheus --watch
   ```
  It may take a few minutes for the load balancer to be created. When the external IP changes over from *pending* to a valid IP, just hit Control-C to exit.

## Deleting the Resources
* Delete the Prometheus/Grafana deployment:
   ```
   kubectl delete -f jakartaee-cafe-dashboard.yml
   ```

* Delete the Jakartaee EE deployment:
   ```
   kubectl delete -f jakartaee-cafe.yml
   ```
