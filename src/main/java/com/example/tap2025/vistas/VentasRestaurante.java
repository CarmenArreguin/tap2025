package com.example.tap2025.vistas;

import com.example.tap2025.modelos.Producto;
import com.example.tap2025.modelos.conexion;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class VentasRestaurante {
    private final Map<String, ObservableList<Producto>> pedidosPorMesa = new HashMap<>();
    private final ListView<String> listViewPedido = new ListView<>();
    private final Label lblTotal = new Label("Total: $0.0");
    private final FlowPane mesaContainer = new FlowPane();
    private final ComboBox<String> comboBoxCategorias = new ComboBox<>();
    private final TilePane tilePaneProductos = new TilePane();
    private final Map<String, List<Producto>> categoriaProductos = new HashMap<>();
    private int mesaSeleccionada = 1;
    private final Map<Integer, Button> botonesMesas = new HashMap<>();
    private final Map<Integer, Boolean> mesasOcupadas = new HashMap<>();

    public void mostrar(Stage stage) {
        inicializarProductos();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        mostrarMesas();

        //Esto es para tener las categorías de forma tipo táctil, solamente que usamos en este caso el mouse.
        comboBoxCategorias.getItems().addAll(categoriaProductos.keySet());
        comboBoxCategorias.setOnAction(e -> mostrarProductos());
        comboBoxCategorias.setStyle("-fx-font-size: 18px;");

        tilePaneProductos.setHgap(10);
        tilePaneProductos.setVgap(10);
        tilePaneProductos.setPrefColumns(4);

        Button btnGenerarTicket = new Button("Generar Ticket PDF");
        btnGenerarTicket.setStyle("-fx-font-size: 14px; -fx-background-color: #EBC093;");

        btnGenerarTicket.setOnAction(event -> generarTicket(stage));

        VBox vboxPedido = new VBox(10);
        vboxPedido.setPadding(new Insets(10));
        vboxPedido.getChildren().addAll(new Label("Pedido actual."), listViewPedido, lblTotal, btnGenerarTicket);

        Button btnGuardar = new Button("Guardar Pedido");
        btnGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #83CBFF;");
        btnGuardar.setOnAction(e -> guardarPedido());

        Button btnLimpiar = new Button("Limpiar Pedido.");
        btnLimpiar.setStyle("-fx-font-size: 14px; -fx-background-color: #EF9A9A;");
        btnLimpiar.setOnAction(e -> limpiarPedido());

        vboxPedido.getChildren().addAll(btnGuardar, btnLimpiar);

        ScrollPane scrollPaneProductos = new ScrollPane(tilePaneProductos);
        scrollPaneProductos.setFitToWidth(true);
        scrollPaneProductos.setPrefHeight(600);
        scrollPaneProductos.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        HBox seccionCentral = new HBox(15, scrollPaneProductos, vboxPedido);

        root.getChildren().addAll(new Label("Seleccionar mesa:"), mesaContainer, new Label("Seleccionar categoría:"), comboBoxCategorias, seccionCentral);

        Scene escena = new Scene(root, 1000, 700);
        stage.setScene(escena);
        stage.setTitle("Ventas Restaurante :)");
        stage.show();
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
        String mesa = "Mesa " + mesaSeleccionada;
        ObservableList<Producto> pedido = pedidosPorMesa.get(mesa);
        Producto existente = pedido.stream().filter(p -> p.getNombre().equals(producto.getNombre())).findFirst().orElse(null);
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

        String sql = "INSERT INTO pedidos (mesa, producto, cantidad, precio, total, fecha) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);

            for (Producto p : pedido) {
                pstmt.setString(1, mesa);
                pstmt.setString(2, p.getNombre());
                pstmt.setInt(3, p.getCantidad());
                pstmt.setDouble(4, p.getPrecio());
                pstmt.setDouble(5, p.getTotal());
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
            }
            pstmt.close();
            mesasOcupadas.put(mesaSeleccionada, true); //Se marca como ocupada.
            actualizarColoresMesas();
            actualizarPedido();
            System.out.println("Pedido guardado en la base de datos.");
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

        if (pedido == null || pedido.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No hay productos en el pedido.");
            alerta.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Ticket PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File archivo = fileChooser.showSaveDialog(stage);

        if (archivo != null) {
            double totalPedido = pedido.stream().mapToDouble(Producto::getTotal).sum();
            TicketPDF.generarTicket("Mesa " + mesaSeleccionada, pedido, totalPedido, archivo);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION, "Ticket generado exitosamente.");
            alerta.showAndWait();

            pedido.clear();
            mesasOcupadas.put(mesaSeleccionada, false);
            actualizarColoresMesas();
            actualizarPedido();
        }
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
        categoriaProductos.put("Cocteles", List.of(
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
        categoriaProductos.put("Bebidas", List.of(
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
        File archivo = new File("productos.csv");
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (datos.length == 5) {
                    String nombre = datos[0];
                    double precio = Double.parseDouble(datos[1]);
                    int cantidad = Integer.parseInt(datos[2]); //Esta variable por ahora no la usamos.
                    String categoria = datos[3];
                    String imagen = datos[4];

                    Producto productoNuevo = new Producto(nombre, precio, categoria, imagen);
                    categoriaProductos.computeIfAbsent(categoria, k -> new ArrayList<>()).add(productoNuevo);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer productos: " + e.getMessage());
        }
    }
}