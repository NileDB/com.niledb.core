package spikes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class BOECrawler {

	public static void main(String args[]) throws Exception {

		File boeFolder = new File("boe/");
		if (!boeFolder.exists()) {
			boeFolder.mkdir();
		}
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

		// Get summaries
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 1961);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		while (true) {
			String summary = "BOE-S-" + sdf.format(calendar.getTime());
			
			File file = new File("boe/" + summary + ".xml");
			
			if (!file.exists()) {
				System.out.println("Getting " + summary + " summary");

				URL url = new URL("https://www.boe.es/diario_boe/xml.php?id=" + summary);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.connect();
				InputStream is = con.getInputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				FileOutputStream fos = new FileOutputStream("boe/" + summary + ".xml");
				while (len != (-1)) {
					len = is.read(buffer, 0, 1024);
					if (len != (-1))
						fos.write(buffer, 0, len);
				}
				fos.close();
				con.disconnect();
			}
			
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			if (calendar.getTime().getTime() > new Date().getTime()) {
				break;
			}
		}
		
		// Get journal summaries
		HashSet<String> pendingBOEs = new HashSet<String>();
		HashMap<String, Boolean> processedBOEs = new HashMap<String, Boolean>();
		
		File boeFiles = new File("boe/");
		String[] files = boeFiles.list();
		
		for (int i = 0; i < files.length; i++) {
			pendingBOEs.add(files[i].split("\\.")[0]);
		}
		
		while (!pendingBOEs.isEmpty()) {
			String boe = pendingBOEs.iterator().next();
			
			System.out.println("Processing " + boe);
			
			File file = new File("boe/" + boe + ".xml");
			
			if (!file.exists()) {
				System.out.println("Getting " + boe + ".xml");
				URL url = new URL("https://www.boe.es/diario_boe/xml.php?id=" + boe);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.connect();
				InputStream is = con.getInputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				FileOutputStream fos = new FileOutputStream("boe/" + boe + ".xml");
				while (len != (-1)) {
					len = is.read(buffer, 0, 1024);
					if (len != (-1))
						fos.write(buffer, 0, len);
				}
				fos.close();
				con.disconnect();
			}
			
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream("boe/" + boe + ".xml"));
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("sumario_nbo")) {
						String newBoe = startElement.getAttributeByName(new QName("id")).getValue();
						if (processedBOEs.get(newBoe) == null) {
							pendingBOEs.add(newBoe);
						}
					}
					// Si también se quieren coger estos !!!! Ufffffffff
					//else if (startElement.getName().getLocalPart().equals("item")) {
					//	String newBoe = startElement.getAttributeByName(new QName("id")).getValue();
					//	if (newBoe.startsWith("BOE-") && processedBOEs.get(newBoe) == null) {
					//		pendingBOEs.add(newBoe);
					//	}
					//}
				}
			}
			processedBOEs.put(boe, true);
			pendingBOEs.remove(boe);
		}
	}
}
