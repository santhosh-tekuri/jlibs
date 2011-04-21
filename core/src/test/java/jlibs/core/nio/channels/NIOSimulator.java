package jlibs.core.nio.channels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class NIOSimulator implements NIOSupport{
    public static final NIOSimulator INSTANCE = new NIOSimulator();
    private NIOSimulator(){}

    @Override
    public IOChannelHandler createHandler(){
        return new IOChannelHandler();
    }

    private List<SimulatingByteChannel> channels = new ArrayList<SimulatingByteChannel>();

    public boolean run(){
        boolean processed = !channels.isEmpty();
        List<SimulatingByteChannel> channels = this.channels;
        this.channels = new ArrayList<SimulatingByteChannel>();
        for(SimulatingByteChannel channel: channels){
            if(channel.isOpen()){
                if(channel.isReadable() ||channel.isWritable()){
                    IOChannelHandler handler = (IOChannelHandler)channel.attachment();
                    boolean readable = channel.isReadable();
                    boolean writable = channel.isWritable();
                    if(readable || writable)
                        channel.prepare();

                    if(handler.input!=null && readable)
                        handler.input.handler.onRead(handler.input);
                    if(handler.output!=null && writable){
                        try{
                            handler.output.onWrite();
                        }catch(IOException ex){
                            handler.output.handler.onIOException(handler.output, ex);
                        }
                    }
                }else
                    register(channel);
            }
        }
        return processed;
    }

    public void register(SimulatingByteChannel channel){
        if(!channels.contains(channel))
            channels.add(channel);
    }

    public void unregister(SimulatingByteChannel channel){
        if(channels.contains(channel))
            channels.remove(channel);
    }
}
