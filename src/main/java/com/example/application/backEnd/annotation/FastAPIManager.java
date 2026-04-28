package com.example.application.backEnd.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Η κλάση FastAPIManager είναι η ενδιάμεση μεταξύ της java και του FastAPI
 * Διαχειρίζεται το Python Script και ανταλλάσει δεδομένα μέσω HTTP από την πύλη 8000.
 * Όταν στέλνονται αιτήματα αναμένεται απάντηση εντός 2 λεπτών (120sec). Αν δε ληφθεί απάντηση
 * τότε γίνεται επανεκκίνηση του server και το αίτημα πρέπει να επαναληφθεί.
 * Απαντήσεις στα αιτήματα:
 * Επισήμανση κειμένου, Επιστρέφει Map όπου το κλειδί είναι η οντότητα και Value είναι
 * μια λίστα με όλες τις ετικέτες του κειμένου που αναφέρονται στην οντότητα.
 * Επισήμανση εικόνων, Επιστρέφει Map όπου το κλειδί είναι η διεύθυνση της εικόνας (URL εικόνας)
 * και το Value είναι μια λίστα από ετικέτες που αναφέρονται στην εικόνα. Επίσης, για να μην είναι
 * όλα τα μοντέλα φορτωμένα στη μνήμη κάθε φορά φορτώνεται το μοντέλο επισήμανσης
 * εικόνας που θα χρησιμοποιηθεί. Τέλος, πριν κλείσει η εφαρμογή εκτελείται η μέθοδος
 * onExit η οποία κλείνει τον server.
 */

@Service
public class FastAPIManager {
    private String fastApiUrl = System.getenv("FASTAPI_URL") ;
    private HttpClient client = HttpClient.newBuilder().
            version(HttpClient.Version.HTTP_1_1).
            connectTimeout(Duration.ofSeconds(10)).build();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Κατασκευαστής κλάσης
     */
    public FastAPIManager(){
        if (fastApiUrl == null || fastApiUrl.isBlank()) {
            fastApiUrl = "http://python-app:8000/";
        }
    }

    public String getFastApiUrl() {
        return fastApiUrl;
    }

    /**
     * Η μέθοδος annotate στέλνει δεδομένα DataForAnnotation στο endPoint του FastAPI server.
     * Αν δε λάβει απάντηση εντός 2 λεπτών (120 sec) προκαλεί επανεκκίνηση του server μέσω της
     * μεθόδου handleTimeout.
     * Επιστρέφει ένα map όπου στην περίπτωση επισήμανσης κειμένου το κλειδί είναι η ετικέτα επισήμανση
     * και Value είναι μια λίστα με όλες τις λέξεις του κειμένου που αναφέρονται στην ετικέτα.
     *  Για την περίπτωση των εικόνων το κλειδί είναι η διεύθυνση της εικόνας και το Value
     *  είναι μια λίστα επισημειώσεων που αναφέρονται στην εικόνα.
     * @param endPoint του FastAPI (/TextAnnotation ή /ImageAnnotation ανάλογα με την επισήμανσης
     * @param annotateData Δεδομένα για επισήμανσης
     * @return χάρτη με τα αποτελέσματα της επισήμανσης
     */
    public Map<String, List<String>> annotate(String endPoint, DataForAnnotation annotateData){

        try {
            String jsonBody = objectMapper.writeValueAsString(annotateData);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fastApiUrl + endPoint)).
                    timeout(Duration.ofSeconds(120)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>() {
            });
        } catch (HttpTimeoutException time) {
            System.out.println("timeout");
            Map<String, List<String>> timeout = new HashMap<>();
            timeout.put("timeout", new ArrayList<>());
            return timeout;
        } catch (Exception e) {
            Map<String, List<String>> exception = new HashMap<>();
            exception.put("exception", new ArrayList<>());
            return exception;
        }
    }

    /**
     * Η μέθοδος loadModel χρησιμοποιείται για να φορτώσει τα μοντέλα επισήμανσης εικόνας.
     * Η μέθοδος επιστρέφει χάρτη με τα μοντέλα επισήμανσης που είναι στη μνήμη
     * @param model μοντέλο επισήμανσης εικόνας
     * @param endPoint του FastAPI (/Update_Settings)
     * @return χάρτη με τα μοντέλα που βρίσκονται στη μνήμη
     */
    public Map<String, String> loadModel(String model, String endPoint){
        try {
            Map<String, String> requestModel = new HashMap<>();
            requestModel.put("img_model", model);

            String jsonBody = objectMapper.writeValueAsString(requestModel);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fastApiUrl + endPoint)).
                    timeout(Duration.ofSeconds(120)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {
            });

        } catch (HttpTimeoutException time) {
            System.out.println("timeout");

            Map<String, String> timeout = new HashMap<>();
            timeout.put("timeout", "");
            return timeout;
        } catch (Exception e) {
            Map<String, String> errorLoad = new HashMap<>();
            errorLoad.put("errorLoad", "");
            return errorLoad;
        }
    }

    /**
     * Η μέθοδος χρησιμοποιείται για να δει ποιο μοντέλο επισήμανσης εικόνων είναι φορτωμένο
     * στη μνήμη. Επιστρέφει έναν χάρτη με το μοντέλο επισήμανσης.
     * @param endPoint του FastAPI("/get_status")
     * @return χάρτη με το μοντέλο που είναι στη μνήμη
     */
    public Map<String, String> getStatus(String endPoint){
        try {
            Map<String, String> requestModel = new HashMap<>();
            String jsonBody = objectMapper.writeValueAsString(requestModel);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fastApiUrl + endPoint)).
                    timeout(Duration.ofSeconds(120)).
                    header("Accept", "application/json").GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {
            });
        } catch (HttpTimeoutException time) {
            Map<String, String> errorStatus = new HashMap<>();
            errorStatus.put("timeout", "");
            return errorStatus;
        } catch (Exception e) {
            Map<String, String> errorStatus = new HashMap<>();
            errorStatus.put("errorStatus", "");
            return errorStatus;
        }
    }

    /**
     * Η μέθοδος isServerAlive εξετάζει αν υπάρχει επικοινωνία με τον server.
     * Στέλνεται αίτημα και αναμένεται απάντηση εντός 10sec
     * @return true αν μέσα σε 10 sec απαντήσει με κωδικό 200 ο server διαφορετικά false
     */
    public boolean isServerAlive(){
        try {
            HttpRequest request = HttpRequest.newBuilder().
                    uri(URI.create(fastApiUrl)).timeout(Duration.ofSeconds(10)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Η μέθοδος convertData μετατρέπει το αποτέλεσμα της επισήμανσης από
     * map σε μια λίστα
     * @param map χάρτης επισημάνσεων από την python
     * @return λίστα με τις επισημάνσεις
     */
    public List<String> convertData(Map<String, List<String>> map){

        List<String> list = new ArrayList<>();

        for (Map.Entry<String, List<String>> e : map.entrySet()) {

            String values = String.join(", ", e.getValue());
            list.add(e.getKey() + " : " + values);
        }
        return list;
    }

    /**
     * Η μέθοδος convertModel μετατρέπει την απάντηση του FastAPI server για τα μοντέλα
     * σε ένα String
     * @param map τα μοντέλα που είναι φορτωμένα στη μνήμη
     * @return String τα μοντέλα επισήμανσης
     */
    public String convertModel(Map<String, String> map){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> m : map.entrySet()) {
            builder.append(m.getKey() + " - " + m.getValue() + ", ");
        }

        return builder.toString().trim();
    }

}
