package org.calvinkeum;

import lombok.extern.slf4j.Slf4j;
import org.calvinkeum.service.DataImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

    private static final String URL = "https://live-test-scores.herokuapp.com/scores";

    @Override
    public void run(String... args) {
        readResults();
    }

    private static void readResults() {
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;

                while ((line = reader.readLine()) != null) {
                    // Validate event score data
                    if (!"event: score".equals(line)) {
                        continue;
                    }

                    // The next line should contain our JSON Data
                    if ((line = reader.readLine()) != null) {
                        DataImportService.importStudentExamData(line);
                    }
                }

                reader.close();
            }
            else {
                log.error("Failed to fetch data. Response code: " + responseCode);
            }

            connection.disconnect();
        }
        catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
