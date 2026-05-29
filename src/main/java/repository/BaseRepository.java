package repository;

import models.mappers.Mapper;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract public class BaseRepository<T> implements IRepository<T> {
    abstract String getInsertQuery();

    abstract String getUpdateQuery();

    abstract Mapper<T> getMapper();

    abstract String tableName();

    abstract void setPstmCreate(PreparedStatement pstm, T obj) throws SQLException;

    abstract void setPstmUpdate(PreparedStatement pstm, T obj) throws SQLException;

    public T create(T obj) {
        String query = this.getInsertQuery();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            this.setPstmCreate(pstm, obj);
            pstm.executeUpdate();

            ResultSet res = pstm.getGeneratedKeys();
            if (res.next()) {
                int id = res.getInt(1);
                return this.getById(id);
            }

            try (
                    Statement idStatement = connection.createStatement();
                    ResultSet idResult = idStatement.executeQuery("SELECT LAST_INSERT_ID()")
            ) {
                if (idResult.next()) {
                    int id = idResult.getInt(1);
                    return this.getById(id);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
        throw new IllegalStateException("Record was saved, but generated id was not returned.");
    }

    public T update(T obj) {
        String query = this.getUpdateQuery();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            this.setPstmUpdate(pstm, obj);
            pstm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public T getById(int id) {
        String query = "SELECT * FROM " + tableName() + " WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, id);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                return getMapper().getFromResultSet(res);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }

    public boolean delete(int objId) {
        String query = "DELETE FROM " + tableName() + " WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, objId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}