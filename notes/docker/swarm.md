---
title: JLibs
layout: default
---

# Docker Swarm

Docker Swarm is native clustering for Docker. It turns a pool of Docker hosts into a single, virtual Docker host.  
Because Docker Swarm serves the standard Docker API, any tool that already communicates with a Docker daemon can use Swarm to transparently scale to multiple hosts.

you can run swarm from docker image.

~~~shell
$ docker run swarm --help
Usage: swarm [OPTIONS] COMMAND [arg...]

A Docker-native clustering system

Version: 1.1.3 (7e9c6bd)

Options:
  --debug           debug mode [$DEBUG]
  --log-level, -l "info"    Log level (options: debug, info, warn, error, fatal, panic)
  --experimental        enable experimental features
  --help, -h            show help
  --version, -v         print the version

Commands:
  create, c Create a cluster
  list, l   List nodes in a cluster
  manage, m Manage a docker cluster
  join, j   join a docker cluster
  help      Shows a list of commands or help for one command

Run 'swarm COMMAND --help' for more information on a command.
~~~

## Swarm Manager

`manage` command is used to run swarm manager.

~~~shell
$ docker run -d swarm manage [OPTIONS] <discovery>
~~~

By default swarm manager listens on port 2375 on all interfaces. Normally we do port mapping for this. To change it:  
`-H tcp://<swarm-ip>:<swarm-port>` or `-H :<swarm-port>`

You can connect docker client to `<swarm-ip>:<swarm-port>` to run any docker commands.

To use tls with same certs as docker host:

~~~shell
$ docker run -d \
    -v /var/lib/boot2docker:/certs:ro \
    swarm manage --tlsverify \
    --tlscacert=/certs/ca.pem \
    --tlscert=/certs/server.pm \
    --tlskey=/certs/server-key.pem \
    <discovery>
~~~

## Discovery

swarm manager uses `<discovery>` to find the nodes in cluster. Following discovery services are supported.

### static list of Nodes 

format: `[nodes://]<ip1>,<ip2>`

ex: `nodes://10.0.0.[10:200]:2375,10.0.1.[2:250]:2375`

### static file

format: `file://path/to/file`

~~~shell
$ echo <ip1>:2375 > /opt/cluster
$ echo <ip2>:2375 > /opt/cluster
$ echo <ip3>:2375 > /opt/cluster
# now use `file:///opt/cluster`
~~~

### key-value store

`zk://<ip1>,<ip2>/<path>`  
`etcd://<ip1>,<ip2>/<path>`  
`consul://<ip>/<path>`

### Docker Hub Discovery Service (hosted)

format: `token://<token>`

First create a cluster to get token:

~~~shell
$ docker run --rm swarm create
0ac50ef75c9739f5bfeeaf00503d4e6e
~~~

Save the token for later use.  
Docker Hub discovery service keeps unused tokens for approximately one week.

if discovery service needs additional options use `--discovery-option`

~~~shelll
swarm manage \
    --discovery-opt kv.cacertfile=/path/to/mycacert.pem \
    --discovery-opt kv.certfile=/path/to/mycert.pem \
    --discovery-opt kv.keyfile=/path/to/mykey.pem \
    consul://<consul_addr>/<optional path prefix>
~~~

## Join Docker Host to cluster

~~~shell
$ docker run swarm join --advertise <engine-ip>:<engine-port> <discovery>
~~~

## High availability for Swarm Manager

Discovery service should be key-value store for high availablilty.

Run each swarm manager with following options:  
`--replication --advertise <manager-host>:<manager-port>`  

`--replication` enables swarm manager replication  
`--advertise` address of swarm manager joining the cluster. Should be reachable by other swarm managers

These Swarm managers operate in an active/passive formation with a single Swarm manager being the `primary`, and all others being `replica`. But you can use docker command on any swarm manager

you can see the current primary in swarm manager logs.  
`docker info` of swarm manager shows `Role: primary/replica`  
If it is replicate it shows `Primary: <ip>:<port>`  

## Strategy

Strategy tells how swarm computes ranking of nodes. When you run a container, swarm choses the node with highest computed ranking.

Specify strategry using `--strategy` flag to `swarm manage` command. Swarm currently supports `spread`, `binpack` and `random`.

### spread (default)

- compute rank according to a node’s available CPU, its RAM, and the number of containers it has
- attempts to balance the number of containers evenly across all nodes in the cluster.
- optimizes for node with least number of containers
- checks number of containers disregarding their state
- containers spread thinly over many machines
- advantage is, if a node goes down you only lose a few containers
- good choice for high performance clusters, as it spreads container workload across all resources in the cluster

### binpack

- compute rank according to a node’s available CPU, its RAM, and the number of containers it has
- optimizes for node which is most packed
- checks number of containers disregarding their state
- runs as many containers as possible on a node, effectively filling it up, before scheduling containers on the next node
- avoids fragmentation because it leaves room for bigger containers on unused machines
- uses fewer machines as Swarm tries to pack as many containers as it can on a node
- good choice for minimizing infrastructure requirements and cost
- if two nodes have the same amount of available RAM and CPUs, node with least containers is chosen

### random

- chooses nodes at random regardless of their available CPU or RAM

## Filters

- allow to swarm to chose nodes when creating and running containers
- each filter has a name that identifies it

### Filter Expressions
specified in `create` and `run` commands using `-e <filter-type>:<key><operator><value>` flag

#### `<operator>`

- either `==` or `!=`
- by default expressions are hard enforced i.e, if expression is not met exactly, container is not run
- for soft expression add suffix `~`. ex: `-e affinity:image=~redis`. soft expressions are ignored if not met.

#### `<value>`

- **literal value:** contains only alpha-numerics, dots, hyphens and underscore
- **globbing pattern:** `abc*`
- **regular-expression:** 
  - specified as `/regexp/`
  - [re2 syntax] (https://github.com/google/re2/wiki/Syntax) is supported
  - `/node[12]/` matches `node1` and `node2`
  - `/node\d/` matches `node` following single digit
  - `/foo\[bar\]/` matches `foo[bar]`
  - `/(?i)node1/` matches `node1` case-insensitive

Filters are devided into two categories.

### Node Filters

- operate on characteristics of the Docker host **or** on the configuration of the Docker daemon
- specified when creating a container **or** building an image `docker build --build-arg=constraint:storage==disk ...`

#### constraint

- refer to docker's default tags or custom labels
- default tags are sourced from `docker info`
- supported default tags:
  - `node` can be id or name
  - `storagedriver`
  - `executiondriver`
  - `operatingsystem`
  - `kernelversion1

ex: `-e constraint:storage==ssd`

#### health

- prevents running containers on unhealthy nodes
- A node is considered unhealthy if the node is down or it cannot communicate with the cluster store

### Container Configuration Filters

- operate on characteristics of containers, **or** on the availability of images on a host
- filters match all containers, including stopped containers, when applying the filter

#### affinity

used to create attactions between containers

**container name/id affinity:**  
`-e affinity:container==container1` run this container in same node where `container1` is running

**image name/id affinity:**  
`-e affinity:image==redis` run this container which has `redis` image already pulled

**container label affinity:**  
`-e affinity:com.example.type==frontend` run this container in same node that has container with label `com.example.type` of value `frontend`

#### dependency

**shared volumes:**  
`--volumes-from=<container>` 

**links:**  
`--link=<container>`  
`--link=<container>:<alias>`

**shared networks:**  
`--net=container:<container>`

#### port

based on container's port configuration, nodes are selected on which particular port is available and unoccupied by another container or process

By default all filters are enabled. To enable only specific subset of filters:

~~~shell
swarm manage --filter=health --filter=dependency
~~~

## Rescheduling (experimental)

Containes are associated with rescheduling policy with: 

- `-e reschedule:<policy>` or 
- `--label 'com.docker.swarm.reschedule-policy=["<policy>"]'`  

Avaiable rescheduling polcies are:

- `off` default if not specified
- `on-node-failure` 

