package rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.mindsphere.sdk.auth.model.MindsphereCredentials;
import com.siemens.mindsphere.sdk.core.RestClientConfig;
import com.siemens.mindsphere.sdk.core.exception.MindsphereException;
import com.siemens.mindsphere.sdk.iot.timeseries.apiclient.TimeseriesClient;
import com.siemens.mindsphere.sdk.iot.timeseries.model.TimeseriesData;

import model.Entity;
import model.NotificationContent;

@Path("/container")
public class ContainerSink {
	private static Logger logger = LoggerFactory.getLogger(ContainerSink.class);

	private static Entity lastWeatherEntity; 

	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDataInJSON() { 
		 logger.debug("CIAO");
		 String result = "Data GET";
		 return Response.status(201).entity(result).build(); 
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDataInJSON(String data) { 
		System.out.println("test");
		 logger.debug("Data ="+data);
		ObjectMapper mapper = new ObjectMapper();



		//JSON from String to Object
		try {
			NotificationContent notificationContent = mapper.readValue(data, NotificationContent.class);
			 logger.debug("notificationContent="+notificationContent);
			for (Entity entity:notificationContent.getData()) {
				if (entity.getType().equalsIgnoreCase("container")){
					sendContainerToMS(entity);
					
				}
				if (entity.getType().equalsIgnoreCase("WeatherObserved")) {
					setLastWeatherEntity(entity);
					
				}
				//sendWeatherObserverdToMS(entity);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = "Data post: "+data;
		return Response.status(201).entity(result).build(); 
	}

/*
	private void sendWeatherObserverdToMS(Entity entity) {
		 logger.debug("************** sendWeatherObserverdToMS **************");


		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("config.properties").getFile());
			FileInputStream input= new FileInputStream(file);

			// load a properties file
			Properties prop = new Properties();

			prop.load(input);

			RestClientConfig config = RestClientConfig.builder()
					.connectionTimeoutInSeconds(100)
					.build();

			MindsphereCredentials credentials = MindsphereCredentials.builder()
					.clientId(prop.getProperty("client-id"))
					.clientSecret(prop.getProperty("client-secret"))
					.tenant("engineer")
					.build();



			TimeseriesClient timeseriesClient = TimeseriesClient.builder()
					.mindsphereCredentials(credentials)
					.restClientConfig(config)
					.build();
			try {
				List<TimeseriesData> timeSeriesList=new ArrayList<TimeseriesData>();



				TimeseriesData timeseriesPoint=new TimeseriesData();
				 logger.debug("entity.getAttributes().get(\"dateObserved\").getValue()="+entity.getAttributes().get("dateObserved").getValue());
				
				timeseriesPoint.getData().put("_time", entity.getAttributes().get("dateObserved").getValue());

				timeseriesPoint.getData().put("rainRate", entity.getAttributes().get("precipitation").getValue());

				timeSeriesList.add(timeseriesPoint);

				timeseriesClient.putTimeseries(prop.getProperty("asset-id"), prop.getProperty("aspect-id"), timeSeriesList, true);

			} catch (MindsphereException e) {
				// Exception handling
				e.printStackTrace();
			}

			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	


	}
*/
	private void sendContainerToMS(Entity entity) {
		 logger.debug("************** sendContainerToMS **************");

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("config.properties").getFile());
			FileInputStream input= new FileInputStream(file);

			// load a properties file
			Properties prop = new Properties();

			prop.load(input);

			RestClientConfig config = RestClientConfig.builder()
					.connectionTimeoutInSeconds(100)
					.build();

			MindsphereCredentials credentials = MindsphereCredentials.builder()
					.clientId(prop.getProperty("client-id"))
					.clientSecret(prop.getProperty("client-secret"))
					.tenant(prop.getProperty("tenant"))
					.build();

		

			TimeseriesClient timeseriesClient = TimeseriesClient.builder()
					.mindsphereCredentials(credentials)
					.restClientConfig(config)
					.build();
			try {
				List<TimeseriesData> timeSeriesList=new ArrayList<TimeseriesData>();



				TimeseriesData timeseriesPoint=new TimeseriesData();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				java.util.Date d =  new Date(Long.parseLong(entity.getAttributes().get("time").getValue().toString()));
				String date = df.format(d);
				timeseriesPoint.getData().put("_time", /*"2018-11-12T10:18:00.001Z"*/date);
				d =  new Date(Long.parseLong(entity.getAttributes().get("extimatedTimeArrival").getValue().toString()));
				date = df.format(d);
				timeseriesPoint.getData().put("extimatedTimeArrival", date);
				d =  new Date(Long.parseLong(entity.getAttributes().get("requestTimeArrival").getValue().toString()));
				date = df.format(d);
				timeseriesPoint.getData().put("requestedTimeArrival", date);
				timeseriesPoint.getData().put("latitude", Float.parseFloat(entity.getAttributes().get("latitude").getValue().toString()));
				timeseriesPoint.getData().put("longitude", Float.parseFloat(entity.getAttributes().get("longitude").getValue().toString()));
				timeseriesPoint.getData().put("sectorID", Integer.parseInt(entity.getAttributes().get("sectorId").getValue().toString()));
				timeseriesPoint.getData().put("tripID", Integer.parseInt(entity.getAttributes().get("tripId").getValue().toString()));
			
				timeseriesPoint.getData().put("tripTime", Long.parseLong(entity.getAttributes().get("tripTime").getValue().toString()));
				timeseriesPoint.getData().put("maxTripTime",  Long.parseLong(entity.getAttributes().get("maxTripTime").getValue().toString()));
				timeseriesPoint.getData().put("minTripTime",  Long.parseLong(entity.getAttributes().get("minTripTime").getValue().toString()));
				timeseriesPoint.getData().put("overMaxThreshold",  Long.parseLong(entity.getAttributes().get("tripTime").getValue().toString())>Long.parseLong(entity.getAttributes().get("maxTripTime").getValue().toString())?true:false);
				timeseriesPoint.getData().put("overMaxThresholdStatus",  Long.parseLong(entity.getAttributes().get("tripTime").getValue().toString())>Long.parseLong(entity.getAttributes().get("maxTripTime").getValue().toString())?1:0);
				if (getLastWeatherEntity()!=null) {
					timeseriesPoint.getData().put("rainRate", Float.parseFloat(getLastWeatherEntity().getAttributes().get("precipitation").getValue().toString()));
				}
				
				timeSeriesList.add(timeseriesPoint);

				timeseriesClient.putTimeseries(prop.getProperty("asset-id"), prop.getProperty("aspect-id"), timeSeriesList, true);

			} catch (MindsphereException e) {
				// Exception handling
				e.printStackTrace();
			}

		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	

	}


	public Entity getLastWeatherEntity() {
		return lastWeatherEntity;
	}


	public void setLastWeatherEntity(Entity lastWeatherEntity) {
		ContainerSink.lastWeatherEntity = lastWeatherEntity;
	}

}
