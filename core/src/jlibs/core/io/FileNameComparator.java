package jlibs.core.io;

import jlibs.core.util.DefaultComparator;

import java.io.File;

import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class FileNameComparator extends DefaultComparator<File>{
    @Override
    protected int _compare(@NotNull File file1, @NotNull File file2){
        return file1.getName().compareToIgnoreCase(file2.getName());
    }
}
