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

package jlibs.xml.sax.binding;

import jlibs.xml.sax.binding.impl.Handler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class BindingHandler extends Handler{
    public BindingHandler(Class clazz){
        this(new BindingRegistry(clazz));
    }
    
    public BindingHandler(QName qname, Class clazz){
        this(new BindingRegistry(qname, clazz));
    }

    public BindingHandler(BindingRegistry docRegistry){
        super(docRegistry.registry);
    }

    private BindingListener listener;

    public void setBindingListener(BindingListener listener){
        this.listener = listener;
    }

    @Override
    protected void onUnresolvedElement(SAXContext context) throws SAXException{
        if(listener!=null)
            listener.unresolvedElement(context);
    }
}
