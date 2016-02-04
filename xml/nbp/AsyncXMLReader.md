---
title: AsyncXMLReader
layout: default
---

# NON-Blocking XMLReader #

`AsyncXMLReader` implements `org.xml.sax.XMLReader` interface.  
So it can be used just like any standard SAX Parser implementation.

But It also supports non-blocking parsing, So that it can be used with NIO.

Let us see how to use it in non-blocking mode.

Rather than using `org.xml.sax.InputSource`, we use `ChannelInputSource`:

~~~java
import jlibs.xml.sax.async.ChannelInputSource;

InputSource source = new ChannelInputSource(socketChannel);
~~~

Note that `ChannelInputSource` is used for non-blocking input.

To parse in non-blocking mode, instead of doing `reader.parse(source)` we will do the following:

~~~java
Feeder feeder = reader.createFeeder(source);
feeder = feeder.feed();
~~~

We are using non-blocking input, thus we are not feeding entire xml document to the reader in one shot.
we should keep feeding the reader, as an when data is ready to read from socketChannel. This concept is
abstracted into `Feeder` class. When data is ready to be read from socketChannel, we call `feeder.feed()`.
When feeding is complete, `feeder.feed()` returns null.

But one interesing thing to be noted is, `feeder.feed()` returns `Feeder`. The returned `Feeder` could be
the same `feeder` object or different one. For example here the feeder returned might be for external dtd,
in case you have resolved external dtd with another `ChannelInputSource`.

~~~java
SocketChannel channel = (SocketChannel)feeder.byteChannel();
// enable read interest on channel
~~~

`feeder.byteChannel()` returns the actual `ReadableByteChannel` on which more input required. Now you can add read
interest on this channel, and when data is ready to read, you can resume feeding by calling `feeder.feed()`.

You can use both blocking and non-blocking inputs intermixed. for example xml document might be feeded from
non-blocking input where as the dtd it is referring may be from a blocking input (local file)
