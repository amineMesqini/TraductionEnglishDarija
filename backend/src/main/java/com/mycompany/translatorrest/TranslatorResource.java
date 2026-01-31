package com.mycompany.translatorrest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Path("/translate")
@Produces(MediaType.APPLICATION_JSON)
public class TranslatorResource {

    private static final String GROQ_API_KEY = "API";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @GET
    public Response translate(
            @QueryParam("text") String text,
            @DefaultValue("english") @QueryParam("from") String from,
            @DefaultValue("darija") @QueryParam("to") String to
    ) {

        if (text == null || text.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"text is required\"}")
                    .build();
        }

        try {
            // PROMPT SIMPLE (PAS DE \n DANGEREUX)
            String prompt = "Translate from " + from + " to " + to + ": " + text;

            // JSON CONSTRUIT SANS TEXT BLOCKS (Java 11 OK)
            String jsonBody =
                    "{"
                            + "\"model\":\"llama-3.1-8b-instant\","
                            + "\"messages\":["
                            + "{"
                            + "\"role\":\"user\","
                            + "\"content\":\"" + prompt.replace("\"", "\\\"") + "\""
                            + "}"
                            + "],"
                            + "\"temperature\":0.2"
                            + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return Response.ok(response.body()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
