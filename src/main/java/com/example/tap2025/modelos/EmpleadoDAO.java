package com.example.tap2025.modelos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class EmpleadoDAO {
    private int idEmpleado;
    private String nombres;
    private String apellidos;
    private String curp;
    private String rfc;
    private double sueldo;
    private String puesto;
    private String celular;
    private String nss;
    private Date fechaIngreso;
    private String password;
    private boolean activo = true;

    public int getIdEmpleado() {
        return idEmpleado;
    }
    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombres() {
        return nombres;
    }
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCurp() {
        return curp;
    }
    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getRfc() {
        return rfc;
    }
    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public double getSueldo() {
        return sueldo;
    }
    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    public String getPuesto() {
        return puesto;
    }
    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getCelular() {
        return celular;
    }
    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getNss() {
        return nss;
    }
    public void setNss(String nss) {
        this.nss = nss;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }
    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void INSERT() {
        String query = "INSERT INTO empleados (nombres, apellidos, curp, rfc, sueldo, puesto, celular, nss, fecha_ingreso, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = Conexion.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nombres);
            pstmt.setString(2, apellidos);
            pstmt.setString(3, curp);
            pstmt.setString(4, rfc);
            pstmt.setDouble(5, sueldo);
            pstmt.setString(6, puesto);
            pstmt.setString(7, celular);
            pstmt.setString(8, nss);
            pstmt.setDate(9, fechaIngreso);
            pstmt.setString(10, puesto.equals("Mesero") || puesto.equals("Gerente") || puesto.equals("Administrador") ? password : null);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idEmpleado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void UPDATE() {
        String query = "UPDATE empleados SET nombres=?, apellidos=?, curp=?, rfc=?, sueldo=?, puesto=?, celular=?, nss=?, fecha_ingreso=?, password=? " +
                "WHERE id_empleado=?";
        try (PreparedStatement pstmt = Conexion.connection.prepareStatement(query)) {
            pstmt.setString(1, nombres);
            pstmt.setString(2, apellidos);
            pstmt.setString(3, curp);
            pstmt.setString(4, rfc);
            pstmt.setDouble(5, sueldo);
            pstmt.setString(6, puesto);
            pstmt.setString(7, celular);
            pstmt.setString(8, nss);
            pstmt.setDate(9, fechaIngreso);
            pstmt.setString(10, puesto.equals("Mesero") || puesto.equals("Gerente") || puesto.equals("Administrador") ? password : null);
            pstmt.setInt(11, idEmpleado);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void DELETE() {
        String query = "UPDATE empleados SET activo = false WHERE id_empleado=?";
        try (PreparedStatement pstmt = Conexion.connection.prepareStatement(query)) {
            pstmt.setInt(1, idEmpleado);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<EmpleadoDAO> SELECT() {
        ObservableList<EmpleadoDAO> lista = FXCollections.observableArrayList();
        String query = "SELECT * FROM empleados WHERE activo = true";

        try (Statement stmt = Conexion.connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                EmpleadoDAO emp = new EmpleadoDAO();
                emp.setIdEmpleado(rs.getInt("id_empleado"));
                emp.setNombres(rs.getString("nombres"));
                emp.setApellidos(rs.getString("apellidos"));
                emp.setCurp(rs.getString("curp"));
                emp.setRfc(rs.getString("rfc"));
                emp.setSueldo(rs.getDouble("sueldo"));
                emp.setPuesto(rs.getString("puesto"));
                emp.setCelular(rs.getString("celular"));
                emp.setNss(rs.getString("nss"));
                emp.setFechaIngreso(rs.getDate("fecha_ingreso"));
                emp.setPassword(rs.getString("password"));
                emp.setActivo(rs.getBoolean("activo"));
                lista.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    //Metodo para identificar empleado por contraseña
    public static EmpleadoDAO autenticarPorPassword(String password) {
        String query = "SELECT * FROM empleados WHERE password = ? AND (puesto = 'Mesero' OR puesto = 'Gerente' OR puesto = 'Administrador')";

        try (PreparedStatement pstmt = Conexion.connection.prepareStatement(query)) {
            pstmt.setString(1, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                EmpleadoDAO emp = new EmpleadoDAO();
                emp.setIdEmpleado(rs.getInt("id_empleado"));
                emp.setNombres(rs.getString("nombres"));
                emp.setApellidos(rs.getString("apellidos"));
                emp.setPuesto(rs.getString("puesto"));
                return emp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
