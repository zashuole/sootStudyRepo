package com.squirtle.intraConstant;

import soot.Local;

import java.util.Objects;

public class ValueDomain {

    public enum ValueType {
        UNDEF,    // 未定义
        CON,    // 常量
        NAC       // 非常量
    }
    private ValueType type;
    private  Integer constValue; // 如果是 CON，则存具体常量值
    private Local local;
    //构造函数
    ValueDomain(ValueType valueType, Integer constValue, Local local) {
        this.type = valueType;
        this.constValue = constValue;
        this.local = local;
    }
    public static ValueDomain undef(Local local){
        return new ValueDomain(ValueType.UNDEF,null,local);
    }
    public static ValueDomain con(Local local,Integer v){
        return new ValueDomain(ValueType.CON,v,local);
    }
    public static ValueDomain nac(Local local){
        return new ValueDomain(ValueType.NAC,null,local);
    }


    //获取其type
    public ValueType getType() {
        return type;
    }
    //获取其常量值
    public Integer getConstValue() {
        return constValue;
    }
    //获取局部变量
    public Local getLocal() {
        return local;
    }

    // 两个 ValueDomain 合并
    public ValueDomain mergeValues(ValueDomain v1, ValueDomain v2) {
        if (v1 == null) return v2;
        if (v2 == null) return v1;

        // 两边都是常量且相等
        if (v1.getType() == ValueDomain.ValueType.CON &&
                v2.getType() == ValueDomain.ValueType.CON &&
                v1.getConstValue().equals(v2.getConstValue())) {
            return ValueDomain.con(v1.getLocal(), v1.getConstValue());
        }

        // 其中一边 UNDEF → 用另一边
        if (v1.getType() == ValueDomain.ValueType.UNDEF) return v2;
        if (v2.getType() == ValueDomain.ValueType.UNDEF) return v1;

        // 常量不同或有 NAC → NAC
        return ValueDomain.nac(v1.getLocal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueDomain that = (ValueDomain) o;
        return type == that.type && Objects.equals(constValue, that.constValue) && Objects.equals(local, that.local);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, constValue, local);
    }

    @Override
    public String toString() {
        return "ValueDomain{" +
                "local=" + local +
                ",type=" + type +
                ", constValue=" + constValue +
                '}';
    }
}
