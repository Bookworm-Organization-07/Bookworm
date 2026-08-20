package com.example.Controller;

import com.example.Repository.GenereRepository;
import com.example.models.Genere;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generes")
public class GenereController {

    private final GenereRepository genereRepository;

    public GenereController(GenereRepository genereRepository) {
        this.genereRepository = genereRepository;
    }

    @GetMapping
    public List<Genere> getAllGeneres() {
        return genereRepository.findAll();
    }
}
