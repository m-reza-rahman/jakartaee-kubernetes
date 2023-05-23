# Effective Kubernetes for Jakarta EE and MicroProfile Developers
This repository shows several key trade-offs to consider while using Kubernetes with Java EE, Jakarta EE and MicroProfile applications. The repository hosts the demos for [this](abstract.md) talk or [this](lab-abstract.md) lab. The prerequistes for the lab are [here](prerequisites.md). A video for the talk is available on [YouTube](https://www.youtube.com/watch?v=Q2jTk3-1Fdc).

The basic Jakarta EE application used throughout is in the [jakartaee](/jakartaee) folder. 

Factors demostrated include:

* How to make application server administration, clustering, auto-scaling, auto-discovery and load-balancing work with Kubernetes deployments. The [clustering](/clustering) folder shows how this is done.
* How to take advantage of the self-healing and monitoring capabilities of Kubernetes such as liveness/readiness probes and Prometheus/Grafana. The [monitoring](/monitoring) folder shows how this is done.
* How to use Kubernetes Operators to more effectively manage application server clusters. The [operators](/operators) folder shows how this is done.
* How your CI/CD pipeline looks like with Jakarta EE and Kubernetes. The [devops](/devops) folder shows how this is done.

The demos use Jakarta EE 8, WebSphere Liberty, PostgreSQL, Azure Kubernetes Service (AKS) and GitHub Actions.

## To Do
* Add optional local verification step
* Add CRIU?
* Use Prometheus Operator?
* Use Grafana Operator?
