package com.mycom.myapp.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.users.entity.UsersRole;
import com.mycom.myapp.users.entity.UsersRoleKey;

public interface UsersRoleRepository extends JpaRepository<UsersRole, UsersRoleKey> {
}
