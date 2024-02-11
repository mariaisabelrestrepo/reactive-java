package com.co.reactor.springreactor;

import com.co.reactor.springreactor.models.documents.Categoria;
import com.co.reactor.springreactor.models.documents.Producto;
import com.co.reactor.springreactor.models.services.ProductoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringReactorApplication.class);

    @Autowired
    private ProductoServiceImpl productoServices;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();


        Categoria electronico = new Categoria("Electronico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computacion");
        Categoria muebles = new Categoria("muebles");

        Flux.just(electronico,deporte,computacion,muebles)
                        .flatMap(productoServices::saveCategoria)
                                .doOnNext(c ->{
                                    log.info("Categoria Creada: "+ c.getNombre()+ " Id: "+ c.getId());
                                }).thenMany(
        Flux.just(
                        new Producto("Tv Smart", 12378,electronico),
                        new Producto("Camara 33¨", 145982,electronico),
                        new Producto("mesa portatil¨", 145982,muebles),
                        new Producto("PC HP¨", 67824,computacion),
                        new Producto("Teclado Gamer¨", 79000,computacion),
                        new Producto("Bicicleta¨", 345999,deporte),
                        new Producto("Patines¨", 7835000,deporte),
                        new Producto("Camara digital¨", 79000,electronico)
                )
                .flatMap(producto -> {
                    producto.setCreateAt(new Date());
                    return productoServices.save(producto);
                }))
                .subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
    }
}
