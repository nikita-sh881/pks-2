package rentcar.dao;

import rentcar.db.DatabaseManager;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDao {

    public List<Client> findAll() {
        String sql = "SELECT * FROM clients ORDER BY id";
        List<Client> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения клиентов: " + e.getMessage());
        }
        return list;
    }

    public Client findById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска клиента: " + e.getMessage());
        }
        throw new EntityNotFoundException("Клиент с ID " + id + " не найден.");
    }

    public void insert(Client client) {
        String sql = "INSERT INTO clients (full_name, passport, phone) VALUES (?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPassport());
            ps.setString(3, client.getPhone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось добавить клиента: " + e.getMessage());
        }
    }

    public void update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, passport = ?, phone = ? WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPassport());
            ps.setString(3, client.getPhone());
            ps.setInt(4, client.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось изменить клиента: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить клиента: " + e.getMessage());
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("passport"),
                rs.getString("phone"));
    }
}