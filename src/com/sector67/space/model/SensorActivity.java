package com.sector67.space.model;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SENSOR_DATA")
public class SensorActivity {
	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(canBeNull = false)
	private String name;
    @DatabaseField(canBeNull = false)
	private Date timestamp;
    @DatabaseField(canBeNull = false)
	private String data;
    
    //For ORMLite
    public SensorActivity() {
    }

    public SensorActivity(String name, Date timestamp, String data) {
    	this.name = name;
    	this.timestamp = timestamp;
    	this.data = data;
    }
    
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
