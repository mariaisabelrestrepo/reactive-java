package com.co.reactor.springreactor.models.dao;

import com.co.reactor.springreactor.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria,String>{
}
