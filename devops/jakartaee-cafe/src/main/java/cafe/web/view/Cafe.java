package cafe.web.view;

import java.io.IOException;
import java.util.List;

import cafe.model.entity.Coffee;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

@Named
@RequestScoped
public class Cafe {
	private static final String BASE_URI = "http://localhost:9080/jakartaee-cafe/rest/coffees";

	private Client client;

	@NotBlank(message = "Name can't be blank")
	private String name;
	@NotNull(message = "Price can't be null")
	@PositiveOrZero(message = "Price can't be negative")
	private double price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@PostConstruct
	private void init() {
		client = ClientBuilder.newBuilder().build();
	}

	public List<Coffee> getCoffeeList() {
		return client.target(BASE_URI).path("/").request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<Coffee>>() {
				});
	}

	public void addCoffee() throws IOException {
		Coffee coffee = new Coffee(this.name, price);
		client.target(BASE_URI).request(MediaType.APPLICATION_JSON).post(Entity.json(coffee));
	}

	public void removeCoffee(String coffeeId) throws IOException {
		client.target(BASE_URI).path(coffeeId).request().delete();
	}
}
