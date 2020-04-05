package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import main.SQLTranslator;


class SQLTranslatorTest {

    private SQLTranslator sqlTranslator;

    SQLTranslatorTest() {
        sqlTranslator = new SQLTranslator();
    }

    @Test
    public void generalTest() {
        Assertions.assertEquals("db.tests.find({})", sqlTranslator.convert("SELECT * FROM tests"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1})", sqlTranslator.convert("SELECT name, surname FROM collection"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1, job: 1, salary: 1})", sqlTranslator.convert("SELECT name, surname, job, salary FROM collection"));
    }

    @Test
    public void offsetTest() {
        Assertions.assertEquals("db.tests.find({}).skip(999)", sqlTranslator.convert("SELECT * FROM tests OFFSET 999"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1}).skip(333)", sqlTranslator.convert("SELECT name, surname FROM collection OFFSET 333"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1, job: 1, salary: 1}).skip(1)", sqlTranslator.convert("SELECT name, surname, job, salary FROM collection OFFSET 1"));
    }

    @Test
    public void limitTest() {
        Assertions.assertEquals("db.tests.find({}).limit(999)", sqlTranslator.convert("SELECT * FROM tests LIMIT 999"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1}).limit(333)", sqlTranslator.convert("SELECT name, surname FROM collection LIMIT 333"));
        Assertions.assertEquals("db.collection.find({}, {name: 1, surname: 1, job: 1, salary: 1}).limit(1)", sqlTranslator.convert("SELECT name, surname, job, salary FROM collection LIMIT 1"));
    }

    @Test
    public void whereTest() {
        Assertions.assertEquals("db.customers.find({name: 'Vasya', age: {$gt: 22}})", sqlTranslator.convert("SELECT * FROM customers WHERE age > 22 AND name = 'Vasya'"));
        Assertions.assertEquals("db.customers.find({name: {$ne: 'Petya'}, age: {$lt: 22}})", sqlTranslator.convert("SELECT * FROM customers WHERE age < 22 AND name <> 'Petya'"));
        Assertions.assertEquals("db.customers.find({name: {$ne: '123'}, salary: {$gt: 500}, job: 'Teacher', age: {$lt: 22}})", sqlTranslator.convert("SELECT * FROM customers WHERE age < 22 AND name <> '123' AND salary > 500 AND job = 'Teacher'"));
        Assertions.assertEquals("db.customers.find({age: {$gt: 22, $lt: 50, $ne: 30}}, {age: 1})", sqlTranslator.convert("SELECT age FROM customers WHERE age > 22 AND age < 50 AND age <> 30"));
        Assertions.assertEquals("db.customers.find({name: 'Tony', age: {$gt: 22, $lt: 50, $ne: 30}}, {age: 1, name: 1})", sqlTranslator.convert("SELECT age, name FROM customers WHERE age > 22 AND age < 50 AND age <> 30 AND name = 'Tony'"));


    }

    @Test
    public void mixedTest() {
        Assertions.assertEquals("db.customers.find({name: 'Vasya', age: {$gt: 22}}, {name: 1, age: 1}).skip(50).limit(10)", sqlTranslator.convert("SELECT name, age FROM customers WHERE age > 22 AND name = 'Vasya' OFFSET 50 LIMIT 10"));
    }

    @Test
    public void invalidCommandTest(){
        Assertions.assertEquals("", sqlTranslator.convert("name, age FROM customers WHERE age > 22 AND name = 'Vasya' OFFSET 50 LIMIT 10"));
        Assertions.assertEquals("", sqlTranslator.convert("SELECT name, age customers WHERE age > 22 AND name = 'Vasya' OFFSET 50 LIMIT 10"));
    }

}