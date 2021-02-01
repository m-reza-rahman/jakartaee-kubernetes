# Jakarta EE Application Server Monitoring with Kubernetes

This demo will show how to make Kubernetes liveness/readiness probes and monitoring via Prometheus/Grafana work with MicroProfile Health and Metrics.

## Prerequisites

- You need to have a Kubernetes cluster with kubectl installed and configured to use your cluster. We used the Azure Kubernetes Service but you can use any Kubernetes capable platform including running Kubernetes locally.
- You need to have Docker CLI installed and you must be signed into your Docker Hub account. To create a Docker Hub account go to [https://hub.docker.com](https://hub.docker.com).

## Start Managed PostgreSQL on Azure
We will be using the fully managed PostgreSQL offering in Azure for this demo. If you have not set it up yet, please do so now. 

* Go to the [Azure portal](http://portal.azure.com).
* Select 'Create a resource'. In the search box, enter and select 'Azure Database for PostgreSQL'. Hit create. Select a single server.
* Specify the Server name to be jakartaee-cafe-db-`<your suffix>` (the suffix could be your first name such as "reza"). Create a new resource group named jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the login name to be postgres. Specify the password to be Secret123!. Hit 'Create'. It will take a moment for the database to deploy and be ready for use.
* In the portal, go to 'All resources'. Find and click on jakartaee-cafe-db-`<your suffix>`. Open the connection security panel. Enable access to Azure services, disable SSL connection enforcement and then hit Save.

Once you are done exploring the demo, you should delete the jakartaee-cafe-group-`<your suffix>` resource group. You can do this by going to the portal, going to resource groups, finding and clicking on jakartaee-cafe-group-`<your suffix>` and hitting delete. This is especially important if you are not using a free subscription! If you do keep these resources around (for example to begin your own prototype), you should in the least use your own passwords and make the corresponding changes in the demo code.

## Setup the Kubernetes Cluster
* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).
* You will need to create a Kubernetes cluster. Go to the [Azure portal](http://portal.azure.com). Hit Create a resource -> Containers -> Kubernetes Service. Select the resource group to be jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the cluster name as jakartaee-cafe-cluster-`<your suffix>` (the suffix could be your first name such as "reza"). Hit Review + create. Hit Create.

## Setup Kubernetes Tooling
* You will now need to setup kubectl. [Here](https://kubernetes.io/docs/tasks/tools/install-kubectl/) are instructions on how to do that.
* Next you will install the Azure CLI. [Here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) are instructions on how to do that.
* You will then connect kubectl to the Kubernetes cluster you created. To do so, run the following command:

   ```
   az aks get-credentials --resource-group jakartaee-cafe-group-<your suffix> --name jakartaee-cafe-cluster-<your suffix>
   ```
  If you get an error about an already existing resource, you may need to delete the ~/.kube directory. 

## Deploy the Jakarta EE Application on Kubernetes
* Open Eclipse.
* Do a full build of the jakartaee-cafe application via Maven by going to Right click the application -> Run As -> Maven install.
* Browse to where you have this repository code in your file system. You will now need to copy the war file to where we will build the Docker image next. You will find the war file under jakartaee/jakartaee-cafe/target. Copy the war file to monitoring/.
* You should explore the Dockerfile in this directory used to build the Docker image. It starts from the `websphere-liberty` image, adds the `jakartaee-cafe.war` from the current directory in to the `dropins` directory, copies the PostgreSqQL driver `postgresql-42.2.4.jar` into the `shared/resources` directory and replaces the defaultServer configuration file `server.xml`.
* You should note the `server.xml`. We have enabled both MicroProfile Health and Metrics.
* You should note the `jakartaee-cafe.yml`. We have added liveness and readiness probes that utilize the MicroProfile Health endpoints.
* Open a terminal. Navigate to where you have this repository code in your file system. Navigate to the monitoring/ directory.
* Log in to Docker Hub using the docker login command:
   ```
   docker login
   ```
* Build a Docker image and push the image to Docker Hub:
   ```
   docker build -t <your Docker Hub account>/jakartaee-cafe:v2 .
   docker push <your Docker Hub account>/jakartaee-cafe:v2
   ```
* Replace the `<your Docker Hub account>` value with your account name and `<your suffix>` value with what you used previously in the `jakartaee-cafe.yml` file, then deploy the application:
   ```
   kubectl create -f jakartaee-cafe.yml
   ```

* Get the External IP address of the Service, then the application will be accessible at `http://<External IP Address>/jakartaee-cafe`:
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

   ```
   docker build -t rezarahman/prometheus:v1 -f Dockerfile-prometheus .
   ```
   
   ```
   docker push rezarahman/prometheus:v1   
   ```

   ```
   docker build -t rezarahman/grafana:v1 -f Dockerfile-grafana .
   ```   
   
   ```
   docker push rezarahman/grafana:v1
   ```   

## Deploy Prometheus to Kubernetes
The next step is to get Prometheus up and running on the Kubernetes cluster so that it can begin scraping metrics from the application.
* You must first grant Prometheus the necessary cluster permissions by issuing the following command:
   ```
   kubectl apply -f prometheus-rbac.yml
   ```
* You need to deploy Prometheus by issuing the following command. You should explore the `prometheus.yml` file. It is configured to collect the MicroProfile Metrics data from the Open Liberty deployments.
   ```
   kubectl apply -f prometheus.yml
   ```
* Get the External IP address of the Prometheus service, the Prometheus UI will be accessible at `http://<External IP Address>:9090`:
   ```
   kubectl get service prometheus --watch
   ```
  It may take a few minutes for the load balancer to be created. When the external IP changes over from *pending* to a valid IP, just hit Control-C to exit.
  
## Deleting the Resources
* Delete the Prometheus deployment:
   ```
   kubectl delete -f prometheus.yml
   ```
* Remove the Prometheus permissions:
   ```
   kubectl delete -f prometheus-rbac.yml
   ```   
* Delete the Jakartaee EE deployment:
   ```
   kubectl delete -f jakartaee-cafe.yml
   ```
