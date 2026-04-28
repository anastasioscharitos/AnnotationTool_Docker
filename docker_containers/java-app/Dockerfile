#Χρήση επίσημου Image της Microsoftγια το Playwright και Java
FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

# Ορισμός φακέλου εργασίας
WORKDIR /app

# Αντιγραφή του JAR
COPY annotation-tool-docker-containers.jar .

# Ρύθμιση για τη χρήση προεγκατεστημένων browsers του Playwright
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

# Δημιουργία φακέλου για τα δεδομένα
RUN mkdir -p data

# Έκθεση στην θύρα 8080
EXPOSE 8080

#Εντολή εκκίνησης της εφαρμογής
ENTRYPOINT [ "java", "-jar", "annotation-tool-docker-containers.jar" ]