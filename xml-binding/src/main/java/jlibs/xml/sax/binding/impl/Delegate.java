/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.xml.sax.binding.impl;

import jlibs.xml.sax.binding.SAXContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public class Delegate extends BindingCumRelation{
    private BindingCumRelation delegate;

    public Delegate(BindingCumRelation delegate){
        this.delegate = delegate;
    }

    @Override
    public void startRelation(int state, SAXContext parent, SAXContext current) throws SAXException{
        delegate.startRelation(state, parent, current);
    }

    @Override
    public void endRelation(int state, SAXContext parent, SAXContext current) throws SAXException{
        delegate.endRelation(state, parent, current);
    }

    @Override
    public void startElement(int state, SAXContext current, Attributes attributes) throws SAXException{
        delegate.startElement(state, current, attributes);
    }

    @Override
    public void text(int state, SAXContext current, String text) throws SAXException{
        delegate.text(state, current, text);
    }

    @Override
    public void endElement(int state, SAXContext current) throws SAXException{
        delegate.endElement(state, current);
    }
}
