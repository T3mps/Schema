package com.temprovich.e30;

public abstract class Expression {

    public interface Visitor<R> {
        public abstract R visitBinaryExpression(Binary binary);
        public abstract R visitGroupingExpression(Grouping grouping);
        public abstract R visitLiteralExpression(Literal literal);
        public abstract R visitUnaryExpression(Unary unary);
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
            return visitor.visitBinaryExpression(this);
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
            return visitor.visitGroupingExpression(this);
        }
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
            return visitor.visitLiteralExpression(this);
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
            return visitor.visitUnaryExpression(this);
        }
    }


    public abstract <R> R accept(Visitor<R> visitor);
}
