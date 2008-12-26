package jlibs.core.lang;

import jlibs.core.io.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class JavaUtil{
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static String getPID() throws IOException{
        String pid = System.getProperty("pid"); //NOI18N
        if(pid==null){
            String cmd[];
            File tempFile = null;
            try{
                if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
                    cmd = new String[]{ "/bin/sh", "-c", "echo $$ $PPID" }; //NOI18N
                else{
                    // getpids.exe is taken from http://www.scheibli.com/projects/getpids/index.html (GPL)
                    tempFile = File.createTempFile("getpids", "exe"); //NOI18N

                    // extract the embedded getpids.exe file from the jar and save it to above file
                    IOUtil.pump(JavaUtil.class.getResourceAsStream("getpids.exe"), new FileOutputStream(tempFile), true, true); //NOI18N
                    cmd = new String[]{ tempFile.getAbsolutePath() };
                }
                if(cmd!=null){
                    Process p = Runtime.getRuntime().exec(cmd);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    IOUtil.pump(p.getInputStream(), bout, false, true);

                    StringTokenizer stok = new StringTokenizer(bout.toString());
                    stok.nextToken(); // this is pid of the process we spanned
                    pid = stok.nextToken();
                    System.setProperty("pid", pid); //NOI18N
                }
            }finally{
                if(tempFile!=null)
                    tempFile.delete();
            }
        }
        return pid;
    }

    /**
     * This method guarantees that garbage collection is
     * done unlike <code>{@link System#gc()}</code>
     */
    @SuppressWarnings({"UnusedAssignment"})
    public static void gc(){
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get()!=null)
            System.gc();
    }

    /**
     * calls <code>{@link #gc()}</code> <code>count</code> times
     */
    public static void gc(int count){
        for(;count!=0; count--)
            gc();
    }

    public static void main(String[] args) throws IOException{
        System.out.println(getPID());
        gc(5);
    }
}
