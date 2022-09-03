package com.temprovich.e30;

import java.util.List;

public abstract class Statement {

    public interface Visitor<R> {

        public abstract R visitBlockStatement(Block statement);

        public abstract R visitExprStatement(Expr statement);

        public abstract R visitFunctionStatement(Function statement);

        public abstract R visitIfStatement(If statement);

        public abstract R visitAutoStatement(Auto statement);

        public abstract R visitWhileStatement(While statement);

        public abstract R visitReturnStatement(Return statement);
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

    public static class Function extends Statement {

        private final Token name;
        private final List<Token> parameters;
        private final List<Statement> body;

        public Function(Token name, List<Token> parameters, List<Statement> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        public Token name() {
            return name;
        }

        public List<Token> parameters() {
            return parameters;
        }

        public List<Statement> body() {
            return body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }
    }

    public static class If extends Statement {

        private final Expression condition;
        private final Statement thenBranch;
        private final Statement elseBranch;

        public If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public Expression condition() {
            return condition;
        }

        public Statement thenBranch() {
            return thenBranch;
        }

        public Statement elseBranch() {
            return elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
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

    public static class While extends Statement {

        private final Expression condition;
        private final Statement body;

        public While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    public static class Return extends Statement {

        private final Token keyword;
        private final Expression value;

        public Return(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        public Token keyword() {
            return keyword;
        }

        public Expression value() {
            return value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}

