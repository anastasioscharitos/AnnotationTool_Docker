package com.example.application.backEnd.handledFiles;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Η κλάση ImageController λειτουργεί ως ένας Resource Server για τις εικόνες οι οποίες
 * είναι αποθηκευμένες τοπικά και θα προβληθούν σε έναν browser. Επιτρέπεται η
 * πρόσβαση μέσω HTTP GET αιτημάτων και επιστρέφονται ως resources για την προβολή
 * σε browser.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final HandledProjects handledProjects;
    private final Path baseDir = Path.of("data");

    /**
     * Κατασκευαστής κλάσης
     * @param handledProjects το τρέχων project
     */
    public ImageController(HandledProjects handledProjects){
        this.handledProjects = handledProjects;
    }

    /**
     *Ανακτά και επιστρέφει μια εικόνα ως HTTP response. Η μέθοδος κατασκευάζει δυναμικά
     * τη διαδρομή data/projectName/images/filename. Χρησιμοποιεί το normalize για
     * ασφάλεια και το probeContentType για προσδιορισμό του τύπου περιεχομένου
     * @param projectName το όνομα του project
     * @param filename όνομα αρχείου εικόνας
     * @return την εικόνα ως Resource
     */
    @GetMapping("/{projectName}/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String projectName, @PathVariable String filename){

        try{

            Path projectImageDir = baseDir.resolve(projectName).resolve("images").normalize();
            Path imagePath = projectImageDir.resolve(filename).normalize();

            if(imagePath == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if(!Files.exists(imagePath)){
            //    System.out.println("Δεν υπάρχει η εικόνα");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if(!imagePath.startsWith(projectImageDir)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(imagePath.toUri());
            String contentType = Files.probeContentType(imagePath);

            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-cache").
                    contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM).
                    body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

