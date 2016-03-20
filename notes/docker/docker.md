---
title: JLibs
layout: default
---

# Docker 1.10.2

To find version of Docker client and daemon you are using, but also the version of Go language used by them

~~~shell
$ docker version
Client:
 Version:      1.10.2
 API version:  1.22
 Go version:   go1.5.3
 Git commit:   c3959b1
 Built:        Mon Feb 22 22:37:33 2016
 OS/Arch:      darwin/amd64

Server:
 Version:      1.10.2
 API version:  1.22
 Go version:   go1.5.3
 Git commit:   c3959b1
 Built:        Mon Feb 22 22:37:33 2016
 OS/Arch:      linux/amd64
~~~

To list commands provided by docker:

~~~shell
$ docker
Usage: docker [OPTIONS] COMMAND [arg...]
       docker [ --help | -v | --version ]

A self-sufficient runtime for containers.

Options:

  --config=~/.docker                                       Location of client config files
  -D, --debug                                              Enable debug mode
  -H, --host=[]                                            Daemon socket(s) to connect to
  -h, --help                                               Print usage
  -l, --log-level=info                                     Set the logging level
  --tls                                                    Use TLS; implied by --tlsverify
  --tlscacert=~/.docker/machine/machines/default/ca.pem    Trust certs signed only by this CA
  --tlscert=~/.docker/machine/machines/default/cert.pem    Path to TLS certificate file
  --tlskey=~/.docker/machine/machines/default/key.pem      Path to TLS key file
  --tlsverify=true                                         Use TLS and verify the remote
  -v, --version                                            Print version information and quit

Commands:
    attach    Attach to a running container
    build     Build an image from a Dockerfile
    commit    Create a new image from a container's changes
    cp        Copy files/folders between a container and the local filesystem
    create    Create a new container
    diff      Inspect changes on a container's filesystem
    events    Get real time events from the server
    exec      Run a command in a running container
    export    Export a container's filesystem as a tar archive
    history   Show the history of an image
    images    List images
    import    Import the contents from a tarball to create a filesystem image
    info      Display system-wide information
    inspect   Return low-level information on a container or image
    kill      Kill a running container
    load      Load an image from a tar archive or STDIN
    login     Register or log in to a Docker registry
    logout    Log out from a Docker registry
    logs      Fetch the logs of a container
    network   Manage Docker networks
    pause     Pause all processes within a container
    port      List port mappings or a specific mapping for the CONTAINER
    ps        List containers
    pull      Pull an image or a repository from a registry
    push      Push an image or a repository to a registry
    rename    Rename a container
    restart   Restart a container
    rm        Remove one or more containers
    rmi       Remove one or more images
    run       Run a command in a new container
    save      Save an image(s) to a tar archive
    search    Search the Docker Hub for images
    start     Start one or more stopped containers
    stats     Display a live stream of container(s) resource usage statistics
    stop      Stop a running container
    tag       Tag an image into a repository
    top       Display the running processes of a container
    unpause   Unpause all processes within a container
    update    Update resources of one or more containers
    version   Show the Docker version information
    volume    Manage Docker volumes
    wait      Block until a container stops, then print its exit code

Run 'docker COMMAND --help' for more information on a command.
~~~

To get help about specific docker command:

~~~shell
$ docker attach --help

Usage:  docker attach [OPTIONS] CONTAINER

Attach to a running container

  --detach-keys       Override the key sequence for detaching a container
  --help              Print usage
  --no-stdin          Do not attach STDIN
  --sig-proxy=true    Proxy all received signals to the process
~~~

## Running Application

docker runs your application inside containers

~~~shell
$ docker run ubuntu /bin/echo 'hello world'
Unable to find image 'ubuntu:latest' locally
latest: Pulling from library/ubuntu

5a132a7e7af1: Pull complete
fd2731e4c50c: Pull complete
28a2f68d1120: Pull complete
a3ed95caeb02: Pull complete
Digest: sha256:4e85ebe01d056b43955250bbac22bdb8734271122e3c78d21e55ee235fc6802d
Status: Downloaded newer image for ubuntu:latest
hello world
~~~

docker lauches container from image `ubuntu` and runs the command `/bin/echo 'hello world'`.
If docker does not find image in docker host, it downloads from [Docker Hub]. After command
exited, the container is stopped.

[Docker Hub]: https://hub.docker.com/

### Running Interactive Container

~~~shell
$ docker run -it ubuntu /bin/bash
root@303ba91b9192:/# exit
exit
~~~

`-t` flag assigns a pseudo-tty or terminal inside our new container  
`-i` flag allows us to make an interactive connection by grabbing STDIN of the container

### Running Daemonized Container

~~~shell
$ docker run -d ubuntu /bin/sh -c "while true; do echo hello world; sleep 1; done"
7ff09b01b4891a19127d7ede69773927bb1a9d20907c5605f2021f65fb7e9a7a
~~~

`-d` flag tells docker to run the container and put it in the background(daemonize).  
the output is *Container ID* in long string format

~~~shell
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
7ff09b01b489        ubuntu              "/bin/sh -c 'while tr"   4 minutes ago       Up 4 minutes                            fervent_engelbart
~~~

notice our container is still running.  
container can be referred by *long container id* or *short container id* or *name* (auto-generated)

To see complete logs of container:

~~~shell
$ docker logs 7ff09b01b489
hello world
hello world
hello world
hello world
~~~

To tail logs of container:

~~~shell
$ docker logs -f 7ff09b01b489
hello world
hello world
~~~

To List processes running in a container:

~~~shell
$ docker top adoring_albattani
UID                 PID                 PPID                C                   STIME               TTY                 TIME                CMD
root                3701                1223                0                   08:20               ?                   00:00:00            python app.py
root                3922                1223                0                   08:31               pts/0               00:00:00            /bin/bash
~~~
here `PID` refers to process id in docker host

`stop` command stops container sending `SIGTERM` and then `SIGKILL` after a grace period.  
default grace period is 10 seconds. `-t` is used to set grace period.

~~~
$ docker stop fervent_engelbart
fervent_engelbart

$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES

$ docker start fervent_engelbart
fervent_engelbart
~~~

to restart container (works on stopped containers also):

~~~shell
$ docker restart fervent_engelbart
fervent_engelbart
~~~

to remove stopped container:

~~~shell
$ docker rm fervent_engelbart
fervent_engelbart
~~~

to remove running container (uses SIGKILL):

~~~shell
$ docker rm -f fervent_engelbart
fervent_engelbart
~~~


### Listing Containers

To list only running containers

~~~shell
$ docker ps
~~~

To get details of only last container created:

~~~shell
$ docker ps -l
~~~

### Port Mapping

~~~shell
$ docker run -d -P training/webapp python app.py
~~~

`-P` flag maps any ports exposed in image from container to the docker host.  
Ports in docker host are chosen from ephemeral port range, which typically ranges from 32768 to 61000.  

To explictly map any port in container:

- `-p 5000` maps port 5000 from container to docker host
- `-p 80:5000` maps port 5000 from container to port 80 in docker host.  

`docker ps` command lists port mappings in `PORTS` column.

~~~shell
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                     NAMES
a60a11fa75a8        training/webapp     "python app.py"     11 minutes ago      Up 11 minutes       0.0.0.0:32768->5000/tcp   trusting_bell
~~~

To list just port mappings:

~~~shell
$ docker port adoring_albattani
5000/tcp -> 0.0.0.0:7777
8888/tcp -> 0.0.0.0:6666
~~~

To get specific port mapping:

~~~shell
$ docker port adoring_albattani 5000
0.0.0.0:7777
~~~

## Inspecting Container

`inspect` command returns json containing useful configuration and status information about container

~~~shell
$ docker inspect adoring_albattani
[
    {
        "Id": "11413064b8094a259117959b6a51944ed02f2d1b3855c4952e9deefd7c5a1276",
        "Created": "2016-03-05T08:20:15.476625804Z",
        "Path": "python",
        "Args": [
            "app.py"
        ],
        "State": {
            "Status": "running",
            "Running": true,
.....
~~~

To get ip address of container:

~~~shell
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' adoring_albattani
172.17.0.4
~~~

