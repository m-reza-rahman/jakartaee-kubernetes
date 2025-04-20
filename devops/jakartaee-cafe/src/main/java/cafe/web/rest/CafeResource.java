package cafe.web.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cafe.model.CafeRepository;
import cafe.model.entity.Coffee;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("coffees")
public class CafeResource {

	private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	@Inject
	private CafeRepository cafeRepository;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Coffee> getAllCoffees() {
		return this.cafeRepository.getAllCoffees();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response createCoffee(Coffee coffee) {
		try {
			coffee = this.cafeRepository.persistCoffee(coffee);
			return Response.created(URI.create(coffee.getId().toString())).build();
		} catch (PersistenceException e) {
			logger.log(Level.SEVERE, "Error creating coffee {0}: {1}.", new Object[] { coffee, e });
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Coffee getCoffeeById(@PathParam("id") Long coffeeId) {
		return this.cafeRepository.findCoffeeById(coffeeId);
	}

	@DELETE
	@Path("{id}")
	public void deleteCoffee(@PathParam("id") Long coffeeId) {
		try {
			this.cafeRepository.removeCoffeeById(coffeeId);
		} catch (IllegalArgumentException ex) {
			logger.log(Level.SEVERE, "Error deleting coffee with ID {0}: {1}.", new Object[] { coffeeId, ex });
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
}
