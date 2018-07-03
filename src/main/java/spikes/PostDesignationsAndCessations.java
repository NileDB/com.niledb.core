package spikes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class PostDesignationsAndCessations {
	public static final String hostname = "localhost";
	public static final int port = 7777;
	
	public static void main(String[] args) throws Exception {
		Map<String, Boolean> organizations = new HashMap<String, Boolean>();
		
		final Vertx vertx = Vertx.vertx();

		WebClientOptions options = new WebClientOptions()
				.setKeepAlive(true)
				.setUsePooledBuffers(true);
		
		WebClient client = WebClient.create(vertx, options);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		
		File boeFiles = new File("boe/");
		String[] files = boeFiles.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("BOE-S-[0-9]{4}-[0-9]+\\.xml");
			}
		});

		Arrays.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int c = o1.substring(0, 10).compareTo(o2.substring(0, 10));
				return (c != 0 ? c : new Integer(Integer.parseInt(o1.substring(11).split("\\.")[0])).compareTo(Integer.parseInt(o2.substring(11).split("\\.")[0])));
			}
		});
		
		boolean isNombramiento = false;
		boolean isTitulo = false;
		boolean isFecha = false;
		boolean isUrlPdf = false;

		String departamento = null;
		Date fecha = null;
		String titulo = null;
		String urlPdf = null;
		
		int contador = 0;

		for (int i = 0; i < files.length; i++) {
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream("boe/" + files[i]));
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("fecha")) {
						isFecha = true;
						isTitulo = false;
						isUrlPdf = false;
					}
					else if (startElement.getName().getLocalPart().equals("item")) {
						titulo = null;
						urlPdf = null;
						isFecha = false;
						isTitulo = false;
						isUrlPdf = false;
					}
					else if (startElement.getName().getLocalPart().equals("departamento")) {
						departamento = startElement.getAttributeByName(new QName("nombre")).getValue();

						if (departamento != null && !departamento.equals("") && organizations.get(departamento) == null) {
							organizations.put(departamento, true);
							if (contador % 1000 == 0) {
								client.close();
								client = WebClient.create(vertx, options);
							}
							
							String query = "mutation createOrganization($name: String!) {\n" + 
									"  OrganizationCreate(\n" + 
									"    entity: {\n" + 
									"      name: $name\n" + 
									"    }\n" + 
									"  ) {\n" + 
									"    id\n" + 
									"  }\n" + 
									"}";
							
							JsonObject variables = new JsonObject()
									.put("authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXIxIn0.KzWjEGN7z69BamdZ_RY5-iNmEudfw30X9kn-qzYw378")
									.put("name", departamento);
	
							JsonObject request = new JsonObject()
									.put("query", query)
									.put("variables", variables);
							
							client
								.post(port, hostname, "/graphql")
								.sendJson(request, response -> {
								});
						}
						
						isFecha = false;
						isTitulo = false;
						isUrlPdf = false;
					}
					else if (startElement.getName().getLocalPart().equals("epigrafe")
							|| startElement.getName().getLocalPart().equals("seccion")) {
						String epigrafe = startElement.getAttributeByName(new QName("nombre")).getValue().toLowerCase();
						isNombramiento = epigrafe.contains("nombramiento") || epigrafe.contains("cese");
						isFecha = false;
						isTitulo = false;
						isUrlPdf = false;
					}
					else if (departamento != null && isNombramiento && startElement.getName().getLocalPart().equals("titulo")) {
						isFecha = false;
						isTitulo = true;
						isUrlPdf = false;
					}
					else if (startElement.getName().getLocalPart().equals("urlPdf")) {
						isFecha = false;
						isTitulo = false;
						isUrlPdf = true;
					}
					else {
						isFecha = false;
						isTitulo = false;
						isUrlPdf = false;
					}
				}
				else if (isTitulo && xmlEvent.isCharacters()) {
					Characters characters = xmlEvent.asCharacters();
					titulo = characters.getData();
					isFecha = false;
					isTitulo = false;
					isUrlPdf = false;
				}
				else if (isFecha && xmlEvent.isCharacters()) {
					Characters characters = xmlEvent.asCharacters();
					fecha = sdf.parse(characters.getData());
					isFecha = false;
					isTitulo = false;
					isUrlPdf = false;
				}
				else if (isUrlPdf && xmlEvent.isCharacters()) {
					Characters characters = xmlEvent.asCharacters();
					urlPdf = "https://boe.es" + characters.getData();
					isFecha = false;
					isTitulo = false;
					isUrlPdf = false;
				}
				
				if (titulo != null && urlPdf != null && fecha != null) {
					if (contador % 1000 == 0) {
						client.close();
						client = WebClient.create(vertx, options);
					}
					
					try {
						
						String query = "mutation createPersonPosition($title: String!, $boe: String!, $date: String!) {\n" + 
								"  PersonPositionCreate(\n" + 
								"    entity: {\n" + 
								"      title: $title\n" + 
								"      boe: $boe\n" + 
								"      date: $date\n" + 
								"    }\n" + 
								"  ) {\n" + 
								"    id\n" + 
								"  }\n" + 
								"}";
						
						JsonObject variables = new JsonObject()
								.put("authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXIxIn0.KzWjEGN7z69BamdZ_RY5-iNmEudfw30X9kn-qzYw378")
								.put("date", sdf2.format(fecha))
								.put("title", titulo)
								.put("boe", urlPdf);
	
						JsonObject request = new JsonObject()
								.put("query", query)
								.put("variables", variables);
						
						client
							.post(port, hostname, "/graphql")
							.sendJson(request, response -> {
							});
						
						titulo = null;
						urlPdf = null;
						contador++;
						Thread.sleep(5);
					}
					catch (Exception e) {
						e.printStackTrace();
						
						Thread.sleep(10000);

						client.close();
						client = WebClient.create(vertx, options);
						
					}
				}
			}
		}
		System.out.println(contador);
		client.close();
		vertx.close();
	}
}
