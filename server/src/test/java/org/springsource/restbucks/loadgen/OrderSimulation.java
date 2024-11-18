package org.springsource.restbucks.loadgen;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.http.HeaderValues.ApplicationJson;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class OrderSimulation extends Simulation {

  final Duration duration = Duration.ofMinutes(120);
  static int usersAtOnce = 2;

  // Jackson object mapper for JSON parsing
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // Function to extract drink hrefs from the JSON response
  private static List<String> extractDrinkLinks(String jsonString) {
    List<String> drinkLinks = new ArrayList<>();
    try {
      JsonNode rootNode = objectMapper.readTree(jsonString);
      JsonNode drinksNode = rootNode.at("/_embedded/restbucks:drinks");
      if (drinksNode.isArray()) {
        for (JsonNode drink : drinksNode) {
          String drinkLink = drink.at("/_links/self/href").asText();
          if (!drinkLink.isEmpty()) {
            drinkLinks.add(drinkLink);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return drinkLinks;
  }

  // HTTP configuration
  HttpProtocolBuilder httpProtocol = http
      .baseUrl("http://localhost:8080")
      .acceptHeader(ApplicationJson())
      .contentTypeHeader(ApplicationJson());

  // Scenario to make requests to /drinks and /orders
  ScenarioBuilder scn = scenario("Drinks Order Scenario")
      .exec(
          http("Get Drinks List")
              .get("/drinks")
              .check(bodyString().saveAs("drinksResponse"))
      )
      .exec(session -> {
        String drinksResponse = session.getString("drinksResponse");
        List<String> drinkLinks = extractDrinkLinks(drinksResponse);

        // Save the list of drink links to session
        return session.set("drinkLinks", drinkLinks);
      })
      .asLongAs(session -> !session.getList("drinkLinks").isEmpty()).on(
          exec(session -> {
            List<String> drinkLinks = session.getList("drinkLinks");
            // Randomly select a drink
            String selectedDrink = drinkLinks.get(
                ThreadLocalRandom.current().nextInt(drinkLinks.size()));

            // Randomize location
            String location = ThreadLocalRandom.current().nextBoolean() ? "To go" : "In store";

            // Create order request body
            String orderRequestBody = String.format("{\"drinks\":[\"%s\"],\"location\":\"%s\"}",
                selectedDrink, location);

            // Save the request body to session
            return session.set("orderRequestBody", orderRequestBody);
          })
              .exec(
                  http("Create Order")
                      .post("/orders")
                      .body(StringBody(session -> session.getString("orderRequestBody"))).asJson()
                      .check(status().in(200, 201, 400, 500)) // Expecting valid status or errors
                      .check(bodyString().saveAs("orderResponse")) // Save order response to session
              )
              .exec(session -> {
                // Extract the order ID from the order response
                String orderResponse = session.getString("orderResponse");
                String orderId = null;
                try {
                  JsonNode rootNode = objectMapper.readTree(orderResponse);
                  JsonNode orderLinkNode = rootNode.at("/_links/self/href");
                  if (orderLinkNode != null) {
                    // Extract the order ID from the URL (assuming last segment is the ID)
                    String orderLink = orderLinkNode.asText();
                    orderId = orderLink.substring(orderLink.lastIndexOf('/') + 1);
                  }
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }

                // Save the order ID to the session
                return session.set("orderId", orderId);
              })
              .exec(session -> {
                // The only valid credit card
                String creditCardNumber = "1234123412341234";

                // Create payment request body
                String paymentRequestBody = String.format("{\"number\":\"%s\"}", creditCardNumber);

                // Save the payment request body to session
                return session.set("paymentRequestBody", paymentRequestBody);
              })
              .exec(
                  http("Submit Payment")
                      .put(session -> "/payment/" + session.getString("orderId"))
                      .body(StringBody(session -> session.getString("paymentRequestBody"))).asJson()
                      .check(status().in(200, 201)) // Expecting valid status or errors
              )
      );

  {
    setUp(
        scn.injectOpen(constantUsersPerSec(usersAtOnce).during(duration))
    ).protocols(httpProtocol);
  }
}
