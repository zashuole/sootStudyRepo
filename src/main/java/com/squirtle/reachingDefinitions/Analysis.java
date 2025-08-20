package com.squirtle.reachingDefinitions;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class Analysis extends ForwardFlowAnalysis<Unit, FlowSet<AssignStmt>> {

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
    protected void flowThrough(FlowSet<AssignStmt> in, Unit d, FlowSet<AssignStmt> out) {
        //先kill再gen
        kill(in,d,out);
        gen(out,d);
    }



    private void kill(FlowSet<AssignStmt> in, Unit d, FlowSet<AssignStmt> out) {
        //先让out暂存结果
        in.copy(out);
        if(d instanceof AssignStmt){
            AssignStmt assign = (AssignStmt)d;
            //当前语句的左值如果存在且表达式的define中有左值的变量，就kill掉
            Value leftOp = assign.getLeftOp();
            for(AssignStmt as : in.toList()){
                if (leftOp.equivTo(as.getLeftOp())) {
                    out.remove(as);
                }
            }
        }
    }

    private void gen(FlowSet<AssignStmt> out, Unit d) {
        if(d instanceof AssignStmt){
            out.add((AssignStmt)d);
        }
    }

    @Override
    protected FlowSet<AssignStmt> newInitialFlow() {
        return new ValueArraySparseSet();
    }
    @Override
    protected FlowSet<AssignStmt> entryInitialFlow() {
        return new ValueArraySparseSet(); // 方法入口为空
    }
    @Override
    protected void merge(FlowSet<AssignStmt> in1, FlowSet<AssignStmt> in2, FlowSet<AssignStmt> out) {
        //may analysis
        in1.union(in2,out);
    }

    @Override
    protected void copy(FlowSet<AssignStmt> source, FlowSet<AssignStmt> dest) {
        source.copy(dest);
    }
    public void showResult( DirectedGraph<Unit> g){
        for (Unit u : g) {
            FlowSet<AssignStmt> in = getFlowBefore(u);
            FlowSet<AssignStmt> out = getFlowAfter(u);
            System.out.println("Unit: " + u);
            System.out.println("  IN: " + in);
            System.out.println("  OUT: " + out);
        }
    }
}
