package com.mycom.myapp.follow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.entity.FollowKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowKey> {

    @Query("select f from Follow f join fetch f.userDest u where u.usersId = :usersId order by f.createdAt desc")
    Page<Follow> findFollowersByUsersIdDest(Pageable pageable, @Param("usersId") Integer usersId);

    @Query("select f from Follow f join fetch f.userSrc u where u.usersId = :usersId order by f.createdAt desc")
    Page<Follow> findFollowingsByUsersIdSrc(Pageable pageable, @Param("usersId") Integer usersId);

    /**
     * 특정 유저가 팔로우하고 있는 모든 유저 식별자 조회
     * @param usersId 특정 유저의 식별자
     * @return 식별자 리스트
     */
    @Query("select f.userDest.usersId from Follow f where f.userSrc.usersId = :usersId")
    List<Integer> findAllByUserSrc(@Param("usersId") Integer usersId);

    @Query("select count(1) from Follow f where f.userSrc.usersId = :usersId and f.userDest.usersId = :targetId")
    Optional<Integer> existsByUsersIdAndTargetId(@Param("usersId") Integer usersId, @Param("targetId") Integer targetId);
}
