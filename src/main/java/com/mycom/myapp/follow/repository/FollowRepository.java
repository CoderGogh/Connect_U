package com.mycom.myapp.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.entity.FollowKey;

public interface FollowRepository extends JpaRepository<Follow, FollowKey> {
}
