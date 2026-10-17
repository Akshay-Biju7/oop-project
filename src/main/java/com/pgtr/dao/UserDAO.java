package com.pgtr.dao;

import com.pgtr.model.User;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Data Access Object interface for User authentication and profile persistence.
 */
public interface UserDAO {
    boolean createUser(User user) throws SQLException;
    Optional<User> getUserById(String id) throws SQLException;
    Optional<User> getUserByEmail(String email) throws SQLException;
    boolean updateProfile(User user) throws SQLException;
    boolean updatePassword(String userId, String newPasswordHash) throws SQLException;
    boolean emailExists(String email) throws SQLException;
}
