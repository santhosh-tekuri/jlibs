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

package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public abstract class ThreadTasker{
    public abstract boolean isValid();
    protected abstract void executeAndWait(Runnable runnable);
    public abstract void executeLater(Runnable runnable);

    public void execute(Runnable runnable){
        if(isValid())
            runnable.run();
        else
            executeAndWait(runnable);
    }

    public <R, E extends Exception> R execute(ThrowableTask<R, E> task) throws E{
        execute(task.asRunnable());
        return task.getResult();
    }
    
    public <R> R execute(Task<R> task){
        execute(task.asRunnable());
        return task.getResult();
    }
}
