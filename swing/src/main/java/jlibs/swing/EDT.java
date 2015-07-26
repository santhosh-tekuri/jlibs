/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.swing;

import jlibs.core.lang.ThreadTasker;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class EDT extends ThreadTasker{
    public static final EDT INSTANCE = new EDT();
    private EDT(){}
    
    @Override
    public boolean isValid(){
        return EventQueue.isDispatchThread();
    }

    @Override
    protected void executeAndWait(Runnable runnable){
        try{
            EventQueue.invokeAndWait(runnable);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex){
            if(ex.getCause()==null)
                throw new RuntimeException(ex);
            else if(ex.getCause() instanceof RuntimeException)
                throw (RuntimeException)ex.getCause();
            else
                throw new RuntimeException(ex.getCause());
        }
    }

    @Override
    public void executeLater(Runnable runnable){
        EventQueue.invokeLater(runnable);
    }
}
