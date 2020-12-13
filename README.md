# Effective Kubernetes for Jakarta EE and MicroProfile Developers
This repository shows several key trade-offs to consider while using Kubernetes with Java EE, Jakarta EE and MicroProfile applications. The repository hosts the demos for [this](abstract.md) talk or [this](lab-abstract.md) lab.

The basic Jakarta EE application used throughout is in the [jakartaee](/jakartaee) folder. 

Factors demostrated include:

* How to make application server administration, clustering, auto-scaling, auto-discovery and load-balancing work with Kubernetes deployments. The [clustering](/clustering) folder shows how this is done.
* How your CI/CD pipeline looks like with Jakarta EE and Kubernetes. The [devops](/devops) folder shows how this is done.

The demos use Jakarta EE 8, WebSphere Liberty, Azure Kubernetes Service (AKS) and Azure DevOps Pipelines.
