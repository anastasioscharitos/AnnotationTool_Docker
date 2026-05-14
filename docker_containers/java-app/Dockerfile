# Χρήση επίσημου Image της Microsoft για το Playwright και Java
FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

# Εγκατάσταση locales
RUN apt-get update && apt-get install -y locales

#Ορισμός των αγγλικών ως προεπιλογή
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen

ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# Ορισμός UTF-8 encoding
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Ορισμός φακέλου εργασίας
WORKDIR /app

# Αντιγραφή του JAR
COPY annotation-tool-docker.jar .

# Ρύθμιση για τη χρήση προεγκατεστημένων browsers του Playwright
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

# Δημιουργία φακέλου για τα δεδομένα
RUN mkdir -p data

# Έκθεση στην θύρα 8080
EXPOSE 8080

#Εντολή εκκίνησης της εφαρμογής
ENTRYPOINT [ "java", "-jar", "annotation-tool-docker.jar" ]