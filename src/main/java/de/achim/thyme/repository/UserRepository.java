package de.achim.thyme.repository;


import org.springframework.data.repository.CrudRepository;
import java.util.List;
import de.achim.thyme.entity.UserThyme;

public interface UserRepository extends CrudRepository<UserThyme, Long>{
	
    List<UserThyme> findByName(String name);

}
