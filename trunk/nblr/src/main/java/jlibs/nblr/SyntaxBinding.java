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

package jlibs.nblr;

import jlibs.nblr.actions.Action;
import jlibs.nblr.actions.BufferAction;
import jlibs.nblr.actions.EventAction;
import jlibs.nblr.actions.PublishAction;
import jlibs.nblr.matchers.*;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import jlibs.nblr.rules.RuleTarget;
import jlibs.xml.sax.binding.*;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
@Binding("syntax")
public class SyntaxBinding extends Matchers{
    static ThreadLocal<Syntax> SYNTAX = new ThreadLocal<Syntax>();

    @Binding.Start
    public static Syntax onStart(){
        Syntax syntax = new Syntax();
        SYNTAX.set(syntax);
        return syntax;
    }

    @Binding.Element(element="rule", clazz=RuleBinding.class)
    public static void rule(){}

    @Relation.Finish("rule")
    public static void relateWithRule(Syntax syntax, Rule rule){
        syntax.add(rule.name, rule);
    }

    @Relation.Finish("*")
    public static void relateWithMatcher(Syntax syntax, Matcher matcher){
        syntax.add(matcher.name, matcher);
    }
}

@Binding("matcher")
class MatcherBinding{
    @Binding.Start
    public static Matcher onStart(@Attr String name){
        return SyntaxBinding.SYNTAX.get().matchers.get(name);
    }
}

@Binding("any")
class AnyBinding{
    @Binding.Start
    public static Matcher onStart(@Attr String name, @Attr String chars){
        Any any = new Any(chars);
        any.name = name;
        return any;
    }
}

@Binding("range")
class RangeBinding{
    @Binding.Start
    public static Matcher onStart(@Attr String name, @Attr String from, @Attr String to){
        Range range = new Range(from.codePointAt(0), to.codePointAt(0));
        range.name = name;
        return range;
    }
}

@Binding("not")
class NotBinding extends Matchers{
    @Binding.Start
    public static Attributes onStart(Attributes attrs){
        return attrs;
    }

    @Relation.Finish("*")
    public static void relate(SAXContext parent, Matcher matcher){
        parent.put(new QName("matcher"), matcher);
    }

    @Binding.Finish
    public static Not onFinish(@Temp String name, @Temp Matcher matcher){
        Not not = new Not(matcher);
        not.name = name;
        return not;
    }
}

@Binding("and")
class AndBinding extends Matchers{
    @Binding.Start
    public static Attributes onStart(Attributes attrs){
        return attrs;
    }

    @Relation.Finish("*")
    public static void relate(SAXContext parent, Matcher matcher){
        parent.add(new QName("matchers"), matcher);
    }

    @Binding.Finish
    public static And onFinish(@Temp String name, @Temp List<Matcher> matchers){
        And and = new And(matchers.toArray(new Matcher[matchers.size()]));
        and.name = name;
        return and;
    }
}

@Binding("or")
class OrBinding extends Matchers{
    @Binding.Start
    public static Attributes onStart(Attributes attrs){
        return attrs;
    }

    @Relation.Finish("*")
    public static void relate(SAXContext parent, Matcher matcher){
        parent.add(new QName("matchers"), matcher);
    }

    @Binding.Finish
    public static Or onFinish(@Temp String name, @Temp List<Matcher> matchers){
        Or or = new Or(matchers.toArray(new Matcher[matchers.size()]));
        or.name = name;
        return or;
    }
}

class Matchers{
    @Binding.Element(element="matcher", clazz=MatcherBinding.class)
    public static void matcher(){}

    @Binding.Element(element="any", clazz=AnyBinding.class)
    public static void any(){}

    @Binding.Element(element="range", clazz=RangeBinding.class)
    public static void range(){}

    @Binding.Element(element="not", clazz=NotBinding.class)
    public static void not(){}

    @Binding.Element(element="and", clazz=AndBinding.class)
    public static void and(){}

    @Binding.Element(element="or", clazz=OrBinding.class)
    public static void or(){}
}

@Binding("rule")
class RuleBinding{
    @Binding.Start
    public static Rule onStart(@Attr String name){
        Syntax syntax = SyntaxBinding.SYNTAX.get();
        Rule rule = syntax.rules.get(name);
        if(rule==null){
            rule = new Rule();
            rule.name = name;
            syntax.add(name, rule);
        }
        return rule;
    }

    @Binding.Element(element="node", clazz=NodeBinding.class)
    public static void node(){}

    @Relation.Finish("node")
    public static @Temp.Add Node relateWithNode(Node node, Rule rule){
        if(rule.node==null)
            rule.node = node;
        return node;
    }

    @Binding.Element(element="edge", clazz=EdgeBinding.class)
    public static void edge(){}

    @Relation.Finish("edge")
    @SuppressWarnings({"unchecked"})
    public static void relateWithEdge(SAXContext parent, @Current Edge edge, @Temp String source, @Temp String target, @Temp String fallback){
        List<Node> nodes = (List<Node>)parent.get("", "node");
        edge.setSource(nodes.get(Integer.parseInt(source)));
        edge.setTarget(nodes.get(Integer.parseInt(target)));
        edge.fallback = Boolean.valueOf(fallback);
    }
}

@Binding("node")
class NodeBinding{
    @Binding.Start
    public static Node onStart(@Attr String name){
        Node node = new Node();
        node.name = name;
        return node;
    }

    @Binding.Start("buffer")
    public static BufferAction onBuffer(){
        return BufferAction.INSTANCE;
    }

    @Binding.Start("publish")
    public static PublishAction onPublish(@Attr String name, @Attr String begin, @Attr String end){
        return new PublishAction(name, Integer.parseInt(begin), Integer.parseInt(end));
    }

    @Binding.Start("event")
    public static EventAction onEvent(@Attr String name){
        return new EventAction(name);
    }

    @Relation.Start("*")
    public static void relateWithAction(Node node, Action action){
        node.action = action;
    }
}

@Binding("edge")
class EdgeBinding extends Matchers{
    @Binding.Start
    public static Attributes onStart(Attributes attrs){
        return attrs;
    }

    @Binding.Start("rule")
    public static RuleTarget onRuleTarget(@Attr String name, @Attr String node){
        Syntax syntax = SyntaxBinding.SYNTAX.get();
        Rule rule = syntax.rules.get(name);
        if(rule==null){
            rule = new Rule();
            rule.name = name;
            syntax.add(name, rule);
        }
        RuleTarget ruleTarget = new RuleTarget();
        ruleTarget.rule = rule;
        ruleTarget.name = node;
        return ruleTarget;
    }

    @Relation.Finish("rule")
    public static RuleTarget relateWithRuleTarget(RuleTarget ruleTarget){
        return ruleTarget;
    }

    @Relation.Finish("*")
    public static void relateWithMatcher(SAXContext parent, Matcher matcher){
        parent.put(new QName("matcher"), matcher);
    }

    @Binding.Finish
    public static Edge onFinish(@Temp("rule") RuleTarget ruleTarget, @Temp Matcher matcher){
        Edge edge = new Edge(null, null);
        edge.ruleTarget = ruleTarget;
        edge.matcher = matcher;
        return edge;
    }
}
