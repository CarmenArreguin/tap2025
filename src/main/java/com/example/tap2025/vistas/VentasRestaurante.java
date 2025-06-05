package com.example.tap2025.vistas;

import com.example.tap2025.modelos.EmpleadoDAO;
import com.example.tap2025.modelos.Producto;
import com.example.tap2025.modelos.Conexion;
import com.example.tap2025.utilidades.TicketPDF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class VentasRestaurante {
    private final Map<String, ObservableList<Producto>> pedidosPorMesa = new HashMap<>();
    private final ListView<String> listViewPedido = new ListView<>();
    private final Label lblTotal = new Label("Total: $0.0");
    private final Label lblMeseroAsignado = new Label("Mesero: No asignado");
    private final FlowPane mesaContainer = new FlowPane();
    private final ComboBox<String> comboBoxCategorias = new ComboBox<>();
    private final ComboBox<String> comboBoxClientes = new ComboBox<>();
    private final TilePane tilePaneProductos = new TilePane();
    private final Map<String, List<Producto>> categoriaProductos = new HashMap<>();
    private int mesaSeleccionada = 1;
    private final Map<Integer, Button> botonesMesas = new HashMap<>();
    private final Map<Integer, Boolean> mesasOcupadas = new HashMap<>();
    private EmpleadoDAO meseroActual;
    private VBox root;

    public void mostrar(Stage stage) {
        // Inicializar el contenedor principal solo una vez
        if (root == null) {
            root = new VBox(10);
            root.setPadding(new Insets(10));
        } else {
            root.getChildren().clear(); // Limpiar hijos existentes
        }

        // Reinicializar estado
        reiniciarEstado();

        // Configurar interfaz
        configurarInterfaz(stage);
    }

    private void reiniciarEstado() {
        pedidosPorMesa.clear();
        mesasOcupadas.clear();
        botonesMesas.clear();
        meseroActual = null;
        lblMeseroAsignado.setText("Mesero: No asignado");
        listViewPedido.getItems().clear();
        lblTotal.setText("Total: $0.0");
        tilePaneProductos.getChildren().clear();
        comboBoxClientes.getSelectionModel().clearSelection();
        comboBoxCategorias.getSelectionModel().clearSelection();
    }

    private void configurarInterfaz(Stage stage) {
        inicializarProductos();
        cargarClientes();
        mostrarMesas();

        // Configurar combobox de categorías
        comboBoxCategorias.getItems().setAll(categoriaProductos.keySet());
        comboBoxCategorias.setStyle("-fx-font-size: 18px;");
        comboBoxCategorias.setOnAction(e -> mostrarProductos());

        // Configurar tilePane
        tilePaneProductos.setHgap(10);
        tilePaneProductos.setVgap(10);
        tilePaneProductos.setPrefColumns(4);

        // Configurar botones y paneles
        Button btnGenerarTicket = crearBotonGenerarTicket(stage);
        Button btnGuardar = crearBotonGuardar();
        Button btnLimpiar = crearBotonLimpiar();

        // Panel de pedido
        VBox vboxPedido = new VBox(10);
        vboxPedido.setPadding(new Insets(10));
        vboxPedido.getChildren().addAll(
                new Label("Pedido actual"),
                lblMeseroAsignado,
                listViewPedido,
                lblTotal,
                btnGenerarTicket,
                btnGuardar,
                btnLimpiar
        );

        // Panel de productos con scroll
        ScrollPane scrollPaneProductos = new ScrollPane(tilePaneProductos);
        scrollPaneProductos.setFitToWidth(true);
        scrollPaneProductos.setPrefHeight(600);
        scrollPaneProductos.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Sección central
        HBox seccionCentral = new HBox(15, scrollPaneProductos, vboxPedido);

        // Agregar componentes al root
        root.getChildren().addAll(
                new Label("Cliente:"), comboBoxClientes,
                new Label("Seleccionar mesa:"), mesaContainer,
                new Label("Seleccionar categoría:"), comboBoxCategorias,
                seccionCentral
        );

        // Configurar escena
        Scene escena = new Scene(root, 1000, 700);
        stage.setScene(escena);
        stage.setTitle("Ventas Restaurante");
        stage.show();
    }

    private Button crearBotonGenerarTicket(Stage stage) {
        Button btn = new Button("Generar Ticket PDF");
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #EBC093;");
        btn.setOnAction(event -> generarTicket(stage));
        return btn;
    }

    private Button crearBotonGuardar() {
        Button btn = new Button("Guardar Pedido");
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #83CBFF;");
        btn.setOnAction(e -> guardarPedido());
        return btn;
    }

    private Button crearBotonLimpiar() {
        Button btn = new Button("Limpiar Pedido");
        btn.setStyle("-fx-font-size: 14px; -fx-background-color: #EF9A9A;");
        btn.setOnAction(e -> limpiarPedido());
        return btn;
    }

    private void cargarClientes() {
        ObservableList<String> clientes = FXCollections.observableArrayList();
        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }
            Statement stmt = Conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nomCte FROM clientes");
            while (rs.next()) {
                clientes.add(rs.getString("nomCte"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboBoxClientes.setItems(clientes);
    }

    private void mostrarMesas() {
        mesaContainer.getChildren().clear();
        mesaContainer.setHgap(10);
        mesaContainer.setVgap(10);

        for (int i = 1; i <= 20; i++) {
            int numMesa = i;
            Button btnMesa = new Button("Mesa " + numMesa);
            btnMesa.setPrefSize(100, 60);
            btnMesa.setStyle("-fx-font-size: 16px; -fx-background-color: #AAD1AC;");
            btnMesa.setOnAction(e -> {
                mesaSeleccionada = numMesa;
                asignarMeseroAMesa();
                actualizarColoresMesas(); //Esto hace que se actualicen los colores.
                mostrarProductos();
                actualizarPedido();
            });
            mesaContainer.getChildren().add(btnMesa);
            botonesMesas.put(numMesa, btnMesa);
            pedidosPorMesa.put("Mesa " + numMesa, FXCollections.observableArrayList());
            mesasOcupadas.put(numMesa, false); //Para que al principio este libre la mesa.
        }
    }

    private void asignarMeseroAMesa() {
        // Solo pedir contraseña si no hay mesero asignado o si la mesa está ocupada
        if (meseroActual == null || mesasOcupadas.get(mesaSeleccionada)) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Asignar Mesero");
            dialog.setHeaderText("Ingrese la contraseña del mesero");
            dialog.setContentText("Contraseña:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String password = result.get();
                meseroActual = EmpleadoDAO.autenticarPorPassword(password);

                if (meseroActual != null) {
                    lblMeseroAsignado.setText("Mesero: " + meseroActual.getNombres() + " " + meseroActual.getApellidos());
                    guardarAsignacionMesa(mesaSeleccionada, meseroActual.getIdEmpleado());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Contraseña incorrecta o empleado no autorizado");
                    alert.showAndWait();
                }
            }
        }
    }

    private void guardarAsignacionMesa(int mesa, int idEmpleado) {
        String query = "INSERT INTO mesas_asignadas (mesa, id_empleado, fecha_hora) VALUES (?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE id_empleado = ?, fecha_hora = NOW()";

        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }

            PreparedStatement pstmt = Conexion.connection.prepareStatement(query);
            pstmt.setInt(1, mesa);
            pstmt.setInt(2, idEmpleado);
            pstmt.setInt(3, idEmpleado);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarColoresMesas() {
        for (Map.Entry<Integer, Button> entry : botonesMesas.entrySet()) {
            int numMesa = entry.getKey();
            Button btn = entry.getValue();

            if (mesaSeleccionada == numMesa) {
                btn.setStyle("-fx-font-size: 16px; -fx-background-color: #90CAF9;"); //Seleccionada.
            } else if (mesasOcupadas.getOrDefault(numMesa, false)) {
                btn.setStyle("-fx-font-size: 16px; -fx-background-color: #FFCC80;"); //Ocupada
            } else {
                btn.setStyle("-fx-font-size: 16px; -fx-background-color: #A5D6A7;"); //Desocupada.
            }
        }
    }

    private void mostrarProductos() {
        tilePaneProductos.getChildren().clear();
        String categoria = comboBoxCategorias.getValue();
        if (categoria == null) return;
        List<Producto> productos = categoriaProductos.get(categoria);

        for (Producto producto : productos) {
            VBox card = new VBox(5);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: gray; -fx-border-radius: 10; -fx-background-color: white;");

            ImageView imageView;
            try {
                imageView = new ImageView(new Image(getClass().getResourceAsStream(producto.getImagen())));
            } catch (Exception e) {
                imageView = new ImageView(); //Si no se encuentra, muestra vacío
            }
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true); //Este es para la calidad.

            Label nombre = new Label(producto.getNombre());
            Label precio = new Label("$" + producto.getPrecio());

            Button btnAgregar = new Button("Agregar");
            btnAgregar.setOnAction(e -> agregarProducto(producto));

            card.getChildren().addAll(imageView, nombre, precio, btnAgregar);
            tilePaneProductos.getChildren().add(card);
        }
    }

    private void agregarProducto(Producto producto) {
        if (meseroActual == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mesero no asignado");
            alert.setHeaderText(null);
            alert.setContentText("Debe asignar un mesero a la mesa antes de agregar productos");
            alert.showAndWait();
            return;
        }

        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);
        Producto existente = pedido.stream()
                .filter(p -> p.getNombre().equals(producto.getNombre()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            existente.incrementarCant();
        } else {
            Producto nuevo = new Producto(producto.getNombre(), producto.getPrecio(), producto.getCategoria(), producto.getImagen());
            pedido.add(nuevo);
        }
        actualizarPedido();
    }

    private void actualizarPedido() {
        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);
        listViewPedido.getItems().clear();

        double total = 0;
        for (Producto p : pedido) {
            listViewPedido.getItems().add(p.getNombre() + " x" + p.getCantidad() + " = $" + p.getTotal());
            total += p.getTotal();
        }
        lblTotal.setText("Total: $" + total);
    }

    private void guardarPedido() {
        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);
        if (pedido.isEmpty()) return;

        if (meseroActual == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mesero no asignado");
            alert.setHeaderText(null);
            alert.setContentText("Debe asignar un mesero a la mesa antes de guardar el pedido");
            alert.showAndWait();
            return;
        }

        String sql = "INSERT INTO pedidos (mesa, producto, cantidad, precio, total_ventas, fecha, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }
            PreparedStatement pstmt = Conexion.connection.prepareStatement(sql);

            for (Producto p : pedido) {
                pstmt.setString(1, mesa);
                pstmt.setString(2, p.getNombre());
                pstmt.setInt(3, p.getCantidad());
                pstmt.setDouble(4, p.getPrecio());
                pstmt.setDouble(5, p.getTotal());
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(7, meseroActual.getIdEmpleado());
                pstmt.executeUpdate();
            }
            pstmt.close();
            mesasOcupadas.put(mesaSeleccionada, true);
            actualizarColoresMesas();
            actualizarPedido();
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Pedido Guardado");
            alerta.setHeaderText(null);
            alerta.setContentText("El pedido ha sido guardado.");
            alerta.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarPedido() {
        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);
        int index = listViewPedido.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < pedido.size()){
            pedido.remove(index);
            actualizarPedido();
        } else {
            Alert aleta = new Alert(Alert.AlertType.WARNING, "Elige el producto a eliminar.");
            aleta.showAndWait();
        }
    }

    private void generarTicket(Stage stage) {
        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);

        // Validación de pedido vacío
        if (pedido == null || pedido.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No hay productos en el pedido.");
            alerta.showAndWait();
            return;
        }

        // Validación de mesero asignado
        if (meseroActual == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mesero no asignado");
            alert.setHeaderText(null);
            alert.setContentText("Debe asignar un mesero a la mesa antes de generar el ticket");
            alert.showAndWait();
            return;
        }

        // Manejo seguro del cliente seleccionado
        String clienteSeleccionado = comboBoxClientes.getValue() != null ?
                comboBoxClientes.getValue().trim() : "No especificado";

        // Configuración del diálogo para el nombre del archivo
        TextInputDialog dialogo = new TextInputDialog("ticket_mesa_" + mesaSeleccionada);
        dialogo.setTitle("Nombre del Ticket PDF");
        dialogo.setHeaderText("Ingrese el nombre del archivo PDF del ticket:");
        dialogo.setContentText("Nombre:");

        dialogo.showAndWait().ifPresent(nombreArchivo -> {
            try {
                String ruta = "C:/Users/100032624/Documents/Topicos Avanzados/TICKETS PDF/" + nombreArchivo + ".pdf";
                File archivo = new File(ruta);

                // Crear directorio si no existe
                archivo.getParentFile().mkdirs();

                double totalPedido = pedido.stream().mapToDouble(Producto::getTotal).sum();
                String meseroNombre = meseroActual.getNombres() + " " + meseroActual.getApellidos();

                // Generar el ticket
                TicketPDF.generarTicket(mesa, pedido, totalPedido, archivo, clienteSeleccionado, meseroNombre);

                // Mostrar confirmación
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Ticket Generado");
                alerta.setHeaderText(null);
                alerta.setContentText("¡El ticket PDF se generó correctamente en:\n" + ruta + "!");
                alerta.showAndWait();

                // Limpiar el pedido después de generar el ticket
                pedido.clear();
                mesasOcupadas.put(mesaSeleccionada, false);
                meseroActual = null;
                lblMeseroAsignado.setText("Mesero: No asignado");
                actualizarColoresMesas();
                actualizarPedido();

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error al generar ticket");
                error.setHeaderText(null);
                error.setContentText("Ocurrió un error al generar el ticket: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }
        });
    }

    private void inicializarProductos() {
        categoriaProductos.clear();
        categoriaProductos.put("Entradas", List.of(
                new Producto("Alitas", 249, "Entradas", "images/Entradas/Alitas.jpg"),
                new Producto("Boneless", 139, "Entradas", "/images/Entradas/Boneless.jpg"),
                new Producto("Ceviche", 293, "Entradas", "/images/Entradas/CevicheMariscos.jpg"),
                new Producto("Tártara de atún", 195, "Entradas", "/images/Entradas/TartaraAtun.jpg"),
                new Producto("Costillas BBQ", 279, "Entradas", "/images/Entradas/CostillasBqq.jpg"),
                new Producto("Cecina con guacamole", 253, "Entradas", "/images/Entradas/CecinaGuacamole.jpg"),
                new Producto("Aguachile", 243, "Entradas", "/images/Entradas/Aguachile.jpg"),
                new Producto("Papas", 60, "Entradas", "images/Entradas/PapasFrancesa.jpg"),
                new Producto("Tabla de quesos", 239, "Entradas", "/images/Entradas/TablaQuesos.jpg"),
                new Producto("Queso fundido", 250, "Entradas", "/images/Entradas/QuesoFundido.jpg")
        ));
        categoriaProductos.put("Tostadas", List.of(
                new Producto("Tostadas de tiritas de pescado", 66, "Tostadas", "/images/Tostadas/TiritasPescado.jpg"),
                new Producto("Tostada de atún en tiras", 89, "Tostadas", "/images/Tostadas/AtunTiras.jpg"),
                new Producto("Tostada de camarón", 71, "Tostadas", "/images/Tostadas/Camaron.jpg"),
                new Producto("Tostada de ceviche de pescado", 71, "Tostadas", "/images/Tostadas/CevichePescado.jpg"),
                new Producto("Tostada de ceviche de pulpo", 79, "Tostadas", "/images/Tostadas/CevichePulpo.jpg"),
                new Producto("Tostada de marlin guisado", 71, "Tostadas", "/images/Tostadas/MarlinGuisado.jpg")
        ));
        categoriaProductos.put("Coctelería", List.of(
                new Producto("Camarón", 235, "Cocteles", "/images/Costeles/Camaron.jpg"),
                new Producto("Pulpo", 235, "Cocteles", "/images/Costeles/Pulpo.jpg"),
                new Producto("Camarón y Pulpo", 235, "Cocteles", "/images/Costeles/CamaronPulpo.jpg")
        ));
        categoriaProductos.put("Tacos", List.of(
                new Producto("Tacos de camarón", 106, "Tacos", "/images/Tacos/Camaron.jpg"),
                new Producto("Tacos de pulpo con camarón", 109, "Tacos", "/images/Tacos/PulpoCamaron.jpg"),
                new Producto("Tacos de jicama", 129, "Tacos", "/images/Tacos/Jicama.jpg"),
                new Producto("Tacos mar y tierra", 106, "Tacos", "/images/Tacos/MarTierra.jpg")
        ));
        categoriaProductos.put("Pastas y Sopas", List.of(
                new Producto("Fettuccine alfredo", 237, "Pastas y Sopas", "/images/PastasSopas/Alfredo.jpg"),
                new Producto("Fettuccine al burro", 149, "Pastas y Sopas", "/images/PastasSopas/AlBurro.jpg"),
                new Producto("Fettuccine cherry con camarón", 239, "Pastas y Sopas", "/images/PastasSopas/CherryCamaron.jpg"),
                new Producto("Fettuccine tres quesos", 237, "Pastas y Sopas", "/images/PastasSopas/TresQuesos.jpg"),
                new Producto("Fettuccine oriental", 279, "Pastas y Sopas", "/images/PastasSopas/Oriental.jpg"),
                new Producto("Sopa de fideos", 69, "Pastas y Sopas", "/images/PastasSopas/Fideos.jpg"),
                new Producto("Sopa de lentejas", 79, "Pastas y Sopas", "/images/PastasSopas/Lentejas.jpg"),
                new Producto("Caldo de camarón", 159, "Pastas y Sopas", "/images/PastasSopas/Camaron.jpg"),
                new Producto("Clam chowder", 109, "Pastas y Sopas", "/images/PastasSopas/Chowder.jpg")
        ));
        categoriaProductos.put("Ensaladas", List.of(
                new Producto("Ensalada de frutos rojos", 200, "Ensaladas", "/images/Ensaladas/FrutosRojos.jpg"),
                new Producto("Ensalada de pulpo", 230, "Ensaladas", "/images/Ensaladas/Pulpo.jpg"),
                new Producto("Ensalada frutal con pollo", 245, "Ensaladas", "/images/Ensaladas/FrutalPollo.jpg"),
                new Producto("Ensalada mar y tierra", 300, "Ensaladas", "/images/Ensaladas/MarTierra.jpg")
        ));
        categoriaProductos.put("Hamburguesas", List.of(
                new Producto("Hamburguesa de sirlon", 260, "Hamburguesas", "/images/Hamburguesas/Sirlon.jpg"),
                new Producto("Hamburguesa de res", 260, "Hamburguesas", "/images/Hamburguesas/Res.jpg"),
                new Producto("Hamburguesa vegetariana", 240, "Hamburguesas", "/images/Hamburguesas/Vegetariana.jpg"),
                new Producto("Hamburguesa de camarón", 260, "Hamburguesas", "/images/Hamburguesas/Camaron.jpg")
        ));
        categoriaProductos.put("Menu Infantil", List.of(
                new Producto("Pechuga de pollo", 90, "Menu Infantil", "/images/MenuInfantil/PechugaPollo.jpg"),
                new Producto("Sabanita de res", 90, "Menu Infantil", "/images/MenuInfantil/SabanitaRes.jpg"),
                new Producto("Pizza individual", 195, "Menu Infantil", "/images/MenuInfantil/PizzaIndividual.jpg"),
                new Producto("Nuggets de pollo", 70, "Menu Infantil", "/images/MenuInfantil/NuggetsPollo.jpg")
        ));
        categoriaProductos.put("Pizzas", List.of(
                new Producto("Pepperoni", 255, "Pizzas", "/images/Pizzas/Pepperoni.jpg"),
                new Producto("Carnes frías", 275, "Pizzas", "/images/Pizzas/CarnesFrias.jpg"),
                new Producto("3 quesos", 255, "Pizzas", "/images/Pizzas/TresQuesos.jpg"),
                new Producto("Margarita", 255, "Pizzas", "/images/Pizzas/Margarita.jpg"),
                new Producto("Vegetariana", 255, "Pizzas", "/images/Pizzas/Vegetariana.jpg"),
                new Producto("Mexicana", 275, "Pizzas", "/images/Pizzas/Mexicana.jpg"),
                new Producto("Pastor", 290, "Pizzas", "/images/Pizzas/Pastor.jpg"),
                new Producto("Hawaiana", 275, "Pizzas", "/images/Pizzas/Hawaiana.jpg"),
                new Producto("Arrachera", 290, "Pizzas", "/images/Pizzas/Arrachera.jpg")
        ));
        categoriaProductos.put("Carnes y Pescados", List.of(
                new Producto("Trucha almendrada", 309, "Carnes y Pescados", "/images/CarnesPescadosMariscos/TruchaAlmendrada.jpg"),
                new Producto("Salmón a las brasas", 349, "Carnes y Pescados", "/images/CarnesPescadosMariscos/SalmonBrasas.jpg"),
                new Producto("Salmón teriyaki", 369, "Carnes y Pescados", "/images/CarnesPescadosMariscos/SalmonTeriyaki.jpg"),
                new Producto("Salmón adobado", 350, "Carnes y Pescados", "/images/CarnesPescadosMariscos/SalmonAdobado.jpg"),
                new Producto("Arrachera", 349, "Carnes y Pescados", "/images/CarnesPescadosMariscos/Arrachera.jpg"),
                new Producto("Rib eye", 489, "Carnes y Pescados", "/images/CarnesPescadosMariscos/RibEye.jpg"),
                new Producto("Cowboy", 479, "Carnes y Pescados", "/images/CarnesPescadosMariscos/Cowboy.jpg"),
                new Producto("Churrasco martin fierro", 340, "Carnes y Pescados", "/images/CarnesPescadosMariscos/ChurrascoMartin.jpg"),
                new Producto("Churrasco al grill", 340, "Carnes y Pescados", "/images/CarnesPescadosMariscos/ChurrascoGrill.jpg"),
                new Producto("Pechuga de pollo", 275, "Carnes y Pescados", "/images/CarnesPescados/PechugaPollo.jpg")
        ));
        categoriaProductos.put("Cafe y Postres", List.of(
                new Producto("Americano", 38, "Cafe y Postres", "/images/CafePostres/Americano.jpg"),
                new Producto("Expresso", 28, "Cafe y Postres", "/images/CafePostres/Expresso.jpg"),
                new Producto("Cappuccino", 50, "Cafe y Postres", "/images/CafePostres/Cappuccino.jpg"),
                new Producto("Cappuccino frappé", 70, "Cafe y Postres", "/images/CafePostres/CappuccinoFrappe.png"),
                new Producto("Café irlandés", 150, "Cafe y Postres", "/images/CafePostres/Irlandes.jpg"),
                new Producto("Carajillo", 135, "Cafe y Postres", "/images/CafePostres/Carajillo.jpg"),
                new Producto("Crepas con cajeta", 120, "Cafe y Postres", "/images/CafePostres/CrepasCajeta.jpg"),
                new Producto("Helado", 110, "Cafe y Postres", "/images/CafePostres/Helado.jpg"),
                new Producto("Conejito turin", 120, "Cafe y Postres", "/images/CafePostres/ConejitoTurin.jpg"),
                new Producto("Red velvet", 120, "Cafe y Postres", "/images/CafePostres/RedVelvet.jpg"),
                new Producto("Tartaleta de plátano", 120, "Cafe y Postres", "/images/CafePostres/TartaletaPlatano.jpg"),
                new Producto("Tartaleta de uvas", 120, "Cafe y Postres", "/images/CafePostres/TartaletaUvas.jpg")
        ));
        categoriaProductos.put("Cocteleria", List.of(
                new Producto("Martini", 129, "Cocteleria", "/images/Cocteleria/Martini.png"),
                new Producto("Ginebra", 129, "Cocteleria", "/images/Cocteleria/Ginebra.jpg"),
                new Producto("Mojito", 129, "Cocteleria", "/images/Cocteleria/Mojito.jpg"),
                new Producto("Daiquiri", 129, "Cocteleria", "/images/Cocteleria/Daiquiri.jpg"),
                new Producto("Perla negra", 129, "Cocteleria", "/images/Cocteleria/PerlaNegra.jpg"),
                new Producto("Margarita", 129, "Cocteleria", "/images/Cocteleria/Margarita.jpg"),
                new Producto("Clericot", 129, "Cocteleria", "/images/Cocteleria/Clericot.jpg"),
                new Producto("Piña colada", 149, "Cocteleria", "/images/Cocteleria/Colada.jpg")
        ));
        categoriaProductos.put("Cervezas", List.of(
                new Producto("Corona", 35, "Cervezas", "/images/Cervezas/Corona.jpg"),
                new Producto("Victoria", 35, "Cervezas", "/images/Cervezas/Victoria.jpg"),
                new Producto("Modelo especial", 35, "Cervezas", "/images/Cervezas/ModeloEspecial.jpg"),
                new Producto("Negra modelo", 35, "Cervezas", "/images/Cervezas/NegraModelo.jpg"),
                new Producto("Pacifico", 35, "Cervezas", "/images/Cervezas/Pacifico.png"),
                new Producto("Corona light", 35, "Cervezas", "/images/Cervezas/CoronaLight.jpg"),
                new Producto("Stella Artois", 60, "Cervezas", "/images/Cervezas/StellaArtois.png"),
                new Producto("Michelob ultra", 60, "Cervezas", "/images/Cervezas/MichelobUltra.jpg"),
                new Producto("Corona cero", 40, "Cervezas", "/images/Cervezas/CoronaCero.jpg")
        ));
        categoriaProductos.put("Bebidas Sin Alcohol", List.of(
                new Producto("Refresco", 37, "Bebidas", "images/Bebidas/Refrescos.jpg"),
                new Producto("Limonada", 40, "Bebidas", "images/Bebidas/Limonada.png"),
                new Producto("Naranjada", 40, "Bebidas", "/images/Bebidas/Naranjada.jpg"),
                new Producto("Limonada", 40, "Bebidas", "/images/Bebidas/Limonada.png"),
                new Producto("Agua fresca", 35, "Bebidas", "/images/Bebidas/AguaFresca.jpg")
        ));
        categoriaProductos.put("Licores", List.of(
                new Producto("Baileys", 180, "Licores", "/images/Licores/Baileys.jpg"),
                new Producto("Jagermeister", 210, "Licores", "/images/Licores/Jagermeister.jpg"),
                new Producto("Licor 43", 210, "Licores", "/images/Licores/Licor43.jpg"),
                new Producto("Amaretto Disaronno", 215, "Licores", "/images/Licores/AmarettoDisaronno.jpg"),
                new Producto("Sambuca negro", 215, "Licores", "/images/Licores/SambucaNegro.jpg"),
                new Producto("Fernet", 180, "Licores", "/images/Licores/Fernet.jpg")
        ));
        categoriaProductos.put("Ron", List.of(
                new Producto("Havana 7 años", 195, "Ron", "/images/Ron/Havana7.jpg"),
                new Producto("Bacardí añejo", 350, "Ron", "/images/Ron/BacardiAnejo.jpg"),
                new Producto("Bacardí blanco", 350, "Ron", "/images/Ron/BacardiBlanco.jpg"),
                new Producto("Flor de caña 5 años", 170, "Ron", "/images/Ron/Flor5.jpg"),
                new Producto("Flor de caña 7 años", 185, "Ron", "/images/Ron/Flor7.jpg"),
                new Producto("Captain morgan", 120, "Ron", "/images/Ron/CaptainMorgan.jpg")
        ));
        categoriaProductos.put("Tequila", List.of(
                new Producto("Don julio 70", 275, "Tequila", "/images/Tequila/Julio70.png"),
                new Producto("Don julio reposado", 235, "Tequila", "/images/Tequila/JulioReposado.jpg"),
                new Producto("1800 cristalino", 260, "Tequila", "/images/Tequila/1800Cristalino.png"),
                new Producto("Centenario reposado", 150, "Tequila", "/images/Tequila/CentenarioReposado.jpg"),
                new Producto("Centenario plata", 130, "Tequila", "/images/Tequila/CentenarioPlata.jpg"),
                new Producto("Tradicional reposado", 140, "Tequila", "/images/Tequila/TradicionalReposado.png"),
                new Producto("Tradicional plata", 140, "Tequila", "/images/Tequila/TradiconalPlata.jpg"),
                new Producto("Herradura ultra", 260, "Tequila", "/images/Tequila/HerraduraUltra.jpg"),
                new Producto("Herradura añejo", 260, "Tequila", "/images/Tequila/HerraduraAnejo.jpg"),
                new Producto("Maestro dobel diamante", 260, "Tequila", "/images/Tequila/MaestroDobel.jpg")
        ));
        categoriaProductos.put("Whisky", List.of(
                new Producto("JW black label", 260, "Whisky", "/images/Whisky/BlackLabel.jpg"),
                new Producto("JW red label", 170, "Whisky", "/images/Whisky/RedLabel.jpg"),
                new Producto("Buchanan's 12 años", 250, "Whisky", "/images/Whisky/Buchanans12.jpg"),
                new Producto("Old parr 12 años", 250, "Whisky", "/images/Whisky/OldParr.jpg"),
                new Producto("Jack Daniel's", 190, "Whisky", "/images/Whisky/JackDaniels.jpg"),
                new Producto("Jack Daniel's honey", 190, "Whisky", "/images/Whisky/DanielsHoney.png")
        ));
        categoriaProductos.put("Brandy", List.of(
                new Producto("Magno", 170, "Brandy", "/images/Brandy/Magno.jpg"),
                new Producto("Torres 10 años", 175, "Brandy", "/images/Brandy/Torres10.jpg"),
                new Producto("Torres 20 años", 325, "Brandy", "/images/Brandy/Torres20.jpg"),
                new Producto("Terry centenario", 165, "Brandy", "/images/Brandy/TerryCentenario.jpg")
        ));
        cargarCategoriasYProductosDesdeBD();
    }

    private void cargarCategoriasYProductosDesdeBD() {
        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }

            //Primero cargar categorías
            Statement stmtCategorias = Conexion.connection.createStatement();
            ResultSet rsCategorias = stmtCategorias.executeQuery("SELECT nombre FROM categorias ORDER BY nombre");

            while (rsCategorias.next()) {
                String categoria = rsCategorias.getString("nombre");
                categoriaProductos.put(categoria, new ArrayList<>());
            }
            rsCategorias.close();
            stmtCategorias.close();

            //Luego cargar productos por categoría
            Statement stmtProductos = Conexion.connection.createStatement();
            ResultSet rsProductos = stmtProductos.executeQuery("SELECT nombre, precio, categoria, imagen FROM productos");

            while (rsProductos.next()) {
                String nombre = rsProductos.getString("nombre");
                double precio = rsProductos.getDouble("precio");
                String categoria = rsProductos.getString("categoria");
                String imagen = rsProductos.getString("imagen");

                Producto producto = new Producto(nombre, precio, categoria, imagen);
                categoriaProductos.get(categoria).add(producto);
            }
            rsProductos.close();
            stmtProductos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}