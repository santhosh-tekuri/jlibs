---
title: JLibs
layout: default
---

# Docker Images

## Referring to Image

An image is referred by IMAGE_ID or REPOSITORY:TAG or REPOSITORY:@DIGEST

### IMAGE_ID

it is a long numeric id, that can be truncated.  
ex: 77af4d6b9913e693e8d0b4b294fa62ade6054e6b2f1ffb617ac955dd63fb0182  
ex: 77af4d6b9913  

all images must have an IMAGE_ID.

### REPOSITORY:TAG

base or root images have single word in REPOSITORY. ex: `ubuntu`.  
these images are official and provided by Docker Inc.  

user images has two words in REPOSITORY. ex: `training/sinatra`.   
here first word is the user that created them.

TAG is used to specify a version. ex: `ubuntu:14.04`  
if TAG is missing it defaults to `latest`. So just `ubuntu` means `ubuntu:latest`.  
Always specify image tag, to know exactly what variant you are using.

images without REPOSITORY:TAG are called *intermediate images*.

### REPOSITORY:@DIGEST

Images that use v2 or latest format have a content-addressable identifier called DIGEST.  
DIGEST is generated from the input used to generate the image, so its value is predictable.

ex: `ouruser/sinatra@sha256:cbbf2f9a99b47fc460d422812b6a5adff7dfee951d8fa2e4a98caa0382cfbdbf`

## Listing Images

Docker downloads images from a registry and stores the downloaded images in docker host.  
The default registry is [Docker Hub Registry]

[Docker Hub Registry]: https://registry.hub.docker.com/

To list images present in docker host:

~~~shell
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
ubuntu              14.04               07c86167cdc4        41 hours ago        188 MB
ubuntu              latest              07c86167cdc4        41 hours ago        188 MB
training/webapp     latest              6fae60ef3446        9 months ago        348.8 MB
~~~

the above command does not list intermediate images.  
`-a` to list all images  
`-q` to list only image ids  
`--digests` to show digests  
`--no-trunc` to show long image ids

to list all images in the `java` repository

~~~shell
$ docker images java
~~~

to check specific image exists:

~~~shell
$ docker images java:8
~~~

To format output use placeholders `.ID`, `.Repository`, `.Tag`, `.Digest`, `.CreatedSince`, `.CreatedAt`, `.Size`

~~~shell{% raw %}
$ docker images --format "{{.Repository}}:{{.Tag}}"
santhosh/webapp:v1
ubuntu:14.04
ubuntu:latest
training/webapp:latest

$ docker images --format "table {{.Repository}}\t{{.Tag}}"
REPOSITORY          TAG
santhosh/webapp     v1
ubuntu              14.04
ubuntu              latest
training/webapp     latest
{% endraw %}
~~~

**Dangling Images:**

they are untagged images, that are the leaves of the images tree (not intermediary layers).  
they occur when image is build leaving it as `<none>:<none>` or untagged.  

~~~shell
$ docker images -f "dangling=true"
~~~

useful in batch cleanup using `docker rmi ...`

## Downloading Images

Docker automatically downloads any image not present in docker host.  

To explictly download an image:

~~~shell
$ docker pull centos:14.04
Pulling repository centos
b7de3133ff98: Pulling dependent layers
5cc9e91966f7: Pulling fs layer
511136ea3c5a: Download complete
ef52fb1fe610: Download complete
. . .

Status: Downloaded newer image for centos
~~~

`-a` to pull all tagged images in the repository

## Searching Images

To find all images containing the term `java`:

~~~shell
$ docker search java
NAME                   DESCRIPTION                                     STARS     OFFICIAL   AUTOMATED
node                   Node.js is a JavaScript-based platform for...   1796      [OK]
java                   Java is a concurrent, class-based, and obj...   696       [OK]
tomcat                 Apache Tomcat is an open source implementa...   508       [OK]
anapsix/alpine-java    Oracle Java 8 (and 7) with GLIBC 2.21 over...   51                   [OK]
~~~

You can also find images from [Docker Hub] website.

## Tag an existing image

you can tag existing image multiple times.

~~~shell
$ docker tag 5db5f8471261 santhosh/webapp:v2
~~~

## Removing Image

~~~shell
$ docker rmi training/webapp
~~~

If any container is using this image, it returns error.

## Backup and Restore Images

`docker save` command produces a tarred repository to standard output.

~~~shell
$ docker save ubuntu:14.04 > ubuntu-14.04.tar
~~~

`-o` allows to redirect to file

~~~shell
$ docker save -o ubuntu-14.04.tar ubuntu:14.04
~~~

to save all tags of repository:

~~~shell
$ docker save ubuntu > ubunu-all.tar
~~~

To cherry-pick particular tags:

~~~shell
$ docker save -o ubuntu.tar ubuntu:lucid ubuntu:saucy
~~~

To save all images:

~~~shell
$ docker save `docker images --format "{{.Repository}}:{{.Tag}}"` > all.tar
~~~

to load images from tar-ball:

~~~shell
$ docker load < all.tar
$ docker load -i all.tar
~~~

**NOTE:** If you save image by its image id, when loaded you wont get tags

## Creating Image from Container

~~~shell
$ docker commit -m "test commit" -a "santhosh kumar" small_williams santhosh/webapp:v1
sha256:c8c5f4c0391d2624e838a7f40399eb9649d2dff3805cd8ee251e6f59f82526f8
~~~

this works even if container is running.

## Creating Image from Docker File

create a directory with a file named `DockerFile` containing:

~~~
# This is a comment
FROM ubuntu:14.04
MAINTAINER Kate Smith <ksmith@example.com>
RUN apt-get update && apt-get install -y ruby ruby-dev
RUN gem install sinatra
~~~

`FROM` tells the source of our image  
`MAINTAINER` to specify who maintains the new image  
`RUN` to execute commands inside the image

to build the image:

~~~shell
$ docker build -t santhosh/sintara:v2 <directory-containing-DockerFile>
~~~

the contents of directory you are building is called build context. Docker first uploads build context.
then executes step-by-step. in each step it creates new container, runs the instruction inside the
container and them commit the change. Finally all intermediate containers are removed.

each instruction creates a layer. An image can't have more than 127 layers.



