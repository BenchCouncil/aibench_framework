The AIBench framework provides an universal AI benchmark framework that is flexible and configurable. It provides loosely coupled modules that can be easily configured and extended to compose an end-to-end application, including the data input, AI problem domain, online inference, offline training, and deployment tool modules.

To achieve diversity and representativeness of our framework, we first
identify prominent AI problem domains that play important roles in most
important Internet services domains. And then we provide the concrete
implementation of the AI algorithms targeting those AI problem domains as
component benchmarks. Also, we profile the most intensive units of computation
across those component benchmarks, and implement them as
a set of micro benchmarks. Both micro and component benchmarks are
implemented with the concern for composability, each of which can run
collectively and individually.

The offline training and online inference modules are provided to construct an
end-to-end application benchmark. First, the offline training module chooses
one or more component benchmarks from the AI problem domain module, through
specifying the required benchmark ID, input data, and execution parameters
like batch size. Then the offline training module trains a model and provides
the trained model to the online inference module. The online inference
module loads the trained model onto the serving system, i.e., TensorFlow
serving. Collaborating with the other non AI-related modules in the critical
paths, an end-to-end application benchmark is built.

To be easily deployed on a large-scale cluster, the framework provides
deployment tools that contain two automated deployment templates using Ansible. Among them, the Ansible templates support
and Kubernetes, respectively. Among them, the Ansible templates support
scalable deployment on physical machines or virtual machines. A configuration
kubernetes templates are used to deploy on container clusters. A configuration
file needs to be specified for installation and deployment, including module
parameters like the chosen benchmark ID, input data, and the cluster
parameters like nodes, memory, and network information.
