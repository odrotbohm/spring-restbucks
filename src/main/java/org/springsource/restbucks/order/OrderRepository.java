package org.springsource.restbucks.order;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

/**
 * Repository to manage {@link Order} instances.
 * 
 * @author Oliver Gierke
 */
@RestResource(path = "orders", rel = "orders")
public interface OrderRepository extends CrudRepository<Order, Long> {

}
