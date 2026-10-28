package rentcar.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import rentcar.exception.BusinessException;
import rentcar.model.Car;
import rentcar.model.CarStatus;
import rentcar.service.RentCarService;

public class CarFormController {

    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField plateField;
    @FXML private TextField yearField;
    @FXML private TextField priceField;
    @FXML private ComboBox<CarStatus> statusCombo;

    private final RentCarService service = new RentCarService();
    private Car current;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(CarStatus.values()));
    }

    public void setCar(Car car) {
        this.current = car;
        if (car != null) {
            brandField.setText(car.getBrand());
            modelField.setText(car.getModel());
            plateField.setText(car.getLicensePlate());
            yearField.setText(String.valueOf(car.getYear()));
            priceField.setText(String.valueOf(car.getPricePerDay()));
            statusCombo.setValue(car.getStatus());
        } else {
            statusCombo.setValue(CarStatus.AVAILABLE);
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void save() {
        try {
            String brand = brandField.getText();
            String model = modelField.getText();
            String plate = plateField.getText();
            int year = Integer.parseInt(yearField.getText());
            double price = Double.parseDouble(priceField.getText().replace(',', '.'));
            CarStatus status = statusCombo.getValue();

            if (current == null) {
                service.addCar(brand, model, plate, year, price, status);
            } else {
                service.updateCar(new Car(current.getId(), brand, model,
                        plate, year, price, status));
            }
            if (onSaved != null) onSaved.run();
            ((Stage) brandField.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            showError("Год и цена должны быть числами.");
        } catch (BusinessException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}