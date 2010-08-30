package jlibs.swing.datatransfer;

import jlibs.core.lang.ImpossibleException;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * Clipboard related utilities
 * 
 * @author Santhosh Kumar T
 */
public class ClipboardUtil{
    /** returns the system clipboard */
    private static Clipboard clipboard(){
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * replaces current contents of clipboard with the specified
     * <code>text</code>. If <code>text</code> is null, this will
     * clear the clipboard contents
     */
    public static void setText(String text){
        if(text==null)
            clear();
        else{
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = clipboard();
            clipboard.setContents(selection, selection);
        }
    }

    /**
     * returns the current text contents of clipboard. If clipboard
     * has no text contents or has no content, it returns null
     */
    public static String getText(){
        Transferable contents = clipboard().getContents(null);
        try{
            if(contents!=null && contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                return (String)contents.getTransferData(DataFlavor.stringFlavor);
            else
                return null;
        }catch(UnsupportedFlavorException ex){
            throw new ImpossibleException(ex);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    /** clears contents of clipboard */
    public static void clear(){
        clipboard().setContents(new Transferable(){
            public DataFlavor[] getTransferDataFlavors(){
                return new DataFlavor[0];
            }

            public boolean isDataFlavorSupported(DataFlavor flavor){
                return false;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{
                throw new UnsupportedFlavorException(flavor);
            }
        }, null);
    }
}
