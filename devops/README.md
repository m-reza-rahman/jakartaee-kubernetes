# CI/CD for Jakarta EE Applications with Kubernetes

This demo will show how to do continous integration (CI)/continous delivery (CD) of Jakarta EE applications using Kubernetes. We will use GitHub Actions for our demo but you could easily use Jenkins or any other DevOps tool. We used the Azure Kubernetes Service but you can use any Kubernetes capable platform.

## Prerequisites

* You will need a GitHub account.
* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).
* You need to have a [Docker Hub](https://hub.docker.com) account.

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

## Set Up GitHub Actions Access
* Clone this repository into your own GitHub account.
* Go to the [Azure portal](http://portal.azure.com). Go to the subscription you are using. Note down the subscription ID.
* Create a new Azure service principal by executing the following command:

   ```
   az ad sp create-for-rbac --name "jakartaee-cafe-principal" --role contributor --scopes /subscriptions/<your subscription ID>/resourceGroups/jakartaee-cafe-group-<your suffix> --sdk-auth
   ```
* Copy and save aside the JSON object for your service principal:

   ```json
   {
       "clientId": "<GUID>",
       "clientSecret": "<GUID>",
       "subscriptionId": "<GUID>",
       "tenantId": "<GUID>",
       (...)
   }  
   ```
* Go to Settings -> Secrets on your GitHub repository.
* Click 'New repository secret'. Specify the secret name to be 'AZURE_CREDENTIALS'. The Value will be the service principal JSON from above.
* Click 'New repository secret'. Specify the secret name to be 'DOCKERHUB_USERNAME'. The Value will be your Docker Hub username.
* Click 'New repository secret'. Specify the secret name to be 'DOCKERHUB_PASSWORD'. The Value will be your Docker Hub password.

## Run GitHub Actions Workflow
* Make sure to update the [devops/jakartaee-cafe.yml](jakartaee-cafe.yml) file to replace occurrences of `rezarahman` with `<Your Docker Hub ID>` and occurrences of `reza` with `<your suffix>`  on GitHub.
* Similarly, make sure to update the [.github/workflows/main.yml](../.github/workflows/main.yml) file to replace occurrences of `reza` with `<your suffix>`  on GitHub.
* Go to Actions -> Workflows -> All workflows -> Main Build -> Run workflow -> Run workflow.
* When the job finishes running, the application will be deployed to Kubernetes.
* Get the External IP address of the Service, then the application will be accessible at `http://<External IP Address>/jakartaee-cafe`:

   ```
   kubectl get svc jakartaee-cafe --watch
   ```
  It may take a few minutes for the load balancer to be created. When the external IP changes over from *pending* to a valid IP, just hit Control-C to exit.
