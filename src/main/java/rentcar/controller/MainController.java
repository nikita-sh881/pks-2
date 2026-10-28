package rentcar.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.*;
import rentcar.service.RentCarService;
import rentcar.util.ExcelExporter;

import java.io.File;
import java.util.Comparator;

public class MainController {

    // Автомобили
    @FXML private TextField carSearchField;
    @FXML private ComboBox<CarStatus> carFilterStatus;
    @FXML private ComboBox<String> carSortField;
    @FXML private TableView<Car> carTable;
    @FXML private TableColumn<Car, Integer> colCarId;
    @FXML private TableColumn<Car, String> colCarBrand;
    @FXML private TableColumn<Car, String> colCarModel;
    @FXML private TableColumn<Car, String> colCarPlate;
    @FXML private TableColumn<Car, Integer> colCarYear;
    @FXML private TableColumn<Car, Double> colCarPrice;
    @FXML private TableColumn<Car, String> colCarStatus;

    // Клиенты
    @FXML private TextField clientSearchField;
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Integer> colClientId;
    @FXML private TableColumn<Client, String> colClientName;
    @FXML private TableColumn<Client, String> colClientPassport;
    @FXML private TableColumn<Client, String> colClientPhone;

    // Аренды
    @FXML private ComboBox<RentalStatus> rentalFilterStatus;
    @FXML private TableView<Rental> rentalTable;
    @FXML private TableColumn<Rental, Integer> colRentalId;
    @FXML private TableColumn<Rental, Integer> colRentalClient;
    @FXML private TableColumn<Rental, Integer> colRentalCar;
    @FXML private TableColumn<Rental, String> colRentalStart;
    @FXML private TableColumn<Rental, String> colRentalEnd;
    @FXML private TableColumn<Rental, Double> colRentalTotal;
    @FXML private TableColumn<Rental, String> colRentalStatus;

    private final RentCarService service = new RentCarService();
    private final ObservableList<Car> cars = FXCollections.observableArrayList();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<Rental> rentals = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initCarTable();
        initClientTable();
        initRentalTable();

        carFilterStatus.setItems(FXCollections.observableArrayList(CarStatus.values()));
        carSortField.setItems(FXCollections.observableArrayList("Цена ↑", "Цена ↓", "Год"));
        rentalFilterStatus.setItems(FXCollections.observableArrayList(RentalStatus.values()));

        refreshAll();
    }

    private void initCarTable() {
        colCarId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colCarBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));
        colCarModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colCarPlate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLicensePlate()));
        colCarYear.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getYear()));
        colCarPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPricePerDay()));
        colCarStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        carTable.setItems(cars);
    }

    private void initClientTable() {
        colClientId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colClientName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colClientPassport.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPassport()));
        colClientPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        clientTable.setItems(clients);
    }

    private void initRentalTable() {
        colRentalId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colRentalClient.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getClientId()));
        colRentalCar.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCarId()));
        colRentalStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDate().toString()));
        colRentalEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndDate().toString()));
        colRentalTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalPrice()));
        colRentalStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        rentalTable.setItems(rentals);
    }

    @FXML
    private void refreshAll() {
        cars.setAll(service.getAllCars());
        clients.setAll(service.getAllClients());
        rentals.setAll(service.getAllRentals());
    }

    // ===== Автомобили =====
    @FXML private void applyCarSearch() {
        try {
            var list = service.getAllCars();
            String q = carSearchField.getText();
            if (q != null && !q.isBlank()) list = service.searchCarsByBrandOrModel(q);
            if (carFilterStatus.getValue() != null) {
                var s = carFilterStatus.getValue();
                list = list.stream().filter(c -> c.getStatus() == s).toList();
            }
            String sort = carSortField.getValue();
            if ("Цена ↑".equals(sort))
                list = list.stream().sorted(Comparator.comparingDouble(Car::getPricePerDay)).toList();
            else if ("Цена ↓".equals(sort))
                list = list.stream().sorted(Comparator.comparingDouble(Car::getPricePerDay).reversed()).toList();
            else if ("Год".equals(sort))
                list = list.stream().sorted(Comparator.comparingInt(Car::getYear)).toList();
            cars.setAll(list);
        } catch (BusinessException e) { showError(e.getMessage()); }
    }

    @FXML private void addCar() { openCarForm(null); }
    @FXML private void editCar() {
        Car s = carTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        openCarForm(s);
    }
    @FXML private void deleteCar() {
        Car s = carTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        if (confirm("Удалить " + s.getBrand() + " " + s.getModel() + "?")) {
            try { service.deleteCar(s.getId()); refreshAll(); }
            catch (BusinessException | EntityNotFoundException e) { showError(e.getMessage()); }
        }
    }
    private void openCarForm(Car car) {
        openForm("/rentcar/fxml/car-form.fxml", c -> {
            CarFormController ctl = (CarFormController) c;
            ctl.setCar(car);
            ctl.setOnSaved(this::refreshAll);
        }, car == null ? "Новый автомобиль" : "Редактирование");
    }

    // ===== Клиенты =====
    @FXML private void applyClientSearch() {
        String q = clientSearchField.getText();
        if (q != null && !q.isBlank()) clients.setAll(service.searchClientsByName(q));
        else clients.setAll(service.getAllClients());
    }
    @FXML private void addClient() { openClientForm(null); }
    @FXML private void editClient() {
        Client s = clientTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        openClientForm(s);
    }
    @FXML private void deleteClient() {
        Client s = clientTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        if (confirm("Удалить клиента " + s.getFullName() + "?")) {
            try { service.deleteClient(s.getId()); refreshAll(); }
            catch (BusinessException | EntityNotFoundException e) { showError(e.getMessage()); }
        }
    }
    private void openClientForm(Client client) {
        openForm("/rentcar/fxml/client-form.fxml", c -> {
            ClientFormController ctl = (ClientFormController) c;
            ctl.setClient(client);
            ctl.setOnSaved(this::refreshAll);
        }, client == null ? "Новый клиент" : "Редактирование");
    }

    // ===== Аренды =====
    @FXML private void applyRentalFilter() {
        if (rentalFilterStatus.getValue() != null) {
            rentals.setAll(service.filterRentalsByStatus(rentalFilterStatus.getValue()));
        } else {
            rentals.setAll(service.getAllRentals());
        }
    }
    @FXML private void addRental() { openRentalForm(null); }
    @FXML private void editRental() {
        Rental s = rentalTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        openRentalForm(s);
    }
    @FXML private void deleteRental() {
        Rental s = rentalTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        if (confirm("Удалить аренду #" + s.getId() + "?")) {
            try { service.deleteRental(s.getId()); refreshAll(); }
            catch (BusinessException | EntityNotFoundException e) { showError(e.getMessage()); }
        }
    }
    @FXML private void completeRental() {
        Rental s = rentalTable.getSelectionModel().getSelectedItem();
        if (s == null) { showError("Выберите строку."); return; }
        try { service.completeRental(s.getId()); refreshAll(); }
        catch (BusinessException e) { showError(e.getMessage()); }
    }
    private void openRentalForm(Rental rental) {
        openForm("/rentcar/fxml/rental-form.fxml", c -> {
            RentalFormController ctl = (RentalFormController) c;
            ctl.setRental(rental);
            ctl.setOnSaved(this::refreshAll);
        }, rental == null ? "Новая аренда" : "Редактирование");
    }

    // ===== Общие =====
    private void openForm(String path, java.util.function.Consumer<Object> init, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            init.accept(loader.getController());
            stage.setTitle(title);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Не удалось открыть форму: " + e.getMessage());
        }
    }

    @FXML private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rentcar/fxml/statistics.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Статистика");
            stage.showAndWait();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void exportCars() {
        exportToExcel("cars.xlsx", "Автомобили", (path) -> ExcelExporter.exportCars(cars, path));
    }
    @FXML private void exportClients() {
        exportToExcel("clients.xlsx", "Клиенты", (path) -> ExcelExporter.exportClients(clients, path));
    }
    @FXML private void exportRentals() {
        exportToExcel("rentals.xlsx", "Аренды", (path) -> ExcelExporter.exportRentals(rentals, path));
    }

    private void exportToExcel(String name, String title, java.util.function.Consumer<String> exporter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить " + title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName(name);
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try { exporter.accept(file.getAbsolutePath()); showInfo("Сохранено: " + file.getAbsolutePath()); }
            catch (BusinessException e) { showError(e.getMessage()); }
        }
    }

    private boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }
    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }
}