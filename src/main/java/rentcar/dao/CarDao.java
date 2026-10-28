package rentcar.dao;

import rentcar.db.DatabaseManager;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.Car;
import rentcar.model.CarStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDao {

    public List<Car> findAll() {
        String sql = "SELECT * FROM cars ORDER BY id";
        List<Car> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения автомобилей: " + e.getMessage());
        }
        return list;
    }

    public Car findById(int id) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска автомобиля: " + e.getMessage());
        }
        throw new EntityNotFoundException("Автомобиль с ID " + id + " не найден.");
    }

    public void insert(Car car) {
        String sql = """
                INSERT INTO cars (brand, model, license_plate, year, price_per_day, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setString(3, car.getLicensePlate());
            ps.setInt(4, car.getYear());
            ps.setDouble(5, car.getPricePerDay());
            ps.setString(6, car.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось добавить автомобиль: " + e.getMessage());
        }
    }

    public void update(Car car) {
        String sql = """
                UPDATE cars SET brand=?, model=?, license_plate=?, year=?,
                                price_per_day=?, status=? WHERE id=?
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setString(3, car.getLicensePlate());
            ps.setInt(4, car.getYear());
            ps.setDouble(5, car.getPricePerDay());
            ps.setString(6, car.getStatus().name());
            ps.setInt(7, car.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось изменить автомобиль: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить автомобиль: " + e.getMessage());
        }
    }

    private Car map(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getString("license_plate"),
                rs.getInt("year"),
                rs.getDouble("price_per_day"),
                CarStatus.valueOf(rs.getString("status")));
    }
}