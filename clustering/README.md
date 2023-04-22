# Jakarta EE Application Server Clustering with Kubernetes

This demo will show how to make application server administration, clustering, auto-scaling, auto-discovery and load-balancing work with Kubernetes deployments.

## Prerequisites

- You need to have a Kubernetes cluster with kubectl installed and configured to use your cluster. We used the Azure Kubernetes Service but you can use any Kubernetes capable platform including running Kubernetes locally.
- You need to have Docker CLI installed and you must be signed into your Docker Hub account. To create a Docker Hub account go to [https://hub.docker.com](https://hub.docker.com).
- You will need to [install Helm](https://helm.sh/docs/intro/install/).

## Start Managed PostgreSQL on Azure
We will be using the fully managed PostgreSQL offering in Azure for this demo. If you have not set it up yet, please do so now. 

* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).
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
* Please delete the ~/.kube directory for good measure.
* You will then connect kubectl to the Kubernetes cluster you created. To do so, run the following command:

   ```
   az aks get-credentials --resource-group jakartaee-cafe-group-<your suffix> --name jakartaee-cafe-cluster-<your suffix>
   ```
  If you get an error about an already existing resource, you may need to delete the ~/.kube directory.
  
## Setup Ingress Controller
* Make sure to update Helm:
   ```
   helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
   helm repo update
   ```

* Create a namespace for your ingress resources by running the following command:
   ```
   kubectl create namespace ingress-basic
   ```
* Use Helm to deploy an NGINX ingress controller:
   ```
   helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress-basic --set controller.replicaCount=2 --set controller.nodeSelector."kubernetes\.io/os"=linux --set defaultBackend.nodeSelector."kubernetes\.io/os"=linux --set controller.admissionWebhooks.patch.nodeSelector."kubernetes\.io/os"=linux
   ```
* Note down the external IP address of the Ingress controller/load-balancer when it becomes available (enter CTRL-C when done):
   ```
   kubectl --namespace ingress-basic get services --watch
   ```   

## Deploy the Jakarta EE Application on Kubernetes
* Open Eclipse.
* Do a full build of the jakartaee-cafe application via Maven by going to Right click the application -> Run As -> Maven install.
* Browse to where you have this repository code in your file system. You will now need to copy the war file to where we will build the Docker image next. You will find the war file under jakartaee/jakartaee-cafe/target. Copy the war file to clustering/.
* You should explore the Dockerfile in this directory used to build the Docker image. It starts from the `websphere-liberty` image, adds the `jakartaee-cafe.war` from the current directory in to the `dropins` directory, copies the PostgreSqQL driver `postgresql-42.6.0.jar` into the `shared/resources` directory and replaces the defaultServer configuration file `server.xml`.
* You should note the `server.xml`. We have added an admin role to access the adminstrative console with. We have also enabled a database session store across the cluster.
* Open a terminal. Navigate to where you have this repository code in your file system. Navigate to the clustering/ directory.
* Log in to Docker Hub using the docker login command:
   ```
   docker login
   ```
* Build a Docker image and push the image to Docker Hub:
   ```
   docker build -t <your Docker Hub account>/jakartaee-cafe:v1 .
   docker push <your Docker Hub account>/jakartaee-cafe:v1
   ```
* Replace the `<your Docker Hub account>` value with your account name and `<your suffix>` value with what you used previously in the `jakartaee-cafe.yml` file, then deploy the application:
   ```
   kubectl create -f jakartaee-cafe.yml
   ```

* Once all the pods are ready and running, the application will be accessible at `http://<Ingress External IP Address>/jakartaee-cafe`:
   ```
   kubectl get pods --watch
   ```
* You can now also log into the administrative console using the credentials in the server.xml and administer the application by accessing `https://<Ingress External IP Address>/adminCenter`.
* Scale your application:
   ```
   kubectl scale deployment jakartaee-cafe --replicas=3
   ```
   
## Deleting the Resources
* Delete the Jakartaee EE deployment:
   ```
   kubectl delete -f jakartaee-cafe.yml
   ```
* Delete the ingress controller namespace:
   ```
   kubectl delete namespace ingress-basic
   ```
