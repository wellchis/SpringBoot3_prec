package me.silvernine.tutorial.repository;

import java.util.Optional;
import me.silvernine.tutorial.entity.Users;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, String> {

    // 쿼리 수행 시, Lazy조회(지연로딩)가 아닌 Eager조회(즉시로딩)로 authorities 정보를 같이 가져옴
    @EntityGraph(attributePaths =  "authorities")
    // username을 기준으로 User정보를 가져올 때, 권한 정보도 같이 가져옴
    Optional<Users> findOneWithAuthoritiesByUsername(String username);
}
