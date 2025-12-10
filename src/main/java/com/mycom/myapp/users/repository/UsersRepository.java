package com.mycom.myapp.users.repository;

import com.mycom.myapp.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
}
