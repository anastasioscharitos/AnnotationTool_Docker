package com.example.application.backEnd.handledFiles;

import com.example.application.backEnd.dataHandling.AnnotatedData;
import com.example.application.backEnd.dataHandling.BaseAnnotation;
import com.example.application.backEnd.dataHandling.WebPageImage;
import com.example.application.backEnd.dataHandling.WebPageImageAddText;
import com.example.application.backEnd.scraping.CheckURL;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Στην κλάση HandledJsonFile γίνεται η διαχείριση του αρχείου json. Αναλαμβάνει τη σειριοποίηση και
 * αποσειριοποίηση των δεδομένων σε μορφή json χρησιμοποιώντας έναν ObjectMapper.
 * Γίνεται η αρχικοποίηση του μονοπατιού του json καθώς και του φακέλου που θα αποθηκευτούν οι εικόνες.
 * Η κλάση διαχειρίζεται τη "φόρτωση", την αποθήκευση και τη διαγραφή μιας λίστας
 * AnnotatedDate στο json αρχείο. Επίσης, γίνεται και η αποθήκευση των εικόνων
 * που έχουν επισημανθεί. Οι εικόνες "κατεβαίνουν" και αποθηκεύονται στον φάκελο images.
 * Για να μπορέσουν να αποθηκευτούν θα πρέπει να ελεγχθεί το πιστοποιητικό από διευθύνσεις HTTPS. Ο χρήστης μπορεί να
 * επιλέξει να παρακάμψει τον έλεγχο για να πραγματοποιηθεί η αποθήκευση.
 */
public class HandledJsonFile {

    private final HandledProjects handledProjects;
    private ObjectMapper objectMapper;
    private String path = "";
    private String pathDirImage = "";

    /**
     * Κατασκευαστής της κλάσης. Αρχικοποιείται το ObjectMapper. Ο κατασκευαστής
     * έχει ως όρισμα το HandledProject
     * @param handledProjects, κλάση διαχείρισης των μονοπατιών
     */
    public HandledJsonFile(HandledProjects handledProjects) {
        this.handledProjects = handledProjects;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Αρχικοποιεί τα μονοπάτια με το project
     */
    public void initHandledJsonFile() {
        path = handledProjects.getPathJson();
        pathDirImage = handledProjects.getPathImages();
    }

    /**
     * Ανάκτηση των δεδομένων από το αρχείο json. Ο objectMapper διαβάζει το αρχείο json
     * και επιστρέφει μια λίστα με AnnotatedData>. Η μέθοδος διαβάζει το json ως ένα map όπου
     * To το κλειδί είναι η διεύθυνση url που γίνεται η επισήμανση. Το Value είναι ένα αντικείμενο
     * AnnotatedData. Η μέθοδος επιστρέφει μία λίστα με τα AnnotatedData. Αν η λίστα
     * είναι κενή τότε επιστρέφει μια νέα λίστα.
     * @return λίστα με AnnotatedData
     */
    public List<AnnotatedData> loadJsonFile() throws Exception{
        File file = new File(path);

        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdir();
        }

        Map<String, AnnotatedData> annotatedDataList = new HashMap<>();
        if (!file.exists() || file.length() == 0) {
            return annotatedDataList.values().stream().toList();
        }


            annotatedDataList = objectMapper.readValue(file, new TypeReference<Map<String, AnnotatedData>>() {
            });

        if (annotatedDataList.isEmpty() || annotatedDataList == null) {
            return new ArrayList<>();
        }
        return annotatedDataList.values().stream().toList();
    }

    /**
     * Αποθήκευση λίστας απο AnnotatedData. Στην αρχή διαβάζεται το αρχείο json και
     * επιστρέφει μια λίστα με AnnotatedData. Αυτή η λίστα μετατρέπεται σε Map, κλειδί είναι
     * η διεύθυνση που έχει γίνει η επισήμανση (AnnotatedData::getPageUrl). Στον χάρτη
     * προστίθενται τα στοιχεία της λίστας που υπάρχει στο όρισμα της μεθόδου. Για κάθε στοιχείο
     * της λίστας ελέγχεται αν υπάρχει είδη το url και προστίθεται το νέο στοιχείο. Διαφορετικά
     * δημιουργείται νέο κλειδί με την τιμή του το αντίστοιχο AnnotatedData. Στο τέλος καλείται
     * η μέθοδος saveMap όπου αποθηκεύει το χάρτη που έχει δημιουργηθεί.
     * @param baseAnnotations λίστα αντικειμένων
     * @return true αν πραγματοποιήθηκε η αποθήκευση διαφορετικά false
     */
    public boolean saveBaseAnnotationNew(List<BaseAnnotation> baseAnnotations) {
        try {
            List<AnnotatedData> annotatedDataList = loadJsonFile();

            Map<String, AnnotatedData> map = new HashMap<>();

            for (AnnotatedData data : annotatedDataList) {
                map.put(data.getPageUrl(), data);
            }

            for (BaseAnnotation base : baseAnnotations) {
                String url = base.getUrl();

                AnnotatedData data = map.computeIfAbsent(url, k -> {
                    AnnotatedData d = new AnnotatedData();
                    d.setPageUrl(url);
                    return d;
                });

                data.addItem(base);
            }

            saveMap(map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Αποθήκευση του χάρτη που έχει δημιουργηθεί από τη μέθοδο saveBaseAnnotationNew.
     * Η αποθήκευση του χάρτη γίνεται με το ObjectMapper
     * @param map
     */
    private void saveMap(Map<String, AnnotatedData> map) {
        try (FileWriter writer = new FileWriter(path)) {
            objectMapper.writeValue(writer, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Αφαιρεί συγκεκριμένες επισημάνσεις από το αρχείο json
     * @param baseAnnotations
     */
    public boolean deleteBaseAnnotation(List<BaseAnnotation> baseAnnotations) {
        try {
            List<AnnotatedData> annotatedDataList = loadJsonFile();

            Map<String, AnnotatedData> map = new HashMap<>();

            for (AnnotatedData data : annotatedDataList) {
                map.put(data.getPageUrl(), data);
            }

            for (BaseAnnotation base : baseAnnotations) {
                String url = base.getUrl();

                AnnotatedData data = map.get(url);

                if (data == null) {
                    continue;
                }

                data.removeItem(base);
            }
            saveMap(map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Πραγματοποιεί τη λήψη μιας εικόνας και την αποθήκευσή της τοπικά. Για συνδέσεις HTTPS
     * μπορεί να γίνει παράκαμψη του SSL.
     * Το όνομα παράγεται αυτόματα από το id της επισήμανσης.
     * @param img, αντικείμενο BaseAnnotation που έχει την εικόνα
     * @param ignoreSSL, παράκαμψη του SSL
     * @return, επιστρέφει String το τελικό όνομα της εικόνας ή "false" σε περίπτωση αποτυχίας
     */
    public String saveImage(BaseAnnotation img, boolean ignoreSSL) {
        if (img instanceof WebPageImage || img instanceof WebPageImageAddText) {
            Pair<String,String> pair = createImagePath(img);
            String pathImage = pair.getLeft();
            String type = pair.getRight();
            if(pathImage.equals("false")){
                return "false";
            }

            try {
                URL url = URI.create(img.getImagePath()).toURL();

                URLConnection connection = url.openConnection();

                connection.setRequestProperty("User-Agent", "Mozilla/5.0 ");

                if(ignoreSSL && connection instanceof HttpsURLConnection) {
                    applySSLBypass((HttpsURLConnection) connection);

                }

                return writeImage(connection, img, pathImage, type);
            } catch (MalformedURLException e) {
                return "false";

            } catch (IOException e) {
                return "false";

            } catch (NoSuchAlgorithmException e) {
                return "false";

            } catch (KeyManagementException e) {
                return "false";

            }
        } else {
            return "false";
        }
    }

    /**
     * Παράκαμψη του SSL. Δημιουργείται η σύνδεση
     * @param connection
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void applySSLBypass(HttpsURLConnection connection) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> true);

    }

    /**
     * Η μέθοδος δημιουργεί το μονοπάτι αποθήκευσης της εικόνας. Στην αρχή ελέγχεται αν η διεύθυνση είναι
     * έγκυρη. Από το url παίρνουμε τον τύπο της εικόνας και επιστρέφει το μονοπάτι της εικόνας με νέο όνομα
     * της εικόνας, το οποίο είναι το id του BaseAnnotation.
     * @param img, αντικείμενο BaseAnnotation
     * @return Pair<Μονοπάτι εικόνας, Τύπος εικόνας>
     */
    private Pair<String, String> createImagePath(BaseAnnotation img) {
        String pathImage = "";
        String type = "";

        if (img instanceof WebPageImage || img instanceof WebPageImageAddText) {
            CheckURL checkURL = new CheckURL(img.getImagePath());

            if (!checkURL.isValid()) {
                return new Pair<>("false", "");
            }
            String typeImage = img.getImagePath().toLowerCase();

            if (typeImage.contains("?")) {
                typeImage = typeImage.split("\\?")[0];
            }

            type = "jpg";

            int lastDot = typeImage.lastIndexOf('.');
            if (lastDot > 0) {
                type = typeImage.substring(lastDot + 1);
            }

            pathImage = pathDirImage + img.getId() + "." + type;

        }
        return new Pair<>(pathImage, type);
    }

    /**
     * Αποθήκευση εικόνας με BufferedInputStream
     * @param connection, η σύνδεση με τον ιστότοπο της εικόνας
     * @param img, αντικείμενο BaseAnnotation
     * @param pathImage, μονοπάτι εικόνας
     * @param type, τύπος εικόνας
     * @return το όνομα_εικόνας.τύπο_εικόνας
     */
    private String writeImage( URLConnection connection, BaseAnnotation img, String pathImage, String type){
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(pathImage)) {

            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return img.getId() + "." + type;

        } catch (IOException e) {

            return "false";
        }
    }
}
