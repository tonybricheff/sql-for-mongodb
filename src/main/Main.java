package main;


public class Main {
    public static void main(String[] args) {
        SQLTranslator sqlTranslator = new SQLTranslator();
        System.out.println(sqlTranslator.convert("SELECT * FROM sales LIMIT 10"));
    }
}
