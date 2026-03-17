package com.example.tour_management.repository;


import com.example.tour_management.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Queue;

@Repository
@Transactional
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> searchUsers(String name, String role) {

        StringBuilder sql = new StringBuilder("""
        SELECT u.*
        FROM users u
        JOIN roles r ON u.role_id = r.id
        WHERE 1=1
    """);

        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND u.user_name LIKE :name ");
        }

        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND r.name = :role ");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), User.class);

        if (name != null && !name.trim().isEmpty()) {
            query.setParameter("name", "%" + name + "%");
        }

        if (role != null && !role.trim().isEmpty()) {
            query.setParameter("role", role);
        }

        return query.getResultList();
    }

}
