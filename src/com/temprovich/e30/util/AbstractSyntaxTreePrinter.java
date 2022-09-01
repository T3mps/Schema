package com.temprovich.e30.util;

import com.temprovich.e30.Expression;
import com.temprovich.e30.Token;
import com.temprovich.e30.TokenType;
import com.temprovich.e30.Expression.Binary;
import com.temprovich.e30.Expression.Grouping;
import com.temprovich.e30.Expression.Literal;
import com.temprovich.e30.Expression.Unary;

public class AbstractSyntaxTreePrinter implements Expression.Visitor<String> {

    public String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpression(Binary binary) {
        return parenthesize(binary.operator().lexeme(), binary.left(), binary.right());
    }

    @Override
    public String visitGroupingExpression(Grouping grouping) {
        return parenthesize("group", grouping.expression());
    }

    @Override
    public String visitLiteralExpression(Literal literal) {
        if (literal.value() == null) {
            return "null";
        }

        return literal.value().toString();
    }

    @Override
    public String visitUnaryExpression(Unary unary) {
        return parenthesize(unary.operator().lexeme(), unary.right());
    }   

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expression : expressions) {
            builder.append("\s");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
