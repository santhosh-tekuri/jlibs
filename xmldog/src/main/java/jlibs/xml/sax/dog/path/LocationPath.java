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

package jlibs.xml.sax.dog.path;

import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.nodset.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class LocationPath extends Predicated{
    public static final LocationPath LOCAL_CONTEXT = new LocationPath(Scope.LOCAL, 0);
    public static final LocationPath DOCUMENT_CONTEXT = new LocationPath(Scope.DOCUMENT, 0);
    public static final LocationPath IMPOSSIBLE = new LocationPath(Scope.GLOBAL, 0);

    public final int scope;
    public final Step steps[];
    public boolean pathExpression;

    public LocationPath(int scope, int noOfSteps){
        this.scope = scope;
        steps = new Step[noOfSteps];
    }

    public final List<LocationPath> contexts = new ArrayList<LocationPath>();
    
    public void addToContext(LocationPath path){
        if(!path.pathExpression && path.contexts.size()>0)
            contexts.addAll(path.contexts);
        else
            contexts.add(path);
        if(!path.pathExpression && path.predicateSet.predicate!=null){
            assert predicateSet.predicate==null;
            predicateSet = path.predicateSet;
            path.predicateSet = new PredicateSet();
            
            assert hitExpression==null;
            hitExpression = path.hitExpression;
            path.hitExpression = null;
        }
    }

    public PathExpression.HitExpression hitExpression;
    
    @Override
    public void addPredicate(Expression predicate){
        if(hitExpression==null && !predicateSet.hasPosition){
            boolean hasPosition = predicateSet.hasPosition(predicate);
            if(hasPosition){
                hitExpression = new PathExpression.HitExpression();
                super.addPredicate(hitExpression);
            }
        }
        super.addPredicate(predicate);
    }

    public Expression typeCast(DataType dataType){
        LocationExpression expr = _typeCast(dataType);
        if(contexts.size()>0)
            return new PathExpression(this, expr, false);
        else
            return expr;
    }
    
    private LocationExpression _typeCast(DataType dataType){
        switch(dataType){
            case NODESET:
                return new NodeSet(this);
            case BOOLEAN:
                return new Bool(this);
            case STRINGS:
                return new Strings(this, DataType.STRINGS, true, false);
            case STRING:
                return new Strings(this, DataType.STRING, false, true);
            case NUMBER:
                return new Strings(this, DataType.NUMBER, false, true);
            case NUMBERS:
                return new Strings(this, DataType.NUMBERS, true, false);
            default:
                throw new ImpossibleException("can't type cast locationPath to "+dataType.name());
        }
    }

    public Expression apply(String function){
        LocationExpression expr = _apply(function);
        if(contexts.size()>0)
            return new PathExpression(this, expr, false);
        else
            return expr;
    }
    
    private LocationExpression _apply(String function){
        switch(function.length()){
            case 3:
                if(function.equals("sum"))
                    return new Strings(this, DataType.NUMBER, true, false);
                return null;
            case 4:
                if(function.equals("name"))
                    return new QualifiedName(this);
                return null;
            case 5:
                if(function.equals("count"))
                    return new Count(this);
                return null;
            case 6:
                if(function.equals("string"))
                    return new Strings(this, DataType.STRING, false, true);
                else if(function.equals("number"))
                    return new Strings(this, DataType.NUMBER, false, true);
                return null;
            case 7:
                if(function.equals("boolean"))
                    return new Bool(this);
                else if(function.equals("strings"))
                    return new Strings(this, DataType.STRINGS, true, false);
                return null;
            case 10:
                if(function.equals("local-name"))
                    return new LocalName(this);
                return null;
            case 13:
                if(function.equals("namespace-uri"))
                    return new NamespaceURI(this);
                return null;
            default:
                assert !ArrayUtil.contains(new String[]{
                        "sum", "name", "count", "string", "number", 
                        "boolean", "strings", "local-name", "namespace-uri"
                        }, function);
                return null;
        }
    }

    @Override
    public String toString(){
        if(steps.length==0)
            return scope==Scope.DOCUMENT ? "/" : "";
        else{
            StringBuilder buff = new StringBuilder();
            for(Step step: steps)
                buff.append('/').append(step);
            return buff.substring(scope==Scope.DOCUMENT ? 0 : 1);
        }
    }
}
