package rentcar.service;

import rentcar.dao.CarDao;
import rentcar.dao.ClientDao;
import rentcar.dao.RentalDao;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentCarService {

    private final ClientDao clientDao = new ClientDao();
    private final CarDao carDao = new CarDao();
    private final RentalDao rentalDao = new RentalDao();

    // ========== КЛИЕНТЫ ==========

    public List<Client> getAllClients() {
        return clientDao.findAll();
    }

    public Client findClientById(int id) {
        return clientDao.findById(id);
    }

    public void addClient(String fullName, String passport, String phone) {
        if (fullName == null || fullName.isBlank())
            throw new BusinessException("ФИО клиента обязательно.");
        if (passport == null || passport.isBlank())
            throw new BusinessException("Паспорт обязателен.");
        if (phone == null || phone.isBlank())
            throw new BusinessException("Телефон обязателен.");
        clientDao.insert(new Client(0, fullName, passport, phone));
    }

    public void updateClient(Client client) {
        if (client.getFullName() == null || client.getFullName().isBlank())
            throw new BusinessException("ФИО клиента обязательно.");
        clientDao.update(client);
    }

    public void deleteClient(int id) {
        clientDao.findById(id);
        List<Rental> active = rentalDao.findAll().stream()
                .filter(r -> r.getClientId() == id && r.getStatus() == RentalStatus.ACTIVE)
                .toList();
        if (!active.isEmpty())
            throw new BusinessException("Нельзя удалить клиента с активной арендой.");
        clientDao.delete(id);
    }

    // ========== АВТОМОБИЛИ ==========

    public List<Car> getAllCars() {
        return carDao.findAll();
    }

    public Car findCarById(int id) {
        return carDao.findById(id);
    }

    public void addCar(String brand, String model, String plate, int year,
                       double price, CarStatus status) {
        validateCar(brand, model, plate, year, price);
        carDao.insert(new Car(0, brand, model, plate, year, price, status));
    }

    public void updateCar(Car car) {
        validateCar(car.getBrand(), car.getModel(), car.getLicensePlate(),
                car.getYear(), car.getPricePerDay());
        Car old = carDao.findById(car.getId());
        if (old.getStatus() == CarStatus.RENTED && car.getStatus() == CarStatus.MAINTENANCE)
            throw new BusinessException("Нельзя перевести автомобиль в ТО во время аренды.");
        carDao.update(car);
    }

    public void deleteCar(int id) {
        carDao.findById(id);
        List<Rental> active = rentalDao.findAll().stream()
                .filter(r -> r.getCarId() == id && r.getStatus() == RentalStatus.ACTIVE)
                .toList();
        if (!active.isEmpty())
            throw new BusinessException("Нельзя удалить автомобиль с активной арендой.");
        carDao.delete(id);
    }

    private void validateCar(String brand, String model, String plate, int year, double price) {
        if (brand == null || brand.isBlank())
            throw new BusinessException("Марка обязательна.");
        if (model == null || model.isBlank())
            throw new BusinessException("Модель обязательна.");
        if (plate == null || plate.isBlank())
            throw new BusinessException("Госномер обязателен.");
        if (year < 1950)
            throw new BusinessException("Год должен быть не меньше 1950.");
        if (price <= 0)
            throw new BusinessException("Цена должна быть положительной.");
    }

    // ========== АРЕНДЫ ==========

    public List<Rental> getAllRentals() {
        return rentalDao.findAll();
    }

    public Rental findRentalById(int id) {
        return rentalDao.findById(id);
    }

    public void createRental(int clientId, int carId, LocalDate start, LocalDate end) {
        if (end.isBefore(start))
            throw new BusinessException("Дата окончания раньше даты начала.");

        Client client = clientDao.findById(clientId);
        Car car = carDao.findById(carId);

        if (car.getStatus() != CarStatus.AVAILABLE)
            throw new BusinessException("Автомобиль недоступен (статус " + car.getStatus() + ").");

        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1;
        double total = days * car.getPricePerDay();

        rentalDao.create(client.getId(), car.getId(), start, end, total);
    }

    public void updateRental(Rental rental) {
        if (rental.getEndDate().isBefore(rental.getStartDate()))
            throw new BusinessException("Дата окончания раньше даты начала.");
        rentalDao.update(rental);
    }

    public void completeRental(int rentalId) {
        Rental r = rentalDao.findById(rentalId);
        if (r.getStatus() != RentalStatus.ACTIVE)
            throw new BusinessException("Завершить можно только активную аренду.");
        rentalDao.complete(rentalId, r.getCarId());
    }

    public void deleteRental(int id) {
        rentalDao.findById(id);
        rentalDao.delete(id);
    }

    // ========== ПОИСК / ФИЛЬТР / СОРТИРОВКА ==========

    public List<Car> searchCarsByBrandOrModel(String query) {
        String q = query.toLowerCase();
        return carDao.findAll().stream()
                .filter(c -> c.getBrand().toLowerCase().contains(q)
                        || c.getModel().toLowerCase().contains(q)
                        || c.getLicensePlate().toLowerCase().contains(q))
                .toList();
    }

    public List<Client> searchClientsByName(String query) {
        String q = query.toLowerCase();
        return clientDao.findAll().stream()
                .filter(c -> c.getFullName().toLowerCase().contains(q)
                        || c.getPassport().contains(q))
                .toList();
    }

    public List<Car> filterCarsByStatus(CarStatus status) {
        return carDao.findAll().stream()
                .filter(c -> c.getStatus() == status)
                .toList();
    }

    public List<Rental> filterRentalsByStatus(RentalStatus status) {
        return rentalDao.findAll().stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Car> sortCarsByPrice(boolean ascending) {
        Comparator<Car> cmp = Comparator.comparingDouble(Car::getPricePerDay);
        if (!ascending) cmp = cmp.reversed();
        return carDao.findAll().stream().sorted(cmp).toList();
    }

    public List<Rental> sortRentalsByStartDate() {
        return rentalDao.findAll().stream()
                .sorted(Comparator.comparing(Rental::getStartDate))
                .toList();
    }

    // ========== СТАТИСТИКА ==========

    public Map<String, Number> getStatistics() {
        List<Client> clients = clientDao.findAll();
        List<Car> cars = carDao.findAll();
        List<Rental> rentals = rentalDao.findAll();

        long activeRentals = rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE).count();
        long availableCars = cars.stream()
                .filter(c -> c.getStatus() == CarStatus.AVAILABLE).count();
        double avgPrice = rentals.stream()
                .mapToDouble(Rental::getTotalPrice).average().orElse(0);
        double totalIncome = rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .mapToDouble(Rental::getTotalPrice).sum();

        Map<String, Number> stats = new HashMap<>();
        stats.put("totalClients", clients.size());
        stats.put("totalCars", cars.size());
        stats.put("totalRentals", rentals.size());
        stats.put("activeRentals", activeRentals);
        stats.put("availableCars", availableCars);
        stats.put("avgPrice", avgPrice);
        stats.put("totalIncome", totalIncome);
        return stats;
    }
}