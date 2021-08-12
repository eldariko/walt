package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.dao.RestaurantRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Comparator.reverseOrder;

@Service
public class WaltServiceImpl implements WaltService {
    @Resource
    private  CustomerRepository customerRepository;
    @Resource
    private  RestaurantRepository restaurantRepository;
    @Resource
    private  DeliveryRepository deliveryRepository;
    @Resource
    private  DriverRepository driverRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        HashMap<String,List<Delivery>> driversDeliveries= new HashMap<>();
        List<Driver> availableDrivers=new ArrayList<>();
        long hourInMillis=TimeUnit.HOURS.toMillis(1);
        if(customer.getCity().getName().equals(restaurant.getCity().getName())) {
            List<Driver> drivers = driverRepository.findAllDriversByCity(customer.getCity());
            for (Driver driver : drivers) {
                List<Delivery> deliveriesByDriver = deliveryRepository.findByDriver(driver);
                if (deliveriesByDriver.size() > 0) {

                    List<Delivery> deliveriesInTheSameTime = deliveriesByDriver.
                            stream()
                            .filter
                                    (delivery -> delivery.getDeliveryTime().getTime() > deliveryTime.getTime()
                                            && delivery.getDeliveryTime().getTime() < deliveryTime.getTime() + hourInMillis)
                            .collect(Collectors.toList());
                    if (deliveriesInTheSameTime.size()==0) {//driver is available
                        availableDrivers.add(driver);
                        driversDeliveries.put(driver.getName(), deliveriesByDriver);
                    }
                }else{
                    availableDrivers.add(driver);
                    driversDeliveries.put(driver.getName(),deliveriesByDriver);
                }

            }

            Driver leastBusyDriver = null;
            int size=Integer.MAX_VALUE;
            if(availableDrivers.size()>0){//checking who is least busy
                for (Driver driver : availableDrivers){
                    List<Delivery> deliveriesOfDriver = driversDeliveries.get(driver.getName());
                    if (deliveriesOfDriver.size()<size) {
                        leastBusyDriver = driver;
                        size=deliveriesOfDriver.size();
                    }
                }
            }
            Delivery newDelivery= new Delivery(leastBusyDriver,restaurant,customer,deliveryTime);
            newDelivery.setDistance(ThreadLocalRandom.current().nextInt(0,21));
            return newDelivery;
        }else{
            System.out.println("No available drivers at the moment in your city");
            return null;
        }

    }

    @Override
    public List<DriverDistance> getDriverRankReport() {

        List<Delivery> deliveries = (List<Delivery>) deliveryRepository.findAll();
        List<Delivery> completedDeliveries = deliveries.stream().filter(delivery -> delivery.getDeliveryTime().after(new Date())).collect(Collectors.toList());
        Map<Driver,Double> ans = new HashMap<>();
        completedDeliveries.forEach(delivery -> {
            if (!ans.containsKey(delivery.getDriver())){
                ans.put(delivery.getDriver(), delivery.getDistance());
            }
            else {
                ans.replace(delivery.getDriver(), ans.get(delivery.getDriver()) + delivery.getDistance());
            }
        });
        List<DriverDistance> anss = new ArrayList<>();
        ans.forEach((k,v) -> anss.add(new DriverDistanceImpl(k, Double.valueOf( v).longValue())));
        return anss.stream().sorted((o1, o2) -> o1.getTotalDistance()-o2.getTotalDistance()<0? 1 :-1).collect(Collectors.toList());


//
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {

        List<DriverDistance> driverDistanceList=getDriverRankReport();
        return driverDistanceList
                .stream().filter(driverDistance -> driverDistance.getDriver().getCity().getName().equals(city.getName())).collect(Collectors.toList());
    }
}
