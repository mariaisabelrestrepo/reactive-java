package com.co.reactor.springreactor.models.dao;

import com.co.reactor.springreactor.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoDao extends ReactiveMongoRepository<Producto,String> {

}