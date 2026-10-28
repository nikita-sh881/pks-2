package rentcar.dao;

import rentcar.db.DatabaseManager;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.Rental;
import rentcar.model.RentalStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDao {

    public List<Rental> findAll() {
        String sql = "SELECT * FROM rentals ORDER BY id";
        List<Rental> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения аренд: " + e.getMessage());
        }
        return list;
    }

    public Rental findById(int id) {
        String sql = "SELECT * FROM rentals WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска аренды: " + e.getMessage());
        }
        throw new EntityNotFoundException("Аренда с ID " + id + " не найдена.");
    }

    public void create(int clientId, int carId, LocalDate start, LocalDate end, double total) {
        String sql = """
                INSERT INTO rentals (client_id, car_id, start_date, end_date, total_price, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String updateCar = "UPDATE cars SET status = 'RENTED' WHERE id = ?";

        try (Connection c = DatabaseManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql);
                 PreparedStatement up = c.prepareStatement(updateCar)) {
                ps.setInt(1, clientId);
                ps.setInt(2, carId);
                ps.setDate(3, Date.valueOf(start));
                ps.setDate(4, Date.valueOf(end));
                ps.setDouble(5, total);
                ps.setString(6, RentalStatus.ACTIVE.name());
                ps.executeUpdate();

                up.setInt(1, carId);
                up.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("Не удалось создать аренду: " + e.getMessage());
        }
    }

    public void update(Rental rental) {
        String sql = """
                UPDATE rentals SET client_id=?, car_id=?, start_date=?, end_date=?,
                                   total_price=?, status=? WHERE id=?
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, rental.getClientId());
            ps.setInt(2, rental.getCarId());
            ps.setDate(3, Date.valueOf(rental.getStartDate()));
            ps.setDate(4, Date.valueOf(rental.getEndDate()));
            ps.setDouble(5, rental.getTotalPrice());
            ps.setString(6, rental.getStatus().name());
            ps.setInt(7, rental.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось изменить аренду: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM rentals WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить аренду: " + e.getMessage());
        }
    }

    public void complete(int rentalId, int carId) {
        String updateRental = "UPDATE rentals SET status='COMPLETED' WHERE id = ?";
        String updateCar = "UPDATE cars SET status='AVAILABLE' WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(updateRental);
                 PreparedStatement up = c.prepareStatement(updateCar)) {
                ps.setInt(1, rentalId);
                ps.executeUpdate();
                up.setInt(1, carId);
                up.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("Не удалось завершить аренду: " + e.getMessage());
        }
    }

    private Rental map(ResultSet rs) throws SQLException {
        return new Rental(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("car_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("total_price"),
                RentalStatus.valueOf(rs.getString("status")));
    }
}