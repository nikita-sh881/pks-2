package rentcar.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import rentcar.exception.BusinessException;
import rentcar.model.*;
import rentcar.service.RentCarService;

import java.time.LocalDate;

public class RentalFormController {

    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Car> carCombo;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;
    @FXML private ComboBox<RentalStatus> statusCombo;

    private final RentCarService service = new RentCarService();
    private Rental current;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        clientCombo.setItems(FXCollections.observableArrayList(service.getAllClients()));
        carCombo.setItems(FXCollections.observableArrayList(service.getAllCars()));
        statusCombo.setItems(FXCollections.observableArrayList(RentalStatus.values()));
        statusCombo.setValue(RentalStatus.CREATED);
    }

    public void setRental(Rental r) {
        this.current = r;
        if (r != null) {
            clientCombo.getItems().stream()
                    .filter(c -> c.getId() == r.getClientId())
                    .findFirst().ifPresent(clientCombo::setValue);
            carCombo.getItems().stream()
                    .filter(c -> c.getId() == r.getCarId())
                    .findFirst().ifPresent(carCombo::setValue);
            startPicker.setValue(r.getStartDate());
            endPicker.setValue(r.getEndDate());
            statusCombo.setValue(r.getStatus());
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void save() {
        try {
            Client client = clientCombo.getValue();
            Car car = carCombo.getValue();
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            if (client == null || car == null || start == null || end == null)
                throw new BusinessException("Заполните все поля.");

            if (current == null) {
                service.createRental(client.getId(), car.getId(), start, end);
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (days <= 0) days = 1;
                double total = days * car.getPricePerDay();
                Rental updated = new Rental(current.getId(), client.getId(), car.getId(),
                        start, end, total, statusCombo.getValue());
                service.updateRental(updated);
            }
            if (onSaved != null) onSaved.run();
            ((Stage) startPicker.getScene().getWindow()).close();
        } catch (BusinessException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}