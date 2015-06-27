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

import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.path.tests.Element;
import jlibs.xml.sax.dog.path.tests.ParentNode;

/**
 * This class has various optimizations for LocationPath
 *
 * @author Santhosh Kumar T
 */
public class LocationPathAnalyzer{
    public static LocationPath simplify(LocationPath path){
        if(impossible(path))
            return LocationPath.IMPOSSIBLE;

        path = discardSelfNode(path);
        if(path.steps.length>0){
            path = compressAnywhere(path);
            pnode_element(path);
        }

        return path;
    }

    private static boolean impossible(LocationPath path){
        /**
         * folowing xpaths always returns empty result
         *
         * 1. /text()
         * 2. /following-sibling:anything
         * 3. /following:anything
         * 4. /attribute:anything
         * 5. /namespace:anything
         */
        if(path.scope==Scope.DOCUMENT && path.steps.length>0){
            switch(path.steps[0].axis){
                case Axis.CHILD:
                    if(path.steps[0].constraint.id==Constraint.ID_TEXT)
                        return true;
                    break;
                case Axis.FOLLOWING_SIBLING:
                case Axis.FOLLOWING:
                case Axis.ATTRIBUTE:
                case Axis.NAMESPACE:
                    return true;
            }
        }


        int lastAxis = -1;
        for(Step step: path.steps){
            // any step having predicate as false() returns nothing
            if(step.predicateSet.impossible)
                return true;

            // following-sibling after attribute or namespace axis always returns nothing
            if(step.axis==Axis.FOLLOWING_SIBLING && (lastAxis==Axis.ATTRIBUTE || lastAxis==Axis.NAMESPACE))
                return true;

            lastAxis = step.axis;
        }

        return false;
    }

    // discard self::node() step if it has no predicate
    private static LocationPath discardSelfNode(LocationPath path){
        int noOfNulls = 0;
        Step steps[] = path.steps;

        for(int i=steps.length-1; i>=0; --i){
            Step step = steps[i];
            if(step.axis==Axis.SELF && step.constraint.id==Constraint.ID_NODE && !step.predicateSet.hasPosition){
                if(step.predicateSet.getPredicate()==null){
                    steps[i] = null;
                    noOfNulls++;
                }else{
                    if(i!=0){
                        steps[i] = null;
                        steps[i-1].setPredicate(step);
                        noOfNulls++;
                    }
                }
            }
        }
        return noOfNulls>0 ? removeNullSteps(path, noOfNulls) : path;
    }

    // "//book" i,e "/descendant-or-self::node()/child::book" can be simplified to
    // "/descendant::book" if there are no predicates on first two steps
    private static LocationPath compressAnywhere(LocationPath path){
        int noOfNulls = 0;
        Step steps[] = path.steps;

        Step prevStep = steps[0];
        for(int i=1, len=steps.length; i<len; i++){
            Step curStep = steps[i];

            if(!curStep.predicateSet.hasPosition && prevStep.predicateSet.getPredicate()==null){
                if(prevStep.axis==Axis.DESCENDANT_OR_SELF && prevStep.constraint.id==Constraint.ID_NODE){
                    if(curStep.axis==Axis.CHILD){
                        steps[i-1] = null;
                        Step newStep = new Step(Axis.DESCENDANT, curStep.constraint);
                        newStep.setPredicate(curStep);
                        steps[i] = curStep = newStep;
                        noOfNulls++;
                    }
                }
            }
            prevStep = curStep;
        }
        return noOfNulls>0 ? removeNullSteps(path, noOfNulls) : path;
    }

    private static void pnode_element(LocationPath path){
        Step steps[] = path.steps;
        for(int i=1, len=steps.length; i<len; i++){
            Step curStep = steps[i];
            switch(curStep.axis){
                case Axis.NAMESPACE:
                case Axis.ATTRIBUTE:
                    // if the step preceding attribute/namespace has no predicate and doing node() test,
                    // the test can be replaced with element() test
                    Step prevStep = steps[i-1];
                    if(!prevStep.predicateSet.hasPosition && prevStep.constraint.id==Constraint.ID_NODE){
                        steps[i-1] = new Step(prevStep.axis, Element.INSTANCE);
                        steps[i-1].setPredicate(prevStep);
                    }
                    break;
                case Axis.CHILD:
                case Axis.DESCENDANT:
                    // if the step preceding child/decendant has no predicate and doing node() test,
                    // the test can be replaced with pnode() test
                    prevStep = steps[i-1];
                    if(!prevStep.predicateSet.hasPosition && prevStep.constraint.id==Constraint.ID_NODE){
                        steps[i-1] = new Step(prevStep.axis, ParentNode.INSTANCE);
                        steps[i-1].setPredicate(prevStep);
                    }
                    break;
            }
        }
    }

    private static LocationPath removeNullSteps(LocationPath path, int noOfNulls){
        LocationPath spath = new LocationPath(path.scope, path.steps.length-noOfNulls);
        int i = 0;
        for(Step step: path.steps){
            if(step!=null){
                spath.steps[i] = step;
                i++;
            }
        }
        return spath;
    }
}
