---
title: JLibs
layout: default
---

# Docker Networking

## Get Container IP Address

To get IP address of container:

~~~shell
$ docker inspect -f '{{.NetworkSettings.IPAddress}}' mycontainer
172.17.0.3
~~~

To list all IP addresses of container:

~~~shell
$ docker inspect --format='{{range .NetworkSettings.Networks}} {{.IPAddress}}{{end}}' mycontainer
172.17.0.2 172.19.0.2 172.18.0.2
~~~

## Default Networks

By default docker provides two network drivers, 

- bridge: bridge network is limited to a single host running Docker Engine
- overlay: overlay network can include multiple hosts

each docker host includes three default networks, which cannot be removed:

~~~shell
$ docker network ls
NETWORK ID          NAME                DRIVER
de38c5965891        none                null
c352aa0c0b19        host                host
7cc7fc7b7517        bridge              bridge
~~~

### bridge network

`bridge` network represents the `docker0` network in docker host.  
Unless you specify otherwise with the `docker run --net=<NETWORK>` option, the Docker daemon connects containers to this network by default.


Containers in this default network are able to communicate with each other using IP addresses.
To communicate with container names, use `docker run --link` option.

### host network

`host` network adds a container on the hosts network stack. You’ll find the network configuration inside the container is identical to the host.

### none network

The none network adds a container to a container-specific network stack. That container lacks a network interface.

## Inspect a network

~~~shell
$ docker network inspect bridge
[
    {
        "Name": "bridge",
        "Id": "7cc7fc7b75171a101d55a73f57636f5b9c1c194b7baa766693c3744031f8f3b9",
        "Scope": "local",
        "Driver": "bridge",
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16"
                }
            ]
        },
        "Containers": {
            "7bf025cf1e9dee076695fd8a71d9690e26573617de109b17130dee496dc88765": {
                "Name": "small_williams",
                "EndpointID": "8cd4c35721e84611a38190198eeed374eae31f9c14a0a76610edeed6f416d8b0",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            }
        },
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        }
    }
]
~~~

you can see the containers attached to this network in the above output.

## Attach and Detach Containers

You can launch containers on a network using the `docker run --net=<NETWORK>` option.  
You can specify only one network using this flag.

You can connect/disconnect a container from a network:

~~~shell
$ docker network disconnect bridge small_williams
$ docker network connect bridge small_williams
~~~

you can attach a container to as many networks as you like.

## User-defined Bridge Network

To create user-defined bridge network:

~~~shell
$ docker network create -d bridge mynetwork
~~~

linking is not supported in user-defined bridge networks.  
Container can communicate with each other using `CONTAINER_NAME` or `CONTAINER_NAME.NETWORK_NAME`

## Overlay Network

`Overlay` network driver supports multi-host networking natively out-of-the-box, with the help of `libnetwork`(a built-in VXLAN-based overlay network driver), and Docker’s `libkv` library.

This network requires a valid key-value store service. Docker's `libkv` library supports Consul, Etcd and Zookeeper currently. Docker hosts in overlay network must be able to communicate with key-value store service.

To create key-value store service: 

~~~shell
$ docker-machine create -d virtualbox keystore
$ eval "$(docker-machine env keystore)"
$ docker run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                                            NAMES
ee43520d64bf        progrium/consul     "/bin/start -server -"   33 seconds ago      Up 33 seconds       53/tcp, 53/udp, 8300-8302/tcp, 8400/tcp, 8301-8302/udp, 0.0.0.0:8500->8500/tcp   mad_williams
~~~

now create two docker hosts:

~~~shell
$ docker-machine create -d virtualbox --engine-opt="cluster-store=consul://$(docker-machine ip keystore):8500" --engine-opt="cluster-advertise=eth1:2376" vm0
$ docker-machine create -d virtualbox --engine-opt="cluster-store=consul://$(docker-machine ip keystore):8500" --engine-opt="cluster-advertise=eth1:2376" vm1
~~~

to add existing boot2docker host:

add following to `/var/lib/boot2docker/profile`:

~~~shell
EXTRA_ARGS='
--label provider=virtualbox
--cluster-store=consul://192.168.99.100:8500
--cluster-advertise=eth1:2376

'
~~~

now restart docker daemon:

~~~shell
$ sudo /etc/init.d/docker restart
~~~

create overlay network from any of docker hosts

~~~shell
$ eval "$(docker-machine env vm0)"
$ docker network create -d overlay mynet
4340f3eb99ff42cfe9ff70a4273de879dc009f2ec70695ab694bd2805a65ed29
$ docker network ls
NETWORK ID          NAME                DRIVER
4340f3eb99ff        mynet               overlay
b7c5821f4f0e        host                host
d57a953470a0        bridge              bridge
d7fd76f58305        none                null
$ eval "$(docker-machine env vm1)"
$ docker network ls
NETWORK ID          NAME                DRIVER
4340f3eb99ff        mynet               overlay
9c0a4ca69bb7        host                host
24f4e046965d        bridge              bridge
8b7e74e742a1        none                null
~~~

when you connect to vm0 and do `docker network inspect mynet` it shows only containers running in vm0.

### docker_gwbridge network

When you create your first overlay network on any host, Docker also creates another network on each host called `docker_gwbridge`. Docker creates only one docker_gwbridge bridge network per host regardless of the number of overlay networks present. Docker uses this network to provide external access for containers.
