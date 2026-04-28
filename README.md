# Annotation Tool Docker README

Οδηγίες εγκατάστασης και εκτέλεσης 

- Προαπαιτούμενα:
  1) Εγκατάσταση του Docker Desktop από τη διεύθυνση https://www.docker.com/get-started/
  2) Προτεινόμενη ρύθμιση: Μετά την εγκατάσταση, καλό είναι να αλλάξετε η θέση του εικονικού
      δίσκου που δημιουργεί το Docker Desktop, για να μη γεμίσει τον δίσκο C:
      - Πατήστε το γρανάζι (πάνω δεξιά - Settings)
      - Από το πλαϊνό menu επιλέξτε Resources
      - Από το Advanced αλλάξτε τo Disk Image location

- Λήψη των Images
    Από ένα τερματικό εκτελέστε τις εντολές:
         a) docker pull ancharitos/annotationtooldockercontainers-java-app
         b) docker pull ancharitos/annotationtooldockercontainers-python-app
 
- Προετοιμασία φακέλου εργασίας
    Δημιουργήστε έναν φάκελο και τοποθετήστε τα παρακάτω αρχείο, που βρίσκονται
    στον φάκελο docker_containers του repository 
    α) docker-compose.yml 
    b) annotationToolLauncher.bat
    c) Δημιουργήστε έναν υποφάκελο με όνομα data (εδώ θα αποθηκεύονται όλα τα δεδομένα)
        Σημείωση: Υπάρχουν έτοιμα παραδείγματα μέσα στον υποφάκελο data του repo

- Εκκίνηση Εργαλείου
   α) Κάντε διπλό click στο αρχείο bat
   b) Μετά από λίγα δευτερόλεπτα η εφαρμογή θα ανοίξει τον browser στη σελίδα
       http://localhost:8080 
   c) Οποιοσδήποτε χρήστης, εντός του τοπικού δικτύου έχει πρόσβαση στην εφαρμογή
   απο τη διεύθυνση http://<IP_HOST>:8080, όπου <IP_HOST> είναι το IP του Host
   που εκτελεί το Docker.
