package com.example.tap2025.componentes;

import com.example.tap2025.modelos.ClientesDAO;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public abstract class ButtonCell extends TableCell<ClientesDAO, String> {
    private Button btnCelda;
    private String strLabelBtn;

    public ButtonCell(String label) {
        strLabelBtn = label;
        btnCelda = new Button(strLabelBtn);
        btnCelda.setOnAction(event -> {
            if (strLabelBtn.equals("Editar")) {
                handleEdit();
            } else {
                handleDelete();
            }
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty)
            this.setGraphic(btnCelda);
        else
            this.setGraphic(null); // Evitar que aparezcan botones en celdas vacías
    }

    public abstract void handleEdit();

    public abstract void handleDelete();
}

