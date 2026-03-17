package com.example.tour_management.repository;

import com.example.tour_management.entity.User;
import java.util.List;

public interface UserRepositoryCustom {
    List<User> searchUsers(String name, String role);
}
