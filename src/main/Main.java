package main;


public class Main {
    public static void main(String[] args) {
        SQLTranslator translator = new SQLTranslator();
        translator.convert("SELECT name, surname, job FROM workers");
        translator.convert("SELECT name FROM collection OFFSET 5 LIMIT 10");
        translator.convert("SELECT * FROM customers WHERE age > 22 AND name = 'Vasya' AND age <> 35 AND salary < 455 OFFSET 10 LIMIT 50");
        translator.convert("SELECT age FROM customers WHERE age > 22 AND name = 'Petya' AND age <> 35 AND salary < 455 OFFSET 10");

    }
}
