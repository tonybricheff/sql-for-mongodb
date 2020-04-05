package main;


import main.models.PredicatePair;

import java.sql.SQLException;
import java.util.*;

public class SQLTranslator {

    // positions of operators
    private int selectPosition;
    private int fromPosition;
    private int limitPosition;
    private int offsetPosition;
    private int wherePosition;


    // public constructor
    public SQLTranslator() {
        this.selectPosition = -1;
        this.fromPosition = -1;
        this.limitPosition = -1;
        this.offsetPosition = -1;
        this.wherePosition = -1;
    }


    /*
        finds operators positions and throws exception if there are no select or from operators
     */

    private void getOperatorsPositions(String sqlCommand) throws SQLException {
        selectPosition = sqlCommand.indexOf("SELECT");
        fromPosition = sqlCommand.indexOf("FROM");
        limitPosition = sqlCommand.indexOf("LIMIT");
        offsetPosition = sqlCommand.indexOf("OFFSET");
        wherePosition = sqlCommand.indexOf("WHERE");

        if (selectPosition == -1 || fromPosition == -1) {
            System.out.println("Invalid command format");
            throw new SQLException();
        }
    }


    /*
        parse command and returns list with names of columns after SELECT
     */

    private List<String> getColumns(String sqlCommand) {
        List<String> columns = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(selectPosition + 6, fromPosition), " ,");
        while (stringTokenizer.hasMoreTokens())
            columns.add(stringTokenizer.nextToken());
        return columns;
    }


    /*
        finds a position where predicates ends
     */

    private int findLastPosition(String sqlCommand) {
        if (offsetPosition != -1 && limitPosition != -1) {
            return Math.min(offsetPosition, limitPosition);
        } else if (offsetPosition != -1) {
            return offsetPosition;
        } else if (limitPosition != -1) {
            return limitPosition;
        } else
            return sqlCommand.length();
    }


    /*
        parse and returns predicates after WHERE
     */

    private Map<String, List<PredicatePair>> getPredicates(String sqlCommand) {
        Map<String, List<PredicatePair>> predicates = new HashMap<>();

        StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(wherePosition + 5, findLastPosition(sqlCommand)), " AND");

        while (stringTokenizer.hasMoreTokens()) {
            String param = stringTokenizer.nextToken();
            String operation = stringTokenizer.nextToken();
            String value = stringTokenizer.nextToken();
            if (!predicates.containsKey(param)) {
                List<PredicatePair> listOfPredicates = new ArrayList<>();
                predicates.put(param, listOfPredicates);
            }
            predicates.get(param).add(new PredicatePair(operation, value));
        }


        return predicates;
    }


    /*
        finds and returns necessary mongo command for sql predicate
     */

    private String validatePredicate(String predicate, String value) throws UnsupportedOperationException {
        switch (predicate) {
            case "=":
                return value;
            case ">":
                return "$gt: " + value;
            case "<":
                return "$lt: " + value;
            case "<>":
                return "$ne: " + value;
            default:
                throw new UnsupportedOperationException();
        }
    }


    /*
        parse and returns the next token after position
     */

    private String parseToken(String sqlCommand, int position) {
        StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(position));
        return stringTokenizer.nextToken();
    }


    /*
        adds sql columns to MongoDB command
     */

    private void addColumns(StringBuilder mongoCommand, List<String> columns) {
        mongoCommand.append(", {");
        for (String name : columns)
            mongoCommand.append(name).append(": 1, ");
        mongoCommand.deleteCharAt(mongoCommand.length() - 2).deleteCharAt(mongoCommand.length() - 1);
        mongoCommand.append("})");
    }


    /*
        adds predicates to MongoDB command
     */

    private void addPredicates(StringBuilder mongoCommand, Map<String, List<PredicatePair>> predicates) {

        for (Map.Entry<String, List<PredicatePair>> predicate : predicates.entrySet()) {
            mongoCommand.append(predicate.getKey()).append(": ");

            if (predicate.getValue().size() == 1) {
                if (predicate.getValue().get(0).getOperation().equals("=")) {
                    mongoCommand.append(validatePredicate(predicate.getValue().get(0).getOperation(),
                            predicate.getValue().get(0).getValue())).append(", ");
                } else {
                    mongoCommand.append("{").append(validatePredicate(predicate.getValue().get(0).getOperation(),
                            predicate.getValue().get(0).getValue())).append("}, ");
                }
            } else {
                mongoCommand.append("{");
                for (PredicatePair parameters : predicate.getValue()) {
                    mongoCommand.append(validatePredicate(parameters.getOperation(), parameters.getValue())).append(", ");
                }
                mongoCommand.append("}");
            }
        }

        mongoCommand.deleteCharAt(mongoCommand.length() - 2).deleteCharAt(mongoCommand.length() - 1);
        mongoCommand.append("}");
    }


    /*
        creates MongoDB find command from SQL command
     */

    private String createMongoFind(String sqlCommand) {
        StringBuilder mongoCommand = new StringBuilder();
        mongoCommand.append("db.").append(parseToken(sqlCommand, fromPosition + 5)).append(".find({");
        if (wherePosition != -1) {
            addPredicates(mongoCommand, getPredicates(sqlCommand));
        } else {
            mongoCommand.append("}");
        }
        List<String> columns = getColumns(sqlCommand);
        if (!columns.get(0).equals("*")) {
            addColumns(mongoCommand, columns);
        } else {
            mongoCommand.append(")");
        }
        if (offsetPosition != -1)
            mongoCommand.append(".skip(").append(parseToken(sqlCommand, offsetPosition + 6)).append(")");
        if (limitPosition != -1)
            mongoCommand.append(".limit(").append(parseToken(sqlCommand, limitPosition + 5)).append(")");

        return mongoCommand.toString();

    }


    /*
        shell of translator, gets SQL command and returns MongoDB command after translation
     */

    public String convert(String sqlCommand) {
        try {
            getOperatorsPositions(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        return createMongoFind(sqlCommand);
    }


}