package rentcar.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import rentcar.service.RentCarService;

public class StatisticsController {

    @FXML private Label totalClients;
    @FXML private Label totalCars;
    @FXML private Label totalRentals;
    @FXML private Label activeRentals;
    @FXML private Label availableCars;
    @FXML private Label avgRentalPrice;
    @FXML private Label totalIncome;

    @FXML
    public void initialize() {
        var s = new RentCarService().getStatistics();
        totalClients.setText("Всего клиентов: " + s.get("totalClients"));
        totalCars.setText("Всего автомобилей: " + s.get("totalCars"));
        totalRentals.setText("Всего аренд: " + s.get("totalRentals"));
        activeRentals.setText("Активных аренд: " + s.get("activeRentals"));
        availableCars.setText("Доступных автомобилей: " + s.get("availableCars"));
        avgRentalPrice.setText(String.format("Средняя стоимость аренды: %.2f",
                s.get("avgPrice").doubleValue()));
        totalIncome.setText(String.format("Доход по завершённым: %.2f",
                s.get("totalIncome").doubleValue()));
    }
}