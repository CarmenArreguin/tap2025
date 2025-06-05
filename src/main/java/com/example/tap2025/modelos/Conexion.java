package com.example.tap2025.modelos;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    private static String DB = "restaurantec";
    private static String USER = "admin2";
    private static String PWD = "1234";
    private static String HOST = "localhost"; //127.0.0.1 (loopback)
    private static String PORT = "3306"; //Esto es para MySQL

    public static Connection connection;

    //metodo para la conexión a la base de datos.
    //Se usa public static para que sea solo una instancia de la clase, solo se mande a llamar.
    //A fuerza se usa un try y catch.


    public static void createConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DB, USER, PWD);
            //Un socket es un mecanismo de comunicación entre dos aplicaciones del mismo (ejemplo: java a java) o diferente tipo.
            System.out.println("Conexión establecida :)");
        }catch (Exception e){
            //Para hacer seguimiento del error.
            e.printStackTrace();
        }
    }
}
