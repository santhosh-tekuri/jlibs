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

package jlibs.nblr.codegen;

import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.core.util.Range;
import jlibs.nblr.codegen.java.SyntaxClass;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Path;
import jlibs.nblr.rules.Routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class State{
    public final RuleMethod ruleMethod;
    public Node fromNode;
    public final List<Decision> decisions = new ArrayList<Decision>();

    public final RootIf rootIF = new RootIf(this);
    public final List<IfBlock> ifBlocks = rootIF.children;

    public State(RuleMethod ruleMethod, Node fromNode){
        this.ruleMethod = ruleMethod;
        this.fromNode = fromNode;

        Routes routes = new Routes(ruleMethod.rule, fromNode, true);
        
        if(routes.toString().endsWith("[)]<EOF>"))
            System.out.print("");
        for(int lookAhead: routes.lookAheads())
            processLookAhead(routes.determinateRoutes(lookAhead));

        // move loop without fallback to the beginning
        for(Decision decision: decisions){
            if(decision.usesFinishAll()){
                ruleMethod.syntaxClass.addToFinishAll(decision.matchers[0]);
                decisions.remove(decision);
                decisions.add(0, decision);
                break;
            }
        }

        if(routes.indeterminateRoute!=null)
            decisions.add(new Decision(this, routes.indeterminateRoute.route()[0]));

        if(routes.routeStartingWithEOF!=null)
            decisions.add(new Decision(this, routes.routeStartingWithEOF));

        optimize();
        populateIfBlocks();
    }

    private void processLookAhead(List<Path> routes){
        processLookAhead(routes, 1);
    }

    private void processLookAhead(List<Path> routes, int depth){
        List<List<Path>> groups = new ArrayList<List<Path>>();
        Matcher matcher = null;
        for(Path route: routes){
            Path path = route.route()[depth-1];
            Matcher curMatcher = path.matcher();
            if(curMatcher==null)
                curMatcher = eofMatcher;

            if(matcher==null || !curMatcher.same(matcher)){
                groups.add(new ArrayList<Path>());
                matcher = curMatcher;
            }
            groups.get(groups.size()-1).add(route);
        }

        for(List<Path> group: groups){
            Path route = group.get(0);
            if(depth<routes.get(0).depth)
                processLookAhead(group, depth+1);
            if(depth==route.depth){
                Decision decision = new Decision(this, route);
                    decisions.add(decision);
            }
        }
    }

    private boolean isCandidate(Decision decision){
        int i = decisions.indexOf(decision)+1;
        while(i<decisions.size()){
            Decision d = decisions.get(i);
            if(decision.matchers.length!=d.matchers.length)
                return true;
            if(!d.path.equals(decision.path)){
                if(ArrayUtil.getLast(d.matchers)==null)
                    return false;
                if(ArrayUtil.getLast(decision.matchers).clashesWith(ArrayUtil.getLast(d.matchers)))
                    return false;
            }
            i++;
        }
        return true;
    }

    private void optimize(){
        List<List<Decision>> lists = new ArrayList<List<Decision>>();
        for(int i=decisions.size()-1; i>=0; i--){
            Decision decision = decisions.get(i);
            if(decision.path.matcher()==null && isCandidate(decision)){
                boolean listFound = false;
                for(List<Decision> list: lists){
                    if(decision.path.equals(list.get(0).path)){
                        listFound = true;
                        list.add(decision);
                        break;
                    }
                }
                if(!listFound){
                    List<Decision> newList = new ArrayList<Decision>();
                    newList.add(decision);
                    lists.add(newList);
                }
            }
        }
        if(lists.size()==0)
            return;

        if(decisions.get(decisions.size()-1).matchers[0]==null){
            Decision lastDecision = decisions.get(decisions.size()-1);
            for(List<Decision> list: lists){
                if(list.get(0).path.equals(lastDecision.path)){
                    lists.clear();
                    lists.add(list);
                    break;
                }
            }
        }

        List<Decision> preferredList = lists.get(0);
        for(int i=1; i<lists.size(); i++){
            List<Decision> list = lists.get(i);
            if(list.size()>preferredList.size())
                preferredList = list;
        }

        Decision preferredDecision = preferredList.get(0);
        if(preferredList.size()==1 && preferredDecision.matchers.length<maxLookAhead())
            return;
        decisions.removeAll(preferredList);
        decisions.add(preferredDecision);
        preferredDecision.matchers = new Matcher[]{ null };
    }

    public void computeNextStates(ArrayList<Node> statesVisited, LinkedHashSet<Node> statesPending){
        for(Decision decision: decisions)
            decision.computeNextStates(statesVisited, statesPending);
    }

    private void populateIfBlocks(){
        List<List<IfBlock>> lists = new ArrayList<List<IfBlock>>();
        int lastLen = 0;

        for(Decision decision: decisions){
            IfBlock curIf = null;
            if(lastLen!=decision.matchers.length){
                lists.add(new ArrayList<IfBlock>());
                lastLen = decision.matchers.length;
            }

            for(Matcher matcher: decision.matchers){
                List<IfBlock> children = curIf==null ? lists.get(lists.size()-1) : curIf.children;
                IfBlock found = null;
                if(matcher!=null){
                    for(IfBlock child: children){
                        if(matcher.same(child.matcher)){
                            found = child;
                            break;
                        }
                    }
                }
                if(found==null){
                    found = new IfBlock(this);
                    found.matcher = matcher;
                    children.add(found);
                    found.parent = curIf;
                }
                curIf = found;
            }
            curIf.path = decision.path;
        }

        for(List<IfBlock> list: lists)
            rootIF.children.addAll(list);

        rootIF.computeCommon();
    }

    public boolean readCodePoint(){
        for(Decision decision: decisions){
            if(decision.readCodePoint())
                return true;
        }
        return false;
    }

    public boolean readCharacter(){
        for(Decision decision: decisions){
            if(!decision.readCharacter())
                return false;
        }
        return true;
    }

    public boolean matchesNewLine(){
        for(Decision decision: decisions){
            if(decision.matchesNewLine())
                return true;
        }
        return false;
    }

    public String expected(){
        StringBuilder builder = new StringBuilder();
        for(Decision decision: decisions){
            if(builder.length()>0)
                builder.append(" OR ");
            builder.append(decision.expected());
        }
        return builder.toString();
    }

    public boolean requiresContinue(State nextState){
        for(Decision decision: decisions){
            if(decision.requiresContinue(nextState))
                return true;
        }
        return false;
    }

    public int maxLookAhead(){
        int maxLookAhead = 0;
        for(Decision decision: decisions)
            maxLookAhead = Math.max(maxLookAhead, decision.matchers.length);
        return maxLookAhead;
    }

    public boolean lookAheadRequired(){
        return maxLookAhead()>1;
    }

    public String readMethod(){
        if(!lookAheadRequired() && readCharacter() && !matchesNewLine())
            return "position==limit ? marker : input[position]";
        else
            return "codePoint()";
    }

    public String breakStatement(){
        return ruleMethod.requiresWhile() ? "break loop;" : "break;";
    }

    public void generate(Printer printer, State nextState){
        printer.printlns(
            "case "+fromNode.stateID+":",
                PLUS
        );
        rootIF.generate(printer, nextState);
        printer.printlns(
                MINUS
        );
    }

    public void generate1(Printer printer, State nextState){
        printer.printlns(
            "case "+fromNode.stateID+":",
                PLUS
        );

        if(readCodePoint() && (!decisions.get(0).usesFinishAll() || lookAheadRequired())){
            printer.printlns(
                "if((ch="+readMethod()+")==EOC)"+(SyntaxClass.DEBUGGABLE ? "{" : ""),
                    PLUS
//                    "exiting(RULE_"+ruleMethod.rule.name.toUpperCase()+", "+fromNode.stateID+");"
            );
            if(SyntaxClass.DEBUGGABLE)
                printer.println("handler.currentNode("+ruleMethod.rule.id+", "+fromNode.stateID+");");
            printer.println(breakStatement());
            printer.printlns(
//                    "return false;",
                    MINUS,
                SyntaxClass.DEBUGGABLE ? "}" : null
            );
        }

        boolean lookAheadReqd = lookAheadRequired();
        int lastLookAhead = 0;
        int elseAfterDecision = 1;
        int lastDecisionAction = Decision.ADD_CONTINUE;
        boolean closeLALengthCheck = false;
        for(int i=0; i<decisions.size(); i++){
            Decision decision = decisions.get(i);
            int curLookAhead = decision.matchers.length; 
            if(curLookAhead>lastLookAhead){
                elseAfterDecision = 1;
                lastDecisionAction = Decision.ADD_CONTINUE;
                if(decision.usesFinishAll())
                     elseAfterDecision = 2;

                if(lookAheadReqd){
                    if(curLookAhead>1){
                        if(lastLookAhead<=1){
                            printer.println("addToLookAhead(ch);");
                            lastLookAhead = 1;
                        }
                        String prefix, condition;
                        if(curLookAhead==lastLookAhead+1){
                            prefix = "if";
                            condition = "ch!=EOF";
                        }else{
                            prefix = "while";
                            condition = "ch!=EOF && laLen<"+curLookAhead; 
                        }
                        printer.printlns(
                            prefix+"("+condition+"){",
                                PLUS,
                                "if((ch=codePoint())==EOC)",
                                    PLUS,
                                    breakStatement(),
                                    MINUS,
                                "addToLookAhead(ch);",
                                MINUS,
                            "}"
                        );
                    }
                                        
                    closeLALengthCheck = true;
                    if(curLookAhead>1){
                        printer.printlns(
                            "if(laLen=="+curLookAhead+"){",
                                PLUS
                        );
                    }
                }
            }

            boolean closeBlock = false;
            if(!lookAheadReqd && (elseAfterDecision<=0 || lastDecisionAction==Decision.GOTO_NEXT_CASE || lastDecisionAction==Decision.CALL_RULE_AND_NEXT_DECISION)){
                printer.print("else ");
                if(decision.matchers[0]==null){
                    closeBlock = true;
                    printer.printlns(
                        "{",
                            PLUS
                    );
                }
            }

            if(decision.usesFinishAll())
                decision.generate(printer, nextState);
            else{
                Decision prevDecision = i==0 ? null : decisions.get(i-1);
                int common = common(prevDecision, decision);
                for(int j=common; j<decision.matchers.length; j++)
                    decision.startMatcher(printer, j);

                decision.addBody(printer, nextState);

                Decision nextDecision = i==decisions.size()-1 ? null : decisions.get(i+1);
                common = common(decision, nextDecision);
                for(int j=common; j<decision.matchers.length; j++)
                    decision.endMatcher(printer, j);
            }

            if(closeBlock){
                printer.printlns(
                        MINUS,
                    "}"
                );
            }

            if(lookAheadReqd && closeLALengthCheck && (i+1==decisions.size() || decisions.get(i+1).matchers.length!=curLookAhead)){
                closeLALengthCheck = false;
                if(curLookAhead>1){
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }
            }

            lastLookAhead = curLookAhead;
            elseAfterDecision--;
            lastDecisionAction = decision.returnAction(nextState);
        }

        Decision lastDecision = decisions.get(decisions.size()-1);
        if(lastDecision.matchers[0]!=null){
            if(!lookAheadReqd)
                printer.print("else ");
            printer.println("expected(ch, \""+ StringUtil.toLiteral(expected(), false)+"\");");
        }
        
        printer.printlns(
                MINUS
        );
    }

    private int common(Decision decision1, Decision decision2){
        if(decision1==null || decision2==null)
            return 0;
        if(decision1.matchers.length!=decision2.matchers.length)
            return 0;
        if(decision1.usesFinishAll())
            return 0;

        for(int i=0; i<decision1.matchers.length; i++){
            Matcher matcher1 = decision1.matchers[i];
            Matcher matcher2 = decision2.matchers[i];
            if(matcher1==null || matcher2==null)
                return i;
            if(!matcher1.same(matcher2))
                return i;
        }
        throw new ImpossibleException();
    }

    protected static Matcher eofMatcher = new Matcher(){
        @Override
        public String toString(){ throw new UnsupportedOperationException(); }

        @Override
        protected String __javaCode(String variable){ throw new UnsupportedOperationException(); }

        @Override
        public List<Range> ranges(){
            return Collections.emptyList();
        }
    };
}
