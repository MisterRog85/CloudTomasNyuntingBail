package cloudTomasNyuntingBail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

@Api(name = "cloud", version = "v1",
namespace = @ApiNamespace(ownerDomain = "helloworld.example.com",
ownerName = "helloworld.example.com",
packagePath = ""))
public class PetitionEndpoint {
	@ApiMethod(name = "newPet")
	public Entity newPet(@Named("owner") String owner, @Named("contenu") String contenu, @Named("titre") String titre ) {
			
		Entity e = new Entity("Petition");
		e.setProperty("owner", owner);
		e.setProperty("titre", titre);
		e.setProperty("contenu", contenu);
		e.setProperty("nbSignataires", 0);
		int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000 + 1);
		String id = titre.replace("\\s+","") + Integer.toString(randomNum);
		e.setProperty("id", id);
		

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		
		return  e;
	}
	
	@ApiMethod(name = "listAllPets")
	public List<Entity> listPetitionsEntity() {
		Query q = new Query("Petition");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		List<Entity> result = prepQuery.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name = "detailPet", httpMethod = HttpMethod.GET)
	public List<Entity> detailPet(@Named("id") String id) {
		Filter propertyFilter =
			    new FilterPredicate("id", FilterOperator.EQUAL, id);
		Query q = new Query("Petition").setFilter(propertyFilter);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		List<Entity> result = prepQuery.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name = "detailPetKey", httpMethod = HttpMethod.GET)
	public Entity detailPetKey(@Named("id") String key) {
		Filter propertyFilter =
			    new FilterPredicate("id", FilterOperator.EQUAL, key);
		Query q = new Query("Petition").setFilter(propertyFilter);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		Entity result = prepQuery.asSingleEntity();
		return result;
	}
	
	@ApiMethod(name = "updateNbSignataires", httpMethod = HttpMethod.PUT, path="/updateSinataires")
	public void updateNbSignataires(@Named("petition") String id) {
		Filter propertyFilter =
			    new FilterPredicate("id", FilterOperator.EQUAL, id);
		Query q = new Query("Petition").setFilter(propertyFilter);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		Entity result = prepQuery.asSingleEntity();
		Long nb = (Long) result.getProperty("nbSignataires");
		result.setProperty("nbSignataires", nb+1);
		DatastoreService datastore1 = DatastoreServiceFactory.getDatastoreService();
		datastore1.put(result);
		//return result;
	}
	
	@ApiMethod(name="signerPet", httpMethod = HttpMethod.POST, path="/signer")
	public Entity signature(@Named("petition") String petition, @Named("signataire") String signataire) {
		Entity e = new Entity("Signataires");
		e.setProperty("petition", petition);
		e.setProperty("signataire", signataire);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		updateNbSignataires(petition);
		return e;
	}
	
	@ApiMethod(name = "listAllSignatures", path = "/signatures")
	public List<Entity> listAllSignatures() {
		Query q = new Query("Signataires");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		List<Entity> result = prepQuery.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name="listerMesPets", path = "/mespets")
	public List<Entity> listerMesPets(@Named("id") String id) {
		List<Entity> resultat = new ArrayList<Entity>();
		Filter propertyFilter =
			    new FilterPredicate("signataire", FilterOperator.EQUAL, id);
		Query q = new Query("Signataires").setFilter(propertyFilter);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery prepQuery = datastore.prepare(q);
		List<Entity> results = prepQuery.asList(FetchOptions.Builder.withDefaults());
		for (Entity result : results) { 
			resultat.add(detailPetKey((String) result.getProperty("petition"))); 
		}
		return resultat;
	}

}
