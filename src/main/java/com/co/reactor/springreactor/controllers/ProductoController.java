package com.co.reactor.springreactor.controllers;

import com.co.reactor.springreactor.models.documents.Categoria;
import com.co.reactor.springreactor.models.documents.Producto;
import com.co.reactor.springreactor.models.services.ProductoServices;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoServices services;

    @Value("${config.uploads.path}")
    private String path;


    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categorias")
    public Flux<Categoria> categorias() {
        return services.findAllCategoria();

    }

    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();
        Resource imagen = new UrlResource(ruta.toUri());
        return Mono.just(
                ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
                        .body(imagen)
        );
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {
        return services.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("titulo", "detalle del producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));

    }

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = services.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });

        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", productos);
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("boton", "crear");

        return Mono.just("form");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = services.findById(id).doOnNext(p -> {
            log.info("producto" + p.getNombre());
        }).defaultIfEmpty(new Producto());

        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("producto", productoMono);
        model.addAttribute("boton", "editar");
        return Mono.just("form");
    }


    @GetMapping("/form-v2/{id}")
    public Mono<String> editarv2(@PathVariable String id, Model model) {
        return services.findById(id).doOnNext(p -> {

                    log.info("producto" + p.getNombre());
                    model.addAttribute("titulo", "Editar producto");
                    model.addAttribute("producto", p);
                    model.addAttribute("boton", "editar");
                }).defaultIfEmpty(new Producto())
                .flatMap((p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                }))
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));


    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart(name = "file") FilePart filePart, SessionStatus sessionStatus) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Errores en el formulario de producto");
            model.addAttribute("boton", "guardar");
            return Mono.just("form");
        } else {
            sessionStatus.setComplete();

            Mono<Categoria> categoria = services.findCategoriaById(producto.getCategoria().getId());

            return categoria.flatMap(c -> {
                        if (producto.getCreateAt() == null) {
                            producto.setCreateAt(new Date());
                        }
                        if (!filePart.filename().isEmpty()) {
                            producto.setFoto(UUID.randomUUID().toString() + " " + filePart.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", "")
                            );
                        }
                        producto.setCategoria(c);
                        return services.save(producto);
                    }).doOnNext(p -> {
                        log.info("Categoria asignada: " + p.getCategoria().getNombre() + " Id categoria: " + p.getCategoria().getId());
                        log.info("producto guardado: " + p.getNombre() + " Id: " + p.getId());
                    })
                    .flatMap((p -> {
                        if (!filePart.filename().isEmpty()) {
                            return filePart.transferTo(new File(path + p.getFoto()));
                        }
                        return Mono.empty();
                    }))


                    .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = services.findAllConNombre().delayElements(Duration.ofSeconds(1));

        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = services.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        }).repeat(5000);

        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarchunked(Model model) {
        Flux<Producto> productos = services.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        }).repeat(5000);

        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        return "listar-chunked";
    }


    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return services.findById(id).defaultIfEmpty(new Producto())
                .flatMap((p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(p);
                })).flatMap(services::delete).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
    }
}
