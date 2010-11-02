/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.nblr.codegen;

import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.core.util.Range;
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
    public final Node fromNode;
    public final List<Decision> decisions = new ArrayList<Decision>();

    public State(RuleMethod ruleMethod, Node fromNode){
        this.ruleMethod = ruleMethod;
        this.fromNode = fromNode;

        Routes routes = new Routes(ruleMethod.rule, fromNode, true);
        
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

        List<List<Decision>> ruleTargetLists = new ArrayList<List<Decision>>();
        for(Decision decision: decisions){
            if(decision.edgeWithRule()!=null){
                boolean listFound = false;
                for(List<Decision> ruleTargetList: ruleTargetLists){
                    if(decision.path.equals(ruleTargetList.get(0).path)){
                        listFound = true;
                        ruleTargetList.add(decision);
                        break;
                    }
                }
                if(!listFound){
                    List<Decision> newList = new ArrayList<Decision>();
                    newList.add(decision);
                    ruleTargetLists.add(newList);
                }
            }
        }

        if(routes.indeterminateRoute!=null)
            decisions.add(new Decision(this, routes.indeterminateRoute.route()[0]));
        if(routes.routeStartingWithEOF!=null)
            decisions.add(new Decision(this, routes.routeStartingWithEOF));

        if(routes.indeterminateRoute==null && routes.routeStartingWithEOF==null){
            if(ruleTargetLists.size()==1){
                Decision lastDecision = decisions.get(decisions.size()-1);
                assert lastDecision.matchers[0]!=null;
                
                List<Decision> ruleTargetList = ruleTargetLists.remove(0);
                decisions.removeAll(ruleTargetList);
                Decision ruleTargetDecision = ruleTargetList.get(0);
                ruleTargetDecision.matchers = new Matcher[]{ null };
                decisions.add(ruleTargetDecision);
            }else if(ruleTargetLists.size()>1 && !lookAheadRequired()){
                int preferred = ruleTargetLists.size()-1;
                for(int i=preferred-1; i>=0; i--){
                    if(ruleTargetLists.get(i).size()>ruleTargetLists.get(preferred).size())
                        preferred = i;
                }

                List<Decision> ruleTargetList = ruleTargetLists.remove(preferred);
                decisions.removeAll(ruleTargetList);
                Decision ruleTargetDecision = ruleTargetList.get(0);
                ruleTargetDecision.matchers = new Matcher[]{ null };
                decisions.add(ruleTargetDecision);
            }
        }
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
//                if(!decisions.contains(decision))
                    decisions.add(decision);
            }
        }
    }

    public void computeNextStates(ArrayList<Node> statesVisited, LinkedHashSet<Node> statesPending){
        for(Decision decision: decisions)
            decision.computeNextStates(statesVisited, statesPending);
    }

    public boolean readCodePoint(){
        for(Decision decision: decisions){
            if(decision.readCodePoint())
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

    public void generate(Printer printer, State nextState){
        printer.printlns(
            "case "+fromNode.stateID+":",
                PLUS
        );

        if(readCodePoint() && (!decisions.get(0).usesFinishAll() || lookAheadRequired())){
            printer.printlns(
                "if((ch=codePoint())==EOC){",
                    PLUS,
                    "exiting(RULE_"+ruleMethod.rule.name.toUpperCase()+", "+fromNode.stateID+");",
                    "return false;",
                    MINUS,
                "}"
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
                        String prefix = curLookAhead==lastLookAhead+1 ? "if" : "while";
                        printer.printlns(
                            prefix+"(ch!=EOF && lookAhead.length()<"+curLookAhead+"){",
                                PLUS,
                                "if((ch=codePoint())==EOC){",
                                    PLUS,
                                    "exiting(RULE_"+ruleMethod.rule.name.toUpperCase()+", "+fromNode.stateID+");",
                                    "return false;",
                                    MINUS,
                                "}",
                                "addToLookAhead(ch);",
                                MINUS,
                            "}"
                        );
                    }
                                        
                    closeLALengthCheck = true;
                    printer.printlns(
                        "if(lookAhead.length()=="+(curLookAhead==1?0:curLookAhead)+"){",
                            PLUS
                    );
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
                printer.printlns(
                        MINUS,
                    "}"
                );
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
