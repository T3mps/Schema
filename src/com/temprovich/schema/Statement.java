package com.temprovich.schema;

import java.util.List;

import com.temprovich.schema.lexer.Token;

public abstract class Statement {

    public interface Visitor<R> {

        public abstract R visit(Block statement);

        public abstract R visit(Expr statement);

        public abstract R visit(Node statement);

        public abstract R visit(Trait statement);

        public abstract R visit(Function statement);

        public abstract R visit(Auto statement);

        public abstract R visit(If statement);

        public abstract R visit(Return statement);

        public abstract R visit(While statement);

        public abstract R visit(Break statement);

        public abstract R visit(Continue statement);

        public abstract R visit(Use statement);
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
            return visitor.visit(this);
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
            return visitor.visit(this);
        }
    }

    public static class Node extends Statement {

        private final Token name;
        private final Expression.Variable parent;
        private final List<Expression> traits;
        private final List<Statement.Function> methods;
        private final List<Statement.Function> metaMethods;

        public Node(Token name, Expression.Variable parent, List<Expression> traits, List<Statement.Function> methods, List<Statement.Function> metaMethods) {
            this.name = name;
            this.parent = parent;
            this.traits = traits;
            this.methods = methods;
            this.metaMethods = metaMethods;
        }

        public Token name() {
            return name;
        }

        public Expression.Variable parent() {
            return parent;
        }

        public List<Expression> traits() {
            return traits;
        }

        public List<Statement.Function> methods() {
            return methods;
        }

        public List<Statement.Function> metaMethods() {
            return metaMethods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Trait extends Statement {

        private final Token name;
        private final List<Expression> traits;
        private final List<Statement.Function> methods;

        public Trait(Token name, List<Expression> traits, List<Statement.Function> methods) {
            this.name = name;
            this.traits = traits;
            this.methods = methods;
        }

        public Token name() {
            return name;
        }

        public List<Expression> traits() {
            return traits;
        }

        public List<Statement.Function> methods() {
            return methods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Function extends Statement {

        private final Token name;
        private final Expression.Function function;

        public Function(Token name, Expression.Function function) {
            this.name = name;
            this.function = function;
        }

        public Token name() {
            return name;
        }

        public Expression.Function function() {
            return function;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return "Function [name=" + name.lexeme() + ", function=" + function + "]";
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
            return visitor.visit(this);
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
            return visitor.visit(this);
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
            return visitor.visit(this);
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
            return visitor.visit(this);
        }
    }

    public static class Break extends Statement {

        public Break() {
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Continue extends Statement {

        public Continue() {
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Use extends Statement {

        private final List<Token> modules;

        public Use(List<Token> modules) {
            this.modules = modules;
        }

        public List<Token> modules() {
            return modules;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}

