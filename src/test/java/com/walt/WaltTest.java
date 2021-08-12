package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);

        createDeliveries(tlv);
    }

    private void createDeliveries(City tlv) {
        //Tlv
        String[] restaurantsInTlv={"vegan","cafe","chinese","mexican"};
        String[] driversInTlv={"Mary","Patricia","Daniel"};
        String[] customersInTlv={"Beethoven","Rachmaninoff","Bach"};
        //Jer
        String[] restaurantsInJer={"meat"};
        String[] driversInJer={"Robert","David","Neta"};
        String[] customersInJer={"Mozart"};

        List<Delivery> deliveries=new ArrayList<>();
        createDeliveries(deliveries,restaurantsInTlv,driversInTlv,customersInTlv);
        deliveries.clear();
        createDeliveries(deliveries,restaurantsInJer,driversInJer,customersInJer);

    }
    private void createDeliveries(List<Delivery> deliveries,String[] res,String[] dri,String[] cus){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (int i = 0; i < 100; i++) {
            int randRes=ThreadLocalRandom.current().nextInt(0,res.length);
            int randDri=ThreadLocalRandom.current().nextInt(0,dri.length);
            int randCus=ThreadLocalRandom.current().nextInt(0,cus.length);
            calendar.add(Calendar.HOUR_OF_DAY, ThreadLocalRandom.current().nextInt(-30,30));
            Delivery d= new Delivery(driverRepository.findByName(dri[randDri]),restaurantRepository.findByName(res[randRes]),
                    customerRepository.findByName(cus[randCus]),calendar.getTime());
            d.setDistance(ThreadLocalRandom.current().nextDouble(0,21));
            deliveries.add(d);

        }
        deliveryRepository.saveAll(deliveries);

    }
    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("mexican", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){

        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        Delivery d1=
                waltService.createOrderAndAssignDriver(customerRepository.findByName("Beethoven"),restaurantRepository.findByName("vegan"),calendar.getTime());
        System.out.println
                ("new Delivery created: Driver: "+d1.getDriver().getName()+" || Restaurant: "+d1.getRestaurant().getName()+" || City: "+d1.getRestaurant().getCity().getName()+" || Customer: "+d1.getCustomer().getName()+".");
        Delivery d2=waltService.createOrderAndAssignDriver(customerRepository.findByName("Mozart"),restaurantRepository.findByName("meat"),calendar.getTime());
        System.out.println
                ("new Delivery created: Driver: "+d2.getDriver().getName()+" || Restaurant: "+d2.getRestaurant().getName()+" || City: "+d2.getRestaurant().getCity().getName()+" || Customer: "+d2.getCustomer().getName()+".");


        printList(waltService.getDriverRankReport(),null);
        System.out.println("**************************");
        printList( waltService.getDriverRankReportByCity(cityRepository.findByName("Tel-Aviv")),cityRepository.findByName("Tel-Aviv"));

    }

    private void printList(List<DriverDistance> driverDistanceList,City optionalCity){
        if(optionalCity!= null)
            System.out.println("Outstanding employees so far by city: "+optionalCity.getName());
        else
            System.out.println("Outstanding employees so far in walt:");

        driverDistanceList.forEach(driverDistance -> System.out.println("| Driver: "+driverDistance.getDriver().getName()+" -- Total distance: "+driverDistance.getTotalDistance().toString()+"Km |"));
        waltService.getDriverRankReport();
    }

}
