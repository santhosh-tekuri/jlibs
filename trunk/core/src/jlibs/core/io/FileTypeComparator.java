package jlibs.core.io;

import jlibs.core.util.DefaultComparator;

import java.io.File;

import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class FileTypeComparator extends DefaultComparator<File>{
    @Override
    protected int _compare(@NotNull File file1, @NotNull File file2){
        boolean dir1 = file1.isDirectory();
        boolean dir2 = file2.isDirectory();
        if(dir1==dir2)
            return 0;
        else
            return dir1 ? +1 : -1;
    }
}
