package com.mycom.myapp.users.repository;

import com.mycom.myapp.users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
