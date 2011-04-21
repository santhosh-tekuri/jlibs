package jlibs.core.nio.channels;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class SampleTest{
    @Test(description="Client[20]->Client[10]")
    public void client20Toclient20() throws IOException{
        String file = "/testng.xml";
        SimulatingByteChannel channel = new SimulatingByteChannel(
                                            getClass().getResourceAsStream(file), new EqualChunkIterator(file.length(), 20), true,
                                            getClass().getResourceAsStream(file), new EqualChunkIterator(file.length(), 10), true
                                        );
        InputChannel input = new ClientInputChannel(channel, NIOSimulator.INSTANCE);
        OutputChannel output = new ClientOutputChannel(channel, NIOSimulator.INSTANCE);

        Pipe pipe = new Pipe(input, output);
        pipe.setHandler(new Pipe.Handler(){
            @Override
            public void onTimeout(Pipe pipe, Channel channel){
                System.out.println("onTimeout: "+channel);
            }

            @Override
            public void onIOException(Pipe pipe, Channel channel, IOException ex){
                System.out.println("onIOException: "+channel);
                ex.printStackTrace();
            }

            @Override
            public void finished(Pipe pipe){
                System.out.println("onFinish");
            }
        });
        pipe.start();

        while(NIOSimulator.INSTANCE.run());

        assert !channel.isInPending();
        assert !channel.isOutPending();
    }
}

class EqualChunkIterator implements Iterator<Integer>{
    private long size;
    private int chunk;

    EqualChunkIterator(long size, int chunk){
        this.size = size;
        this.chunk = chunk;
    }

    @Override
    public boolean hasNext(){
        return size>0;
    }

    @Override
    public Integer next(){
        long min = Math.min(size, chunk);
        size -= min;
        return (int)min;
    }

    @Override
    public void remove(){
        throw new UnsupportedOperationException();
    }
}
