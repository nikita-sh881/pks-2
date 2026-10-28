package rentcar.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import rentcar.exception.BusinessException;
import rentcar.model.Client;
import rentcar.service.RentCarService;

public class ClientFormController {

    @FXML private TextField nameField;
    @FXML private TextField passportField;
    @FXML private TextField phoneField;

    private final RentCarService service = new RentCarService();
    private Client current;
    private Runnable onSaved;

    public void setClient(Client client) {
        this.current = client;
        if (client != null) {
            nameField.setText(client.getFullName());
            passportField.setText(client.getPassport());
            phoneField.setText(client.getPhone());
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void save() {
        try {
            String name = nameField.getText();
            String passport = passportField.getText();
            String phone = phoneField.getText();
            if (current == null) {
                service.addClient(name, passport, phone);
            } else {
                service.updateClient(new Client(current.getId(), name, passport, phone));
            }
            if (onSaved != null) onSaved.run();
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (BusinessException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}