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

package jlibs.xml.sax.dog.expr;

/**
 * @author Santhosh Kumar T
 */
public abstract class Evaluation<X extends Expression> extends EvaluationListener{
    public final X expression;
    public final long order;

    protected Evaluation(X expression, long order){
        this.expression = expression;
        this.order = order;
    }

    public abstract void start();
    public abstract Object getResult();

    /*-------------------------------------------------[ Listener Support ]---------------------------------------------------*/

    protected EvaluationListener listener;

    public void addListener(EvaluationListener listener){
        this.listener = listener;
    }

    public void removeListener(EvaluationListener listener){
        if(this.listener==listener){
            this.listener = null;
            dispose();
        }
    }

    protected void fireFinished(){
        listener.finished(this);
    }

    protected void dispose(){}
}
