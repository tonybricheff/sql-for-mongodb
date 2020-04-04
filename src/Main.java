import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Main {

    static class Translator {
        private int selectPosition;
        private int fromPosition;
        private int limitPosition;
        private int offsetPosition;
        private int wherePosition;

        Translator() {
            this.selectPosition = -1;
            this.fromPosition = -1;
            this.limitPosition = -1;
            this.offsetPosition = -1;
            this.wherePosition = -1;
        }

        private void parseOperators(String sqlCommand) throws SQLException {
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

        private List<String> getColumns(String sqlCommand) {
            List<String> columns = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(selectPosition + 6, fromPosition), " ,");
            while (stringTokenizer.hasMoreTokens())
                columns.add(stringTokenizer.nextToken());
            return columns;
        }

        private int findLastPosition(String sqlCommand) {
            if (offsetPosition != -1 && limitPosition != -1) {
                return Math.min(offsetPosition, limitPosition);
            } else if (offsetPosition != -1) {
                return offsetPosition;
            } else if (limitPosition != 1) {
                return limitPosition;
            } else
                return sqlCommand.length() - 1;
        }


        private List<String> getPredicates(String sqlCommand) {
            List<String> predicates = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(sqlCommand.substring(wherePosition + 5, findLastPosition(sqlCommand)), " AND");

            while (stringTokenizer.hasMoreTokens())
                predicates.add(stringTokenizer.nextToken());
            System.out.println(predicates);
            return predicates;
        }

        private String validatePredicate(String predicate, String value) throws UnsupportedOperationException {
            switch (predicate) {
                case "=":
                    return value;
                case ">":
                    return "{$gt: " + value + "}";
                case "<":
                    return "{$lt: " + value + "}";
                case "<>":
                    return "{$ne: " + value + "}";
                default:
                    throw new UnsupportedOperationException();
            }
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

        private void addPredicates(StringBuilder mongoCommand, List<String> predicates) {
            for (int i = 0; i < predicates.size() - 2; i += 3) {
                mongoCommand.append(predicates.get(i)).append(": ").append(validatePredicate(predicates.get(i + 1),
                        predicates.get(i + 2))).append(", ");
            }

            mongoCommand.deleteCharAt(mongoCommand.length() - 2).deleteCharAt(mongoCommand.length() - 1);
            mongoCommand.append("}");
        }


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


        public void convert(String sqlCommand) {
            try {
                parseOperators(sqlCommand);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            System.out.println(createMongoFind(sqlCommand));
        }


    }


    public static void main(String[] args) {
        Translator translator = new Translator();
        translator.convert("SELECT name, surname, job FROM workers");
        translator.convert("SELECT name FROM collection OFFSET 5 LIMIT 10");
        translator.convert("SELECT * FROM customers WHERE age > 22 AND name = 'Vasya' AND age <> 35 AND salary < 455 OFFSET 10 LIMIT 50");
        translator.convert("SELECT age FROM customers WHERE age > 22 AND name = 'Vasya' AND age <> 35 AND salary < 455 OFFSET 10");

    }
}
