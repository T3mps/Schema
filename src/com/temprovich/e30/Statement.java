package com.temprovich.e30;

import java.util.List;

public abstract class Statement {

    public interface Visitor<R> {

        public abstract R visitBlockStatement(Block block);

        public abstract R visitExprStatement(Expr expr);

        public abstract R visitPrintStatement(Print print);

        public abstract R visitAutoStatement(Auto auto);
    }

    public static class Block extends Statement {

        private final List<Statement> statements;

        public Block(List<Statement> statements) {
            this.statements = statements;
        }

        public List<Statement> statements() {
            return statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    public static class Expr extends Statement {

        private final Expression expression;

        public Expr(Expression expression) {
            this.expression = expression;
        }

        public Expression expression() {
            return expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }
    }

    public static class Print extends Statement {

        private final Expression expression;

        public Print(Expression expression) {
            this.expression = expression;
        }

        public Expression expression() {
            return expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    public static class Auto extends Statement {

        private final Token name;
        private final Expression value;

        public Auto(Token name, Expression value) {
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
            return visitor.visitAutoStatement(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}

