package com.temprovich.e30;

import java.util.List;

public abstract class Expression {

    public interface Visitor<R> {

        public abstract R visit(Literal expression);

        public abstract R visit(Grouping expression);

        public abstract R visit(Variable expression);

        public abstract R visit(Assign expression);

        public abstract R visit(Unary expression);

        public abstract R visit(Binary expression);

        public abstract R visit(Logical expression);

        public abstract R visit(Call expression);

        public abstract R visit(Function expression);

        public abstract R visit(Attribute expression);

        public abstract R visit(Set expression);

        public abstract R visit(Self expression);

        public abstract R visit(Parent expression);
    }

    public static class Literal extends Expression {

        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object value() {
            return value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Grouping extends Expression {

        private final Expression expression;

        public Grouping(Expression expression) {
            this.expression = expression;
        }

        public Expression expression() {
            return expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Variable extends Expression {

        private final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public Token name() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Assign extends Expression {

        private final Token name;
        private final Expression value;

        public Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        public Token name() {
            return name;
        }

        public Expression value() {
            return value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Unary extends Expression {

        private final Token operator;
        private final Expression right;

        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        public Token operator() {
            return operator;
        }

        public Expression right() {
            return right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Binary extends Expression {

        private final Expression left;
        private final Token operator;
        private final Expression right;

        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression left() {
            return left;
        }

        public Token operator() {
            return operator;
        }

        public Expression right() {
            return right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Logical extends Expression {

        private final Expression left;
        private final Token operator;
        private final Expression right;

        public Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression left() {
            return left;
        }

        public Token operator() {
            return operator;
        }

        public Expression right() {
            return right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Call extends Expression {

        private final Expression callee;
        private final Token paren;
        private final List<Expression> arguments;

        public Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        public Expression callee() {
            return callee;
        }

        public Token paren() {
            return paren;
        }

        public List<Expression> arguments() {
            return arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Function extends Expression {

        private final List<Token> parameters;
        private final List<Statement> body;

        public Function(List<Token> parameters, List<Statement> body) {
            this.parameters = parameters;
            this.body = body;
        }

        public List<Token> parameters() {
            return parameters;
        }

        public List<Statement> body() {
            return body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Attribute extends Expression {

        private final Expression object;
        private final Token name;

        public Attribute(Expression object, Token name) {
            this.object = object;
            this.name = name;
        }

        public Expression object() {
            return object;
        }

        public Token name() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Set extends Expression {

        private final Expression object;
        private final Token name;
        private final Expression value;

        public Set(Expression object, Token name, Expression value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        public Expression object() {
            return object;
        }

        public Token name() {
            return name;
        }

        public Expression value() {
            return value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Self extends Expression {

        private final Token keyword;

        public Self(Token keyword) {
            this.keyword = keyword;
        }

        public Token keyword() {
            return keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Parent extends Expression {

        private final Token keyword;
        private final Token method;

        public Parent(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        public Token keyword() {
            return keyword;
        }

        public Token method() {
            return method;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}

