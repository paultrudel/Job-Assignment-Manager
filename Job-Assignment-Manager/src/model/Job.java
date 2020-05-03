package model;

import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;

public class Job {
	
	private StringProperty id;
	public StringProperty idProperty() {
		if(id == null)
			id = new SimpleStringProperty(this, "id");
		return id;
	}
	
	private StringProperty number;
	public StringProperty numberProperty() {
		if(number == null)
			number = new SimpleStringProperty(this, "num");
		return number;
	}
	
	private int type;
	private StringProperty typeProperty;
	public StringProperty typeProperty() {
		if(typeProperty == null)
			typeProperty = new SimpleStringProperty(this, "type");
		return typeProperty;
	}
	
	private int duration;
	private StringProperty durationProperty;
	public StringProperty durationProperty() {
		if(durationProperty == null)
			durationProperty = new SimpleStringProperty(this, "duration");
		return durationProperty;
	}
	
	private Point2D location;
	private StringProperty locationProperty;
	public StringProperty locationProperty() {
		if(locationProperty == null)
			locationProperty = new SimpleStringProperty(this, "location");
		return locationProperty;
	}
	private double payment;
	
	public Job() {
		idProperty().set(UUID.randomUUID().toString());
	}
	
	public Job(int type, int duration, double x, double y) {
		idProperty().set(UUID.randomUUID().toString());
		this.type = type;
		this.duration = duration;
		this.location = new Point2D(x, y);
		setTypeProperty(String.valueOf(type));
		setDurationProperty(String.valueOf(duration));
		setLocationProperty(String.valueOf(location.toString()));
		computePayment();
	}
	
	public Job(int type, int duration, Point2D location) {
		idProperty().set(UUID.randomUUID().toString());
		this.type= type;
		this.duration = duration;
		this.location = location;
		setTypeProperty(String.valueOf(type));
		setDurationProperty(String.valueOf(duration));
		setLocationProperty(String.valueOf(location.toString()));
		computePayment();
	}
	
	public void setNumberProperty(String number) { numberProperty().set(number); }
	private void setTypeProperty(String type) { typeProperty().set(type); }
	private void setDurationProperty(String duration) { durationProperty().set(duration); }
	private void setLocationProperty(String location) { locationProperty().set(location); }
	
	public String getId() { return idProperty().get(); }
	public String getNumber() { return numberProperty().get(); }
	public int getType() { return type; }
	public int getDuration() { return duration; }
	public Point2D getLocation() { return location; }
	public double getPayment() { return payment; }
	
	public String getTypeProperty() { return typeProperty().get(); }
	public String getDurationProperty() { return durationProperty().get(); }
	public String getLocationProperty() { return locationProperty().get(); }
	
	public void computePayment() {
		double payment = ((duration / 60.0) * Company.BASE_JOB_PAY) * type;
		this.payment = payment;
	}
}
