package main;


public class Main {
    public static void main(String[] args) {
        SQLTranslator sqlTranslator = new SQLTranslator();
        System.out.println(sqlTranslator.convert("SELECT * FROM people WHERE age > 25 AND age < 50 OFFSET 50"));
    }
}
