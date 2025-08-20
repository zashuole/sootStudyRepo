package com.squirtle.reachingDefinitions;

import polyglot.ast.Assign;
import soot.EquivTo;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.toolkits.scalar.AbstractFlowSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 很忙表达式分析专用 FlowSet
 * 支持基于 EquivTo 的语义等价判断
 */
public class ValueArraySparseSet extends AbstractFlowSet<AssignStmt> {

    private AssignStmt[] elements;
    private int numElements;
    private static final int DEFAULT_SIZE = 8;

    public ValueArraySparseSet() {
        elements = new AssignStmt[DEFAULT_SIZE];
        numElements = 0;
    }

    // 用于 clone
    private ValueArraySparseSet(ValueArraySparseSet other) {
        this.numElements = other.numElements;
        this.elements = new AssignStmt[other.elements.length];
        System.arraycopy(other.elements, 0, this.elements, 0, other.numElements);
    }

    @Override
    public void add(AssignStmt obj) {
        if (!contains(obj)) {
            if (numElements == elements.length) {
                expand();
            }
            elements[numElements++] = obj;
        }
    }

    @Override
    public void remove(AssignStmt obj) {
        for (int i = 0; i < numElements; i++) {
            if (elements[i] instanceof EquivTo && ((EquivTo) elements[i]).equivTo(obj)) {
                removeAt(i);
                return;
            } else if (elements[i].equals(obj)) {
                removeAt(i);
                return;
            }
        }
    }

    private void removeAt(int index) {
        numElements--;
        elements[index] = elements[numElements];
        elements[numElements] = null;
    }

    @Override
    public boolean contains(AssignStmt obj) {
        for (int i = 0; i < numElements; i++) {
            if (elements[i] instanceof EquivTo && ((EquivTo) elements[i]).equivTo(obj)) {
                return true;
            } else if (elements[i].equals(obj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<AssignStmt> iterator() {
        return new Iterator<AssignStmt>() {
            private int idx = 0;
            private boolean canRemove = false;

            @Override
            public boolean hasNext() {
                return idx < numElements;
            }

            @Override
            public AssignStmt next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                canRemove = true;
                return elements[idx++];
            }

            @Override
            public void remove() {
                if (!canRemove) {
                    throw new IllegalStateException("next() has not been called yet");
                }
                removeAt(--idx);
                canRemove = false;
            }
        };
    }

    @Override
    public List<AssignStmt> toList() {
        List<AssignStmt> list = new ArrayList<>(numElements);
        for (int i = 0; i < numElements; i++) {
            list.add(elements[i]);
        }
        return list;
    }

    @Override
    public boolean isEmpty() {
        return numElements == 0;
    }

    @Override
    public int size() {
        return numElements;
    }

    @Override
    public ValueArraySparseSet clone() {
        return new ValueArraySparseSet(this);
    }

    private void expand() {
        AssignStmt[] newElements = new AssignStmt[elements.length * 2];
        System.arraycopy(elements, 0, newElements, 0, numElements);
        elements = newElements;
    }
}
