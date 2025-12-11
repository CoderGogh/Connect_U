package com.mycom.myapp.users.repository;

import com.mycom.myapp.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    @Query("""
    select u
    from Users u
    join fetch u.usersRoles ur
    join fetch ur.role
    where u.email = :email and u.isDeleted = false
    """)
    Optional<Users> findByEmailForLogin(@Param("email") String email);

    boolean existsByEmail(String email);
}
