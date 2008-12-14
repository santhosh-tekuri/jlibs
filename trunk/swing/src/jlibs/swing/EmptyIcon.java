package jlibs.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class EmptyIcon implements Icon{
    public static final EmptyIcon INSTANCE = new EmptyIcon();

    private EmptyIcon(){}
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y){
    }

    @Override
    public int getIconWidth(){
        return 0;
    }

    @Override
    public int getIconHeight(){
        return 0;
    }
}
