package com.mycom.myapp.users.repository;

import com.mycom.myapp.users.entity.UsersRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRoleRepository extends JpaRepository<UsersRole, Integer> {
}
