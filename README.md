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
    b) Launcher.bat
    c) Δημιουργήστε έναν υποφάκελο με όνομα data (εδώ θα αποθηκεύονται όλα τα δεδομένα)
        Σημείωση: Υπάρχουν έτοιμα παραδείγματα μέσα στον υποφάκελο data του repo

- Εκκίνηση Εργαλείου
   α) Κάντε διπλό click στο αρχείο bat
   b) Μετά από λίγα δευτερόλεπτα (15 sec από την εκκίνηση των containers) η εφαρμογή
     θα ανοίξει τον browser στη σελίδα http://localhost:8080 
   c) Οποιοσδήποτε χρήστης, εντός του τοπικού δικτύου έχει πρόσβαση στην εφαρμογή
   απο τη διεύθυνση http://<IP_HOST>:8080, όπου <IP_HOST> είναι το IP του Host
   που εκτελεί το Docker.
- Παρατήρηση:
  Όσο εκτελείται η εφαρμογή δεν πρέπει να κλείσει το terminal.
   Αν συμβεί αυτό θα πρέπει:
    - Είτε να ανοίξετε νέο terminal και να εκτελέσετε την εντολή
     docker-compose down, μέσα στον φάκελο που βρίσκεται το αρχείο docker-compose.yml.
    - Είτε μέσω του docker desktop να σταματήσετε ή να διαγράψετε τα αντίστοιχα Containers
      (όχι τα Ιmages)
