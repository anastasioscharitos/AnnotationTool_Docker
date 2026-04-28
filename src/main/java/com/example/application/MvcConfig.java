package com.example.application;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Αυτή η κλάση επιτρέπει στον browser να αποκτήσει πρόσβαση σε αρχεία τα οποία
 * είναι έξω από τον κώδικα της εφαρμογής. Το Spring Boot, για λόγους ασφαλείας δεν
 * μπορεί να δει αρχεία του συστήματος. Από το /data/images/ διαβάζονται οι εικόνες.
 */
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String dataPath = Paths.get("data").toAbsolutePath().toString();

        registry.addResourceHandler("/images/**").
                addResourceLocations("file:" + dataPath + "/");
    }

}
