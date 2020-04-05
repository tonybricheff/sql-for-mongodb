package main;


public class Main {
    public static void main(String[] args) {
        SQLTranslator sqlTranslator = new SQLTranslator();
        System.out.println(sqlTranslator.convert("SELECT COUNT(name, age) FROM people WHERE status = 'GT' ORDER BY user_id DESC"));
    }
}
