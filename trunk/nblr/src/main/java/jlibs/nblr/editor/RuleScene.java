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

package jlibs.nblr.editor;

import jlibs.nblr.Syntax;
import jlibs.nblr.editor.actions.*;
import jlibs.nblr.editor.layout.RuleLayout;
import jlibs.nblr.editor.layout.RuleRouter;
import jlibs.nblr.editor.widgets.EdgeWidget;
import jlibs.nblr.editor.widgets.NBLRWidget;
import jlibs.nblr.editor.widgets.NodeWidget;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.Observable;

/**
 * @author Santhosh Kumar T
 */
public class RuleScene extends GraphScene<Node, Edge>{
    private final WidgetAction moveAction = ActionFactory.createMoveAction();
    private final WidgetAction nodePopupAction = ActionFactory.createPopupMenuAction(new NodePopupProvider(this));
    private final WidgetAction edgePopupAction = ActionFactory.createPopupMenuAction(new EdgePopupProvider(this));

    private final WidgetAction hoverAction;
    private final WidgetAction editAction;

    private LayerWidget interactionLayer = new LayerWidget (this);
    private WidgetAction connectAction = ActionFactory.createConnectAction(interactionLayer, new NodeConnectProvider());

    public RuleScene(TwoStateHoverProvider hoverProvider, EditProvider edgeEditProvider){
        addChild(nodes);
        addChild(connections);
        addChild(interactionLayer);

        hoverAction = ActionFactory.createHoverAction(new Highlighter(hoverProvider));
        editAction = ActionFactory.createEditAction(edgeEditProvider);

        getActions().addAction(hoverAction);
        getActions().addAction(ActionFactory.createPopupMenuAction(new ScenePopupProvider(this)));
        getActions().addAction(ActionFactory.createWheelPanAction());
    }

    /*-------------------------------------------------[ Rule ]---------------------------------------------------*/

    private Syntax syntax;
    private Rule rule;
    public final Observable ruleObservable = new Observable(){
        @Override
        public void notifyObservers(Object arg){
            setChanged();
            super.notifyObservers(arg);
        }
    };
    
    public void setRule(Syntax syntax, Rule rule){
        this.syntax = syntax;
        
        if(this.rule!=null){
            for(Node node: getNodes().toArray(new Node[getNodes().size()]))
                removeNodeWithEdges(node);
        }
        this.rule = rule;
        if(this.rule!=null)
            populate(rule.node);

        layout();
        ruleObservable.notifyObservers(rule);
    }

    public Syntax getSyntax(){
        return syntax;
    }
    
    public Rule getRule(){
        return rule;
    }

    private void populate(Node node){
        if(findWidget(node)==null){
            addNode(node);

            for(Edge edge: node.outgoing){
                populate(edge.target);
                addEdge(edge);
                setEdgeSource(edge, edge.source);
                setEdgeTarget(edge, edge.target);
            }
        }
    }

    /*-------------------------------------------------[ Layout ]---------------------------------------------------*/

    private final SceneLayout layout = LayoutFactory.createSceneGraphLayout(this, new RuleLayout(false));
    
    public void layout(){
        validate();
        layout.invokeLayoutImmediately();
    }

    public void layout(Object model){
        findWidget(model).revalidate();
        layout();
    }

    public void refresh(){
        Syntax syntax = this.syntax;
        Rule rule = this.rule;
        setRule(null, null);
        setRule(syntax, rule);
    }

    /*-------------------------------------------------[ Nodes ]---------------------------------------------------*/

    private final LayerWidget nodes = new LayerWidget(this);

    @Override
    protected Widget attachNodeWidget(Node node){
        NodeWidget widget = new NodeWidget(this);
        widget.setFont(Util.FIXED_WIDTH_FONT);
        widget.highLight(false);

        nodes.addChild(widget);
//        widget.getActions().addAction(moveAction);
        widget.getActions().addAction(hoverAction);
        widget.getActions().addAction(nodePopupAction);
        widget.getActions().addAction(connectAction);

        return widget;
    }

    /*-------------------------------------------------[ Edges ]---------------------------------------------------*/

    private final LayerWidget connections = new LayerWidget(this);
    private final Router router = new RuleRouter();

    private LabelWidget createEdgeLabel(Edge edge){
        LabelWidget label = new LabelWidget(this, edge.toString());
        label.setFont(Util.FIXED_WIDTH_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        label.getActions().addAction(hoverAction);
        label.getActions().addAction(editAction);
        label.getActions().addAction(moveAction);
        label.getActions().addAction(edgePopupAction);
        return label;
    }

    @Override
    protected Widget attachEdgeWidget(Edge edge){
        EdgeWidget connection = new EdgeWidget(this);
        connection.setStroke(Util.STROKE_2);
        connection.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);

        LabelWidget label = createEdgeLabel(edge);
        connection.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER, 0.5f);

        connection.addChild(label);
        connections.addChild(connection);

        connection.setRouter(router);
        connection.highLight(false);
        
        connection.getActions().addAction(hoverAction);
        connection.getActions().addAction(editAction);
        connection.getActions().addAction(edgePopupAction);
        return connection;
    }

    @Override
    protected void attachEdgeSourceAnchor(Edge edge, Node oldSource, Node newSource){
        Widget w = newSource!=null ? findWidget(newSource) : null;
        ((ConnectionWidget)findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
    }

    @Override
    protected void attachEdgeTargetAnchor(Edge edge, Node oldTarget, Node newTarget){
        Widget w = newTarget!=null ? findWidget(newTarget) : null;
        ((ConnectionWidget)findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
    }

    /*-------------------------------------------------[ Executing ]---------------------------------------------------*/

    private Widget executionWidget;
    
    private void executing(Widget widget){
        if(executionWidget!=widget){
            if(executionWidget!=null){
                ((NBLRWidget)executionWidget).executing(false);
                executionWidget = null;
            }
            if(widget!=null){
                executionWidget = widget;
                ((NBLRWidget)executionWidget).executing(true);
                getView().scrollRectToVisible(Util.bounds(widget));
            }
            validate();
        }
    }

    public void executing(Node node){
        executing(findWidget(node));
    }

    public void executing(Edge edge){
        executing(findWidget(edge));
    }

    /*-------------------------------------------------[ Antialiasing ]---------------------------------------------------*/
    
    public void paintChildren(){
        Graphics2D g = getGraphics();
        Object anti = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object textAnti = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintChildren();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, anti);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAnti);
    }
}
