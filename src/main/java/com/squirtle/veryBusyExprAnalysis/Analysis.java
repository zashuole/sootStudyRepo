package com.squirtle.veryBusyExprAnalysis;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

//采用backWard
public class Analysis extends BackwardFlowAnalysis<Unit, FlowSet<Expr>>{
    private UnitGraph g;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public Analysis(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Expr> in, Unit u, FlowSet<Expr> out) {
        // 步骤1：先将IN集合中被当前节点“杀死”的信息移除，结果暂存到outSet
        kill(in, u, out);
        // 步骤2：向outSet中添加当前节点“生成”的信息
        gen(out, u);
    }

    protected void kill(FlowSet<Expr> in, Unit u, FlowSet<Expr> out) {
        in.copy(out);
        if (u instanceof AssignStmt) {
            Value left = ((AssignStmt) u).getLeftOp();
            for (Expr expr : in.toList()) {
                for (ValueBox vb : expr.getUseBoxes()) {
                    Value v = vb.getValue();
                    if (v.equivTo(left)) {
                        out.remove(expr);
                        break;
                    }
                }
            }
        }else if(u instanceof IdentityStmt){
            Value left = ((IdentityStmt) u).getLeftOp();
            for (Expr expr : in.toList()) {
                for (ValueBox vb : expr.getUseBoxes()) {
                    Value v = vb.getValue();
                    if (v.equivTo(left)) {
                        out.remove(expr);
                        break;
                    }
                }
            }
        }
    }

    protected void gen(FlowSet<Expr> out, Unit u) {
        for (ValueBox vb : u.getUseBoxes()) {
            Value v = vb.getValue();
            if (v instanceof Expr && !(v instanceof Constant) && !(v instanceof Local)) {
                out.add((Expr) v);
            }
        }
    }

    @Override
    protected FlowSet<Expr> newInitialFlow() {
        return new ValueArraySparseSet();
    }
    @Override
    protected FlowSet<Expr> entryInitialFlow() {
        return new ValueArraySparseSet(); // 方法出口为空
    }
    @Override
    protected void merge(FlowSet<Expr> in1, FlowSet<Expr> in2, FlowSet<Expr> out) {
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<Expr> source, FlowSet<Expr> dest) {
        source.copy(dest);
    }

}
