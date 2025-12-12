package com.mycom.myapp.follow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.entity.FollowKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, FollowKey> {

    @Query("select f from Follow f join fetch f.userDest u where u.usersId = :usersId order by f.createdAt desc")
    Page<Follow> findFollowersByUsersIdDest(Pageable pageable, @Param("usersId") Integer usersId);

    @Query("select f from Follow f join fetch f.userSrc u where u.usersId = :usersId order by f.createdAt desc")
    Page<Follow> findFollowingsByUsersIdSrc(Pageable pageable, @Param("usersId") Integer usersId);
}
