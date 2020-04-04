package ru.ifmo.rain.brichev;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Main {

    static class Translator {
        private int selectPosition;
        private int fromPosition;
        private int limitPosition;
        private int offsetPosition;

        private void parseMainOperators(String sqlCommand) {
            selectPosition = sqlCommand.indexOf("SELECT");
            fromPosition = sqlCommand.indexOf("FROM");
            limitPosition = sqlCommand.indexOf("LIMIT");
            offsetPosition = sqlCommand.indexOf("OFFSET");
        }

        private List<String> getColumns(String sqlCommand) {
            List<String> columns = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(selectPosition + 6, fromPosition), " ,");
            while (stringTokenizer.hasMoreTokens())
                columns.add(stringTokenizer.nextToken());
            return columns;
        }

        private String parseToken(String sqlCommand, int position) {
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(position));
            return stringTokenizer.nextToken();
        }

        private void addColumns(StringBuilder mongoCommand, List<String> columns) {
            mongoCommand.append(", {");
            for (String name : columns)
                mongoCommand.append(name).append(": 1, ");
            mongoCommand.deleteCharAt(mongoCommand.length() - 2).deleteCharAt(mongoCommand.length() - 1);
            mongoCommand.append("})");
        }


        private String createMongoFind(String sqlCommand, String tableName, List<String> columns) {
            StringBuilder mongoCommand = new StringBuilder();
            mongoCommand.append("db.").append(tableName).append(".find({}");
            if (!columns.get(0).equals("*")) {
                addColumns(mongoCommand, columns);
            } else
                mongoCommand.append(")");
            if (offsetPosition != -1)
                mongoCommand.append(".skip(").append(parseToken(sqlCommand, offsetPosition + 6)).append(")");
            if (limitPosition != -1)
                mongoCommand.append(".limit(").append(parseToken(sqlCommand, limitPosition + 5)).append(")");

            return mongoCommand.toString();

        }


        public void convert(String sqlCommand) {
            parseMainOperators(sqlCommand);
            System.out.println(createMongoFind(sqlCommand, parseToken(sqlCommand, fromPosition + 5), getColumns(sqlCommand)));


        }


    }


    public static void main(String[] args) {
        Translator translator = new Translator();
        translator.convert("SELECT name, surname, job FROM workers");
        translator.convert("SELECT name FROM collection OFFSET 5 LIMIT 10");


    }
}
