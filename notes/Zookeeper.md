---
title: JLibs
layout: default
---

# Zookeeper 3.4.6

## Standalone mode Configuration

create `conf/zoo.cfg` file with minimal configuration:

~~~properties
# length of a tick in milliseconds 
# basic unit of measurement of time used by ZooKeeper
# timeouts used by zookeeper are specified in units of tickTime
# minimum client session timeout is two ticks
tickTime=2000

# location to store the in-memory database snapshots and, 
# unless specified otherwise, the transaction log of updates to the database.
dataDir=/var/lib/zookeeper

# port that clients use to connect to this server
# default port is 2181
clientPort=2181

# by default, server listen on all of its interfaces on clientPort
# clientPortAddress=
~~~

## Replicated Mode Configuration

create `conf/zoo.cfg` file with minimal configuration:

~~~properties
# basic time unit in milliseconds used by ZooKeeper
# It is used to do heartbeats and the minimum session timeout will be twice the tickTime
tickTime=2000

# location to store the in-memory database snapshots and, 
# unless specified otherwise, the transaction log of updates to the database.
dataDir=/var/lib/zookeeper

# port to listen for client connections
clientPort=2181

###################[ Cluster Options ]###################

# length of time the ZooKeeper servers in quorum have to connect to a leader
initLimit=5

# how far out of date a server can be from a leader
syncLimit=2

# list the servers that make up the ZooKeeper service
# first port used for peer communication
# second port used for leader election
server.1=zoo1:2888:3888
server.2=zoo2:2888:3888
server.3=zoo3:2888:3888
~~~

create file `$dataDir/myid` which contains the current server numbr in ASCII

## Startup Script

~~~shell
$ bin/zkServer.sh start
$ bin/zkServer.sh stop
~~~

`zkServer.sh` supports `start|start-foreground|stop|restart|status|upgrade|print-cmd`  
`${dataDir}/zookeeper_server.pid` file contains server pid  
`zookeeper.out` contains the server logs

## CLI

~~~shell
$ bin/zkCli.sh -server 127.0.0.1:2181
~~~

following commands are supported:

~~~shell
help
connect host:port
get path [watch]
ls path [watch]
set path data [version]
rmr path
delquota [-n|-b] path
quit
printwatches on|off
create [-s] [-e] path data acl
stat path [watch]
close
ls2 path [watch]
history
listquota path
setAcl path acl
getAcl path
sync path
redo cmdno
addauth scheme auth
delete path [version]
setquota -n|-b val path
~~~

