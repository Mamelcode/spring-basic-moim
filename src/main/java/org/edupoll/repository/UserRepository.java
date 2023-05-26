package org.edupoll.repository;

import java.awt.print.Pageable;
import java.util.List;

import org.edupoll.model.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>{
	List<User> findByIdContainingOrNickContaining
	(String id, String nick, PageRequest pageRequest);
	
	List<User> findByIdContainingOrNickContaining
	(String id, String nick);
}
