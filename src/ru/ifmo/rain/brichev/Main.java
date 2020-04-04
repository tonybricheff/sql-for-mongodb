package ru.ifmo.rain.brichev;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Main {

    static class Translator {
        private int selectPosition;
        private int fromPosition;

        private void parseMainOperators(String sqlCommand) {
            selectPosition = sqlCommand.indexOf("SELECT");
            fromPosition = sqlCommand.indexOf("FROM");

        }

        private List<String> getColumns(String sqlCommand) {
            List<String> columns = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(selectPosition + 6, fromPosition), " ,");
            while (stringTokenizer.hasMoreTokens())
                columns.add(stringTokenizer.nextToken());
            return columns;
        }

        private String getTableName(String sqlCommand) {
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(fromPosition + 5));
            return stringTokenizer.nextToken();
        }

        private void addColumns(StringBuilder mongoCommand, List<String> columns) {
            mongoCommand.append(", {");
            for (String name : columns)
                mongoCommand.append(name).append(": 1, ");
            mongoCommand.deleteCharAt(mongoCommand.length() - 2).deleteCharAt(mongoCommand.length() - 1);
            mongoCommand.append("})");
        }

        private String createMongoFind(String tableName, List<String> columns) {
            StringBuilder mongoCommand = new StringBuilder();
            mongoCommand.append("db.").append(tableName).append(".find({}");
            if (!columns.get(0).equals("*")) {
                addColumns(mongoCommand, columns);
            } else
                mongoCommand.append(")");
            return mongoCommand.toString();

        }

        public void convert(String sqlCommand) {
            parseMainOperators(sqlCommand);
            System.out.println(createMongoFind(getTableName(sqlCommand), getColumns(sqlCommand)));


        }


    }


    public static void main(String[] args) {
        Translator translator = new Translator();
        translator.convert("SELECT name, surname, job FROM workers");
        translator.convert("SELECT * FROM sales ");


    }
}
