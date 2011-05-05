package eu.europeana.lod.spaqrl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.pubby.MappedResource;
import de.fuberlin.wiwiss.pubby.ModelResponse;
import de.fuberlin.wiwiss.pubby.ResourceDescription;
import de.fuberlin.wiwiss.pubby.vocab.FOAF;

public class RDFdata {
	
	
	public Model get(Model description, MappedResource resource){
		
		Model rdfDescription;
		// Add owl:sameAs statements referring to the original dataset URI
		Resource r = description.getResource(resource.getWebURI());
		if (resource.getDataset().getAddSameAsStatements()) {
			r.addProperty(OWL.sameAs, description.createResource(resource.getDatasetURI()));
		}
		
		// Add links to RDF documents with descriptions of the blank nodes
		StmtIterator it = r.listProperties();
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (!stmt.getObject().isAnon()) continue;
			String pathDataURL = resource.getPathDataURL(stmt.getPredicate());
			((Resource) stmt.getResource()).addProperty(RDFS.seeAlso, 
					description.createResource(pathDataURL));
		}
		it = description.listStatements(null, null, r);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (!stmt.getSubject().isAnon()) continue;
			String pathDataURL = resource.getInversePathDataURL(stmt.getPredicate());
			((Resource) stmt.getSubject().as(Resource.class)).addProperty(RDFS.seeAlso, 
					description.createResource(pathDataURL));
		}
		
		// Add document metadata
		if (description.qnameFor(FOAF.primaryTopic.getURI()) == null
				&& description.getNsPrefixURI("foaf") == null) {
			description.setNsPrefix("foaf", FOAF.NS);
		}
		if (description.qnameFor(RDFS.label.getURI()) == null
				&& description.getNsPrefixURI("rdfs") == null) {
			description.setNsPrefix("rdfs", RDFS.getURI());
		}
		rdfDescription=description;
		/*Resource document = description.getResource(addQueryString(resource.getDataURL(), request));
		document.addProperty(FOAF.primaryTopic, r);
		document.addProperty(RDFS.label, 
				"RDF description of " + 
				new ResourceDescription(resource, description, config).getLabel());
		resource.getDataset().addDocumentMetadata(description, document);
		resource.getDataset().addMetadataFromTemplate(description, resource, getServletContext());


		ModelResponse server = new ModelResponse(description, request, response);*/
		return rdfDescription;
	}

}
