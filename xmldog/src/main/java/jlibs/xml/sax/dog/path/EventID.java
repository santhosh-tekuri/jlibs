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

package jlibs.xml.sax.dog.path;

import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.dog.path.tests.NamespaceURI;
import jlibs.xml.sax.dog.path.tests.QName;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class EventID{
    private final int type;
    public String location;
    public EventID previous;

    public boolean interestedInAttributes;
    public boolean interestedInNamespaces;
    public int interestedInText;

    private ConstraintEntry listenersArray[][];

    public EventID(int type, ConstraintEntry listenersArray[][]){
        this.type = type;
        this.listenersArray = listenersArray;
        if(type!=NodeType.DOCUMENT)
            rootElementVisited = true;
    }

    /*-------------------------------------------------[ Empty ]---------------------------------------------------*/

    /**
     * empty[axis][nodetype] is true if the axis on that nodetype returns nothing
     */
    private static boolean empty[/*axis-type*/][/*node-type*/] = new boolean[Axis.MAX+1][NodeType.MAX+1];
    static{
        // attribute and namespace axis makes sense only for element
        boolean attr[] = empty[Axis.ATTRIBUTE];
        boolean namespace[] = empty[Axis.NAMESPACE];
        for(int i=13; i>0; i--){
            if(i!=NodeType.ELEMENT){
                attr[i] = true;
                namespace[i] = true;
            }
        }

        // child and descendant axis makes sense only for document and element
        boolean child[] = empty[Axis.CHILD];
        boolean descendant[] = empty[Axis.DESCENDANT];
        for(int i=13; i>0; i--){
            if(i!=NodeType.DOCUMENT && i!=NodeType.ELEMENT){
                child[i] = true;
                descendant[i] = true;
            }
        }

        // following-sibling axis doesn't make sense for document, attribute and namespace
        boolean followingSibling[] = empty[Axis.FOLLOWING_SIBLING];
        followingSibling[NodeType.DOCUMENT] = true;
        followingSibling[NodeType.ATTRIBUTE] = true;
        followingSibling[NodeType.NAMESPACE] = true;

        // following axis doesn't make sense for document
        empty[Axis.FOLLOWING][NodeType.DOCUMENT] = true;
    }

    /** tells if the specified axis always returns empty on this eventID */
    public boolean isEmpty(int axis){
        return empty[axis][type];
    }

    /*-------------------------------------------------[ Listeners ]---------------------------------------------------*/

    private static class AxisEntry{
        boolean active;
        int textCount;
        ConstraintEntry constraintEntry;

        public AxisEntry(boolean active){
            this.active = active;
        }
    }

    public static class ConstraintEntry{
        Constraint constraint;
        AxisListener listener;
        ConstraintEntry next;

        public ConstraintEntry(Constraint constraint, AxisListener listener){
            this.constraint = constraint;
            this.listener = listener;
        }
    }

    private int activeCount;
    public int axisEntryCount;
    private AxisEntry axisEntries[] = new AxisEntry[6];

    private boolean checkState(){
        if(axisEntries[Axis.ATTRIBUTE]!=null)
            assert axisEntries[Axis.ATTRIBUTE].active;
        if(axisEntries[Axis.NAMESPACE]!=null)
            assert axisEntries[Axis.NAMESPACE].active;

        int active = activeCount;
        int total = axisEntryCount;
        int textCount = 0;
        for(AxisEntry entry: axisEntries){
            if(entry!=null){
                assert entry.constraintEntry!=null;
                if(entry.active){
                    active--;
                    for(ConstraintEntry constraintEntry=entry.constraintEntry; constraintEntry!=null; constraintEntry=constraintEntry.next){
                        int constraintID = constraintEntry.constraint.id;
                        if(constraintID==Constraint.ID_NODE || constraintID==Constraint.ID_TEXT)
                            textCount++;
                    }
                }
                total--;
                assert active>=0;
            }
        }
        assert active==0 && total==0;
        assert interestedInText==textCount;
        return true;
    }

    public void addListener(Event event, Step step, AxisListener listener){
        int axis = step.axis;
        assert !isEmpty(axis);

        Constraint constraint = step.constraint;
        if(axis==Axis.SELF){
            if(constraint.matches(event))
                listener.onHit(this);
            listener.expired();
            return;
        }

        if(axis==Axis.DESCENDANT_OR_SELF){
            if(constraint.matches(event)){
                listener.onHit(this);
                if(listener.manuallyExpired)
                    return;
            }
            axis = Axis.DESCENDANT;
        }

        boolean active = true;
        int constraintID = constraint.id;

        switch(axis){
            case Axis.CHILD:
                if(type==NodeType.DOCUMENT && constraintID==Constraint.ID_TEXT){
                    listener.expired();
                    return;
                }
                break;
            case Axis.FOLLOWING_SIBLING:
            case Axis.FOLLOWING:
                if(type==NodeType.ELEMENT)
                    active = false;
                break;
        }

        AxisEntry entry = axisEntries[axis];
        if(entry==null){
            axisEntries[axis] = entry = new AxisEntry(active);
            if(active)
                ++activeCount;
            axisEntryCount++;
        }

        ConstraintEntry listeners[] = listenersArray[axis];
        ConstraintEntry oldConstraintEntry = listeners[constraintID];
        if(oldConstraintEntry!=null){
            assert entry.constraintEntry!=null;
            listener.nextAxisListener = oldConstraintEntry.listener;
            oldConstraintEntry.listener = listener;
        }else{
            ConstraintEntry constraintEntry = new ConstraintEntry(constraint, listener);
            constraintEntry.next = entry.constraintEntry;
            entry.constraintEntry = constraintEntry;
            listeners[constraintID] = constraintEntry;
            if(constraintID==Constraint.ID_NODE || constraintID==Constraint.ID_TEXT){
                entry.textCount++;
                if(active)
                    interestedInText++;
            }
        }

        assert checkState();
    }

    public void listenersAdded(){
        int noOfConstraints = listenersArray[0].length;
        // iterating axises other than Descendant-Self, Self
        for(int iAxis=0; iAxis<Axis.MAX_TRACKED+1; iAxis++){
            ConstraintEntry listeners[] = listenersArray[iAxis];
            for(int iListener=0; iListener<noOfConstraints; iListener++)
                listeners[iListener] = null;
        }

        interestedInNamespaces = axisEntries[Axis.NAMESPACE]!=null;
        interestedInAttributes = axisEntries[Axis.ATTRIBUTE]!=null;
    }

    private void expire(int axis){
        AxisEntry axisEntry = axisEntries[axis];
        if(axisEntry!=null){
            assert axisEntry.active;

            ConstraintEntry constraintEntry = axisEntry.constraintEntry;
            do{
                expireList(constraintEntry.listener);
                constraintEntry = constraintEntry.next;
            }while(constraintEntry!=null);
            interestedInText -= axisEntry.textCount;

            axisEntries[axis] = null;
            activeCount--;
            axisEntryCount--;
        }
    }

    private void expireList(AxisListener listener){
        do{
            if(!listener.manuallyExpired)
                listener.expired();
            AxisListener next = listener.nextAxisListener;
            listener.nextAxisListener = null;
            listener = next;
        }while(listener!=null);
    }

    private void inactivate(int axis){
        AxisEntry axisEntry = axisEntries[axis];
        if(axisEntry!=null && axisEntry.active){
            axisEntry.active = false;
            interestedInText -= axisEntry.textCount;
            activeCount--;
        }
    }

    private void activate(int axis){
        AxisEntry axisEntry = axisEntries[axis];
        if(axisEntry!=null && !axisEntry.active){
            axisEntry.active = true;
            interestedInText += axisEntry.textCount;
            activeCount++;
        }
    }

    /*-------------------------------------------------[ Axis Matching ]---------------------------------------------------*/

    private int d;

    public boolean push(){
        assert axisEntryCount!=0;

        d++;
        switch(d){
            case 1:
                inactivate(Axis.FOLLOWING_SIBLING);
                break;
            case 2:
                if(interestedInNamespaces){
                    expire(Axis.NAMESPACE);
                    interestedInNamespaces = false;
                }
                inactivate(Axis.CHILD);
        }
        assert checkState();
        return axisEntryCount==0;
    }

    private boolean subTreeFinished = false;
    private boolean parentLevelFinished = false;
    public boolean pop(boolean doc){
        assert axisEntryCount!=0;

        d--;
        if(doc)
            expire(Axis.FOLLOWING);

        switch(d){
            case -1:
                parentLevelFinished = true;
                expire(Axis.FOLLOWING_SIBLING);
                break;
            case 0:
                if(!subTreeFinished){
                    subTreeFinished = true;
                    if(interestedInNamespaces){
                        expire(Axis.NAMESPACE);
                        interestedInNamespaces = false;
                    }
                    expire(Axis.CHILD);
                    expire(Axis.DESCENDANT);

                }
                if(!parentLevelFinished){
                    activate(Axis.FOLLOWING_SIBLING);
                    activate(Axis.FOLLOWING);
                }
                break;
            case 1:
                if(!subTreeFinished)
                    activate(Axis.CHILD);
                break;
        }

        assert checkState();
        return axisEntryCount==0;
    }

    public boolean onEndAttributes() {
        assert axisEntryCount!=0;

        if(activeCount==0)
            return false;
        if(interestedInAttributes){
            expire(Axis.ATTRIBUTE);
            interestedInAttributes = false;
        }

        assert checkState();
        return axisEntryCount==0;
    }

    private boolean rootElementVisited;
    public boolean onEvent(Event event){
        assert axisEntryCount!=0;

        if(activeCount==0)
            return false;

        int eventType = event.type();
        if(eventType==NodeType.NAMESPACE){
            if(interestedInNamespaces){
                if(onEvent(event, axisEntries[Axis.NAMESPACE])){
                    axisEntries[Axis.NAMESPACE] = null;
                    activeCount--;
                    axisEntryCount--;
                    interestedInNamespaces = false;
                }
            }
        }else{
            if(interestedInNamespaces){
                expire(Axis.NAMESPACE);
                interestedInNamespaces = false;
            }
            if(eventType==NodeType.ATTRIBUTE){
                if(interestedInAttributes){
                    if(onEvent(event, axisEntries[Axis.ATTRIBUTE])){
                        axisEntries[Axis.ATTRIBUTE] = null;
                        activeCount--;
                        axisEntryCount--;
                        interestedInAttributes = false;
                    }
                }
            }else{
                for(int i=2; i<6; i++){ // excluding namespace & attribute axisEntries
                    AxisEntry axisEntry = axisEntries[i];
                    if(axisEntry!=null && axisEntry.active){
                        if(onEvent(event, axisEntry)){
                            axisEntries[i] = null;
                            activeCount--;
                            axisEntryCount--;
                        }
                    }
                }

                //take advantage that document has only one element
                if(!rootElementVisited && eventType==NodeType.ELEMENT){
                    rootElementVisited = true;
                    AxisEntry childEntry = axisEntries[Axis.CHILD];
                    if(childEntry!=null){
                        assert childEntry.active;

                        ConstraintEntry headConstraintEntry = null;
                        ConstraintEntry lastConstraintEntry = null;
                        ConstraintEntry constraintEntry = childEntry.constraintEntry;
                        do{
                            Constraint constraint = constraintEntry.constraint;
                            int constraintID = constraint.id;
                            if(constraintID==Constraint.ID_STAR
                                    || constraintID==Constraint.ID_PARENTNODE
                                    || constraintID==Constraint.ID_ELEMENT
                                    || constraint instanceof NamespaceURI
                                    || constraint instanceof QName){
                                expireList(constraintEntry.listener);
                            }else{
                                if(headConstraintEntry==null)
                                    headConstraintEntry = constraintEntry;
                                else
                                    lastConstraintEntry.next = constraintEntry;
                                lastConstraintEntry = constraintEntry;
                            }
                            constraintEntry = constraintEntry.next;
                        }while(constraintEntry!=null);
                        childEntry.constraintEntry = headConstraintEntry;
                        if(lastConstraintEntry!=null)
                            lastConstraintEntry.next = null;

                        if(headConstraintEntry==null){
                            axisEntries[Axis.CHILD] = null;
                            activeCount--;
                            axisEntryCount--;
                        }
                    }
                }
            }
        }

        assert checkState();
        return axisEntryCount==0;
    }

    private boolean onEvent(Event event, AxisEntry axisEntry){
        assert axisEntry.active;

        EventID eventID = null;
        ConstraintEntry headConstraintEntry = null;
        ConstraintEntry lastConstraintEntry = null;
        ConstraintEntry constraintEntry = axisEntry.constraintEntry;
        do{
            Constraint constraint = constraintEntry.constraint;
            boolean keep = true;
            if(constraint.matches(event)){
                AxisListener headListener = null;
                AxisListener lastListener = null;

                AxisListener listener = constraintEntry.listener;
                do{
                    if(!listener.manuallyExpired){
                        if(eventID==null)
                            eventID = event.getID();
                        listener.onHit(eventID);

                        if(!listener.manuallyExpired){
                            if(headListener==null)
                                headListener = listener;
                            else
                                lastListener.nextAxisListener = listener;
                            lastListener = listener;
                        }
                    }
                    listener = listener.nextAxisListener;
                }while(listener!=null);

                if(headListener==null){
                    keep = false;
                    int constraintID = constraint.id;
                    if(constraintID==Constraint.ID_NODE || constraintID==Constraint.ID_TEXT){
                        axisEntry.textCount--;
                        interestedInText--;
                    }
                }else{
                    constraintEntry.listener = headListener;
                    lastListener.nextAxisListener = null;
                }
            }
            if(keep){
                if(headConstraintEntry==null)
                    headConstraintEntry = constraintEntry;
                else
                    lastConstraintEntry.next = constraintEntry;
                lastConstraintEntry = constraintEntry;
            }
            constraintEntry = constraintEntry.next;
        }while(constraintEntry!=null);

        if(headConstraintEntry==null)
            return true;
        else{
            axisEntry.constraintEntry = headConstraintEntry;
            lastConstraintEntry.next = null;
            return false;
        }
    }
}
