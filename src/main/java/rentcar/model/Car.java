package rentcar.model;

public class Car {
    private int id;
    private String brand;
    private String model;
    private String licensePlate;
    private int year;
    private double pricePerDay;
    private CarStatus status;

    public Car(int id, String brand, String model, String licensePlate,
               int year, double pricePerDay, CarStatus status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.licensePlate = licensePlate;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    public int getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getLicensePlate() { return licensePlate; }
    public int getYear() { return year; }
    public double getPricePerDay() { return pricePerDay; }
    public CarStatus getStatus() { return status; }

    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setYear(int year) { this.year = year; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setStatus(CarStatus status) { this.status = status; }

    @Override
    public String toString() {
        return brand + " " + model + " [" + licensePlate + "]";
    }
}