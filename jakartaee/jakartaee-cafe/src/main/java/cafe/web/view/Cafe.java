package cafe.web.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import cafe.model.entity.Coffee;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

@Named
@SessionScoped
public class Cafe implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient String baseUri;
	private transient Client client;

	@NotBlank
	private String name;
	@NotNull
	@PositiveOrZero
	private Number price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getPrice() {
		return price;
	}

	public void setPrice(Number price) {
		this.price = price;
	}

	@PostConstruct
	private void init() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		baseUri = "http://localhost:9080" + request.getContextPath() + "/rest/coffees";
		client = ClientBuilder.newBuilder().build();
	}

	public List<Coffee> getCoffeeList() {
		return client.target(baseUri).path("/").request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<Coffee>>() {
				});
	}

	public void addCoffee() throws IOException {
		Coffee coffee = new Coffee(this.name, this.price.doubleValue());
		client.target(baseUri).request(MediaType.APPLICATION_JSON).post(Entity.json(coffee));
		FacesContext.getCurrentInstance().getExternalContext().redirect("");
	}

	public void removeCoffee(String coffeeId) throws IOException {
		client.target(baseUri).path(coffeeId).request().delete();
		FacesContext.getCurrentInstance().getExternalContext().redirect("");
	}

	// This method is called after de-serialization to initialize transient fields.
	private Object readResolve() {
		init();
		return this;
	}
}
