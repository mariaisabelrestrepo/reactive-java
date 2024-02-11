package com.co.reactor.springreactor.models.services;

import com.co.reactor.springreactor.models.documents.Categoria;
import com.co.reactor.springreactor.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoServices {

    public Flux<Producto>findAll();
    public Flux<Producto>findAllConNombre();
    public Mono<Producto>findById(String id);
    public Mono<Producto>save(Producto producto);
    public Mono<Void>delete(Producto producto);
    public Flux<Categoria>findAllCategoria();
    public Mono<Categoria>findCategoriaById(String id);
    public Mono<Categoria>saveCategoria(Categoria categoria);
}
