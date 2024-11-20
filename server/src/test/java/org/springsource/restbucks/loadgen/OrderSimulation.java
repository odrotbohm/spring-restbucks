package org.springsource.restbucks.loadgen;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.jetbrains.annotations.Nullable;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.JsonPathLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.MediaType;

import static io.gatling.http.HeaderValues.ApplicationJson;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class OrderSimulation extends Simulation {

	final Duration duration = Duration.ofMinutes(120);
	static int usersAtOnce = 2;
	final ObjectMapper objectMapper = new ObjectMapper();

	// Function to extract drink hrefs from the JSON response
	private static List<String> extractDrinkLinks(String jsonString) {
		Links linksWithRel = new FixedHalLinkDiscoverer().findLinksWithRel(LinkRelation.of("restbucks:drink"), jsonString);
		return linksWithRel.stream().map(Link::getHref).toList();
	}

	static class FixedHalLinkDiscoverer extends JsonPathLinkDiscoverer {
		public FixedHalLinkDiscoverer() {
			this(MediaTypes.HAL_JSON);
		}

		protected FixedHalLinkDiscoverer(MediaType... mediaTypes) {
			super("$.._links.['%s']", mediaTypes);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.JsonPathLinkDiscoverer#extractLink(java.lang.Object, org.springframework.hateoas.LinkRelation)
		 */
		@Override @SuppressWarnings("unchecked") protected Link extractLink(Object element, LinkRelation rel) {

			if (!Map.class.isInstance(element)) {
				return super.extractLink(element, rel);
			}

			Map<String, String> json = (Map<String, String>) element;

			return Link.of(json.get("href"), rel) //
					.withHreflang(json.get("hreflang")) //
					.withMedia(json.get("media")) //
					.withTitle(json.get("title")) //
					.withType(json.get("type")) //
					.withDeprecation(json.get("deprecation")) //
					.withProfile(json.get("profile")) //
					.withName(json.get("name"));
		}
	}

	// HTTP configuration
	HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080").acceptHeader(ApplicationJson())
			.contentTypeHeader(ApplicationJson());

	// Scenario to make requests to /drinks and /orders
	ScenarioBuilder scn = scenario("Drinks Order Scenario").exec(
			http("Get Drinks List").get("/drinks").check(bodyString().saveAs("drinksResponse"))).exec(session -> {
		String drinksResponse = session.getString("drinksResponse");
		List<String> drinkLinks = extractDrinkLinks(drinksResponse);
		// Save the list of drink links to session
		return session.set("drinkLinks", drinkLinks);
	}).asLongAs(session -> !session.getList("drinkLinks").isEmpty()).on(exec(session -> {
		List<String> drinkLinks = session.getList("drinkLinks");
		// Randomly select a drink
		String selectedDrink = drinkLinks.get(ThreadLocalRandom.current().nextInt(drinkLinks.size()));

		// Randomize location
		String location = ThreadLocalRandom.current().nextBoolean() ? "To go" : "In store";

		// Create order request body
		String orderRequestBody = String.format("{\"drinks\":[\"%s\"],\"location\":\"%s\"}", selectedDrink, location);

		// Save the request body to session
		return session.set("orderRequestBody", orderRequestBody);
	}).exec(
					http("Create Order")
							.post("/orders") //
							.body(StringBody(session -> session.getString("orderRequestBody"))).asJson() //
							.check(status().in(200, 201)) // Expecting valid status or errors
							.check(bodyString().saveAs("orderResponse")) // Save order response to session
			).exec(session -> {
				// Extract the order ID from the order response
				String orderResponse = session.getString("orderResponse");
				HalLinkDiscoverer halLinkDiscoverer = new HalLinkDiscoverer();
				Link payment = tryToGetPayment(halLinkDiscoverer, orderResponse);
				Link self = halLinkDiscoverer.findRequiredLinkWithRel(IanaLinkRelations.SELF, orderResponse);
				return session.set("payment", payment != null ? payment.getHref() : payment) //
						.set("order", self.getHref()); //
			}).exec(session -> {
				// The only valid credit card
				String creditCardNumber = "1234123412341234";

				// Create payment request body
				String paymentRequestBody = String.format("{\"number\":\"%s\"}", creditCardNumber);

				// Save the payment request body to session
				return session.set("paymentRequestBody", paymentRequestBody);
			}).exec(
					http("Submit Payment") //
					.put(session -> session.get("payment")) //
					.body(StringBody(session -> session.getString("paymentRequestBody"))).asJson()
					.check(bodyString().saveAs("paymentResponse")) // Save order response to session
					.check(status().in(200, 201)) // Expecting valid status
			).asLongAs(session -> session.getString("receipt") == null && !"Taken".equals(session.getString("orderStatus")))
			.on(
					pause(1) //
					.exec(http("Get Order") //
							.get(session -> session.get("order")) //
							.check(bodyString().saveAs("orderResponse")) // Save order response to session
							.check(status().in(200, 201, 404))// Expecting valid status
			).exec(session -> {
				String orderResponse = session.getString("orderResponse"); //
				HalLinkDiscoverer halLinkDiscoverer = new HalLinkDiscoverer(); //
				Link receipt = getReceipt(halLinkDiscoverer, orderResponse);
				String orderStatus = readOrder(orderResponse); //
				return session.set("receipt", receipt != null ? receipt.getHref() : null).set("orderStatus", orderStatus);
			})).exec(http("Take Order") //
							.delete(session -> session.get("receipt")) //
							.check(status().in(200, 201, 404, 500)) //
					// Expecting valid status
			));

	private static Link getReceipt(HalLinkDiscoverer halLinkDiscoverer, String orderResponse) {
		Link receipt = null; try {
			receipt = halLinkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("restbucks:receipt"), orderResponse);
		} catch (Exception e) {
		} return receipt;
	}

	private String readOrder(String orderResponse) {
		String orderStatus = ""; try {
			JsonNode rootNode = objectMapper.readTree(orderResponse); JsonNode orderStatusLinkNode = rootNode.at("/status");
			if (orderStatusLinkNode != null) {
				// Extract the order ID from the URL (assuming last segment is the ID)
				orderStatus = orderStatusLinkNode.asText();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return orderStatus;
	}

	private static Link tryToGetPayment(HalLinkDiscoverer halLinkDiscoverer, String orderResponse) {
		try {
			return halLinkDiscoverer.findRequiredLinkWithRel(LinkRelation.of("restbucks:payment"), orderResponse);
		} catch (Exception ex) {
		}
		return null;
	}

	{
		setUp(scn.injectOpen(constantUsersPerSec(usersAtOnce).during(duration))).protocols(httpProtocol);
	}
}
